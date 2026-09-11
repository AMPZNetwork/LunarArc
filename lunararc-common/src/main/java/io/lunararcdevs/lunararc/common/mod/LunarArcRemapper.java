package io.lunararcdevs.lunararc.common.mod;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import io.lunararcdevs.lunararc.common.server.LunarArcVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LunarArcRemapper extends org.objectweb.asm.commons.Remapper {
    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/Remapper");

    private static final String CRAFTBUKKIT_PREFIX = "org/bukkit/craftbukkit/";

    private static final Map<String, String> CLASS_MAP = new HashMap<>();

    private static final Map<String, String> MOJANG_TO_SPIGOT_CLASS = new HashMap<>();

    private static final Map<MemberKey, String> FIELD_MAP = new HashMap<>();
    private static final Map<MemberKey, String> METHOD_MAP = new HashMap<>();
    private static final Map<MemberNameKey, String> FIELD_NAME_MAP = new HashMap<>();
    private static final Map<MemberNameKey, String> METHOD_NAME_MAP = new HashMap<>();
    // Indexes METHOD_MAP by (owner, name) so overload resolution by parameter
    // descriptor never has to scan the full NMS method-mapping table (which can
    // hold tens of thousands of entries). Built once alongside METHOD_MAP.
    private static final Map<MemberNameKey, List<Map.Entry<MemberKey, String>>> METHOD_OVERLOAD_INDEX = new HashMap<>();
    private static final Map<String, String> RUNTIME_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> RUNTIME_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> BYTECODE_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> BYTECODE_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> ALREADY_CORRECT_CACHE = new ConcurrentHashMap<>();
    private static final int DYNAMIC_CACHE_LIMIT = 16_384;

    private final boolean remapNms;

    static {
        loadMappings();
    }

    public LunarArcRemapper() {
        this(true);
    }

    public LunarArcRemapper(boolean remapNms) {
        this.remapNms = remapNms;
    }

    public boolean isNmsRemappingEnabled() {
        return this.remapNms;
    }

    public static void verifyCompatibilityOutput(byte[] bytes, String className) {
        if (bytes == null || bytes.length < 8) return;
        if (containsAscii(bytes, "net/minecraft/server/v1_") || containsAscii(bytes, "org/bukkit/craftbukkit/v1_")) {
            LOGGER.warn("Post-transform compatibility check failed for {}: remapped bytecode still "
                            + "contains a legacy versioned NMS/CraftBukkit symbol — the transform likely "
                            + "did not fully remap this class.", className);
        }
    }

    private static void loadMappings() {
        String base = "mappings/" + LunarArcVersionInfo.minecraftVersion() + "/";
        ClassLoader loader = LunarArcRemapper.class.getClassLoader();

        try {

            try (InputStream stream = loader.getResourceAsStream(base + "paper-reobf.tiny")) {
                if (stream == null) {
                    throw new IllegalStateException("Missing Paper reobf mappings " + base + "paper-reobf.tiny");
                }
                loadPaperMappings(stream);
            }


            try (InputStream stream = loader.getResourceAsStream(base + "plugin-remap.tsv")) {
                if (stream != null) loadOverrides(stream);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void loadPaperMappings(InputStream stream) throws Exception {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            lines = reader.lines().toList();
        }
        if (lines.isEmpty()) throw new IllegalStateException("Paper mapping resource is empty");

        String[] header = lines.get(0).split("\\t", -1);
        if (header.length == 0) throw new IllegalStateException("Invalid Paper mapping header");

        List<PendingMember> pending = new ArrayList<>();
        if ("tiny".equals(header[0])) {
            parseTinyV2(lines, header, pending);
        } else if ("v1".equals(header[0])) {
            parseTinyV1(lines, header, pending);
        } else if ("tsrg2".equals(header[0])) {
            parseTsrg2(lines, pending);
        } else {
            throw new IllegalStateException("Unsupported Paper mapping format: " + lines.get(0));
        }


        for (PendingMember member : pending) {
            String spigotDescriptor = mapDescriptorClasses(member.mojangDescriptor, MOJANG_TO_SPIGOT_CLASS);
            MemberKey key = new MemberKey(member.spigotOwner, member.spigotName, spigotDescriptor);
            if (member.method) {
                putMethod(key, member.mojangName);
                addUniqueNameMapping(METHOD_NAME_MAP, new MemberNameKey(member.spigotOwner, member.spigotName), member.mojangName);
            } else {
                FIELD_MAP.put(key, member.mojangName);
                addUniqueNameMapping(FIELD_NAME_MAP, new MemberNameKey(member.spigotOwner, member.spigotName), member.mojangName);
            }
        }
    }

    /** Single write path for METHOD_MAP so METHOD_OVERLOAD_INDEX can never drift out of sync with it. */
    private static void putMethod(MemberKey key, String mojangName) {
        METHOD_MAP.put(key, mojangName);
        METHOD_OVERLOAD_INDEX
                .computeIfAbsent(new MemberNameKey(key.owner(), key.name()), ignored -> new ArrayList<>())
                .add(Map.entry(key, mojangName));
    }

    private static void parseTinyV2(List<String> lines, String[] header, List<PendingMember> pending) {
        if (header.length < 5 || !"2".equals(header[1])) {
            throw new IllegalStateException("Unsupported Tiny header: " + lines.get(0));
        }
        int sourceIndex = 3;
        int targetIndex = findSpigotNamespace(header, sourceIndex + 1);
        if (targetIndex < 0) targetIndex = header.length - 1;

        String mojangOwner = null;
        String spigotOwner = null;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] p = line.split("\\t", -1);

            if (p.length >= 3 && "c".equals(p[0])) {
                mojangOwner = p[1];
                int mappedColumn = targetIndex - sourceIndex + 1;
                if (mappedColumn >= p.length) continue;
                spigotOwner = p[mappedColumn];
                if (!mojangOwner.isEmpty() && !spigotOwner.isEmpty()) {
                    CLASS_MAP.put(spigotOwner, mojangOwner);
                    MOJANG_TO_SPIGOT_CLASS.put(mojangOwner, spigotOwner);
                }
                continue;
            }


            if (p.length >= 5 && p[0].isEmpty() && ("f".equals(p[1]) || "m".equals(p[1]))
                    && mojangOwner != null && spigotOwner != null) {
                int mappedColumn = targetIndex - sourceIndex + 3;
                if (mappedColumn >= p.length) continue;
                String descriptor = p[2];
                String mojangName = p[3];
                String spigotName = p[mappedColumn];
                if (!mojangName.isEmpty() && !spigotName.isEmpty()) {
                    pending.add(new PendingMember("m".equals(p[1]), spigotOwner, spigotName, descriptor, mojangName));
                }
            }
        }
    }

    private static void parseTinyV1(List<String> lines, String[] header, List<PendingMember> pending) {
        int targetIndex = findSpigotNamespace(header, 1);
        if (targetIndex < 0) targetIndex = header.length - 1;
        int mappedNameOffset = targetIndex;

        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\t", -1);
            if (p.length < 3) continue;
            switch (p[0]) {
                case "CLASS" -> {
                    if (mappedNameOffset >= p.length) continue;
                    String mojang = p[1];
                    String spigot = p[mappedNameOffset];
                    if (!mojang.isEmpty() && !spigot.isEmpty()) {
                        CLASS_MAP.put(spigot, mojang);
                        MOJANG_TO_SPIGOT_CLASS.put(mojang, spigot);
                    }
                }
                case "FIELD", "METHOD" -> {

                    int mappedColumn = targetIndex + 2;
                    if (p.length <= mappedColumn) continue;
                    String mojangOwner = p[1];
                    String spigotOwner = MOJANG_TO_SPIGOT_CLASS.getOrDefault(mojangOwner, mojangOwner);
                    pending.add(new PendingMember("METHOD".equals(p[0]), spigotOwner, p[mappedColumn], p[2], p[3]));
                }
                default -> {
                }
            }
        }
    }


    private static void parseTsrg2(List<String> lines, List<PendingMember> pending) {
        String mojangOwner = null;
        String spigotOwner = null;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) continue;
            if (!Character.isWhitespace(line.charAt(0))) {
                String[] p = line.trim().split("\\s+");
                if (p.length >= 2) {
                    mojangOwner = p[0];
                    spigotOwner = p[1];
                    CLASS_MAP.put(spigotOwner, mojangOwner);
                    MOJANG_TO_SPIGOT_CLASS.put(mojangOwner, spigotOwner);
                }
                continue;
            }
            if (mojangOwner == null || spigotOwner == null) continue;
            String[] p = line.trim().split("\\s+");
            if (p.length == 2) {

                pending.add(new PendingMember(false, spigotOwner, p[1], "*", p[0]));
            } else if (p.length >= 3 && p[1].startsWith("(")) {
                pending.add(new PendingMember(true, spigotOwner, p[2], p[1], p[0]));
            } else if (p.length >= 3) {
                pending.add(new PendingMember(false, spigotOwner, p[p.length - 1], p[1], p[0]));
            }
        }
    }

    private static int findSpigotNamespace(String[] header, int start) {
        for (int i = start; i < header.length; i++) {
            String value = header[i].toLowerCase(java.util.Locale.ROOT);
            if (value.contains("spigot") || value.contains("reobf")) return i;
        }
        return -1;
    }

    private static void loadOverrides(InputStream stream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\t", -1);
                switch (parts[0]) {
                    case "CLASS" -> {
                        if (parts.length != 3) throw invalid(line);
                        CLASS_MAP.put(parts[1], parts[2]);
                        MOJANG_TO_SPIGOT_CLASS.put(parts[2], parts[1]);
                    }
                    case "FIELD" -> {
                        if (parts.length != 5) throw invalid(line);
                        FIELD_MAP.put(new MemberKey(parts[1], parts[2], parts[3]), parts[4]);
                        addUniqueNameMapping(FIELD_NAME_MAP, new MemberNameKey(parts[1], parts[2]), parts[4]);
                    }
                    case "METHOD" -> {
                        if (parts.length != 5) throw invalid(line);
                        putMethod(new MemberKey(parts[1], parts[2], parts[3]), parts[4]);
                        addUniqueNameMapping(METHOD_NAME_MAP, new MemberNameKey(parts[1], parts[2]), parts[4]);
                    }
                    default -> throw invalid(line);
                }
            }
        }
    }

    private static final String AMBIGUOUS = "\u0000";

    private static void addUniqueNameMapping(Map<MemberNameKey, String> map, MemberNameKey key, String value) {
        String previous = map.putIfAbsent(key, value);
        if (previous != null && !previous.equals(value)) map.put(key, AMBIGUOUS);
    }

    private static IllegalStateException invalid(String line) {
        return new IllegalStateException("Invalid plugin mapping entry: " + line);
    }

    @Override
    public String map(String internalName) {
        if (internalName == null) return null;

        // Paper-style CraftBukkit is canonical and unversioned on LunarArc.
        // Spigot plugins may still be compiled against versioned CraftBukkit names
        // such as org/bukkit/craftbukkit/v1_21_R1/entity/CraftPlayer. Rewrite only
        // that version segment to the canonical unversioned Paper package.
        if (internalName.startsWith(CRAFTBUKKIT_PREFIX)) {
            String remainder = internalName.substring(CRAFTBUKKIT_PREFIX.length());
            int slash = remainder.indexOf('/');
            if (slash > 0 && remainder.substring(0, slash).matches("v\\d+_\\d+_R\\d+")) {
                return CRAFTBUKKIT_PREFIX + remainder.substring(slash + 1);
            }
            return internalName;
        }

        if (!remapNms) return internalName;
        String mapped = CLASS_MAP.get(internalName);
        if (mapped != null) return mapped;

        int nested = internalName.indexOf('$');
        if (nested > 0) {
            String mappedOwner = CLASS_MAP.get(internalName.substring(0, nested));
            if (mappedOwner != null) return mappedOwner + internalName.substring(nested);
        }
        return internalName;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        if (!remapNms || owner == null || name == null) return name;

        String spigotOwner = toSpigotOwner(owner);
        String lookupDescriptor = toSpigotDescriptor(descriptor);
        String mapped = FIELD_MAP.get(new MemberKey(spigotOwner, name, lookupDescriptor));
        if (mapped == null && !java.util.Objects.equals(lookupDescriptor, descriptor)) {
            mapped = FIELD_MAP.get(new MemberKey(spigotOwner, name, descriptor));
        }
        if (mapped == null) mapped = FIELD_MAP.get(new MemberKey(spigotOwner, name, "*"));
        if (mapped == null) {
            String unique = FIELD_NAME_MAP.get(new MemberNameKey(spigotOwner, name));
            if (unique != null && !AMBIGUOUS.equals(unique)) mapped = unique;
        }
        if (mapped == null && spigotOwner.startsWith("net/minecraft/")) {
            String key = spigotOwner + '#' + name + '#' + descriptor;
            mapped = boundedComputeIfAbsent(BYTECODE_FIELD_CACHE, key,
                    ignored -> resolveInheritedBytecodeMember(spigotOwner, name, descriptor, false));
            if (name.equals(mapped)) mapped = null;
        }
        if (mapped == null && spigotOwner.startsWith("net/minecraft/")
                && !namesRuntimeMember(runtimeClassFor(spigotOwner), name, false)) {
            LOGGER.warn("No mapping found for NMS field {}#{} {} — plugin bytecode will keep the "
                            + "unmapped name and is likely to throw NoSuchFieldError at runtime.",
                    spigotOwner, name, descriptor);
        }
        return mapped != null ? mapped : name;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (!remapNms || "<init>".equals(name) || "<clinit>".equals(name)) return name;
        String spigotOwner = toSpigotOwner(owner);
        String lookupDescriptor = toSpigotDescriptor(descriptor);
        String mapped = METHOD_MAP.get(new MemberKey(spigotOwner, name, lookupDescriptor));
        if (mapped == null && !java.util.Objects.equals(lookupDescriptor, descriptor)) {
            mapped = METHOD_MAP.get(new MemberKey(spigotOwner, name, descriptor));
        }
        // Both lookups below are descriptor-blind, so each answer is checked against the runtime
        // class before it is allowed to rewrite a call site. See runtimeHasMethod.
        if (mapped == null) {
            String wildcard = METHOD_MAP.get(new MemberKey(spigotOwner, name, "*"));
            if (wildcard != null && runtimeHasMethod(spigotOwner, wildcard, descriptor)) mapped = wildcard;
        }
        if (mapped == null) {
            String unique = METHOD_NAME_MAP.get(new MemberNameKey(spigotOwner, name));
            if (unique != null && !AMBIGUOUS.equals(unique)
                    && runtimeHasMethod(spigotOwner, unique, descriptor)) {
                mapped = unique;
            }
        }
        if (mapped == null && spigotOwner.startsWith("net/minecraft/")) {
            String key = spigotOwner + '#' + name + '#' + descriptor;
            mapped = boundedComputeIfAbsent(BYTECODE_METHOD_CACHE, key,
                    ignored -> resolveInheritedBytecodeMember(spigotOwner, name, descriptor, true));
            if (name.equals(mapped)) mapped = null;
        }
        if (mapped == null && spigotOwner.startsWith("net/minecraft/")
                && !namesRuntimeMember(runtimeClassFor(spigotOwner), name, true)) {
            LOGGER.warn("No mapping found for NMS method {}#{} {} — plugin bytecode will keep the "
                            + "unmapped name and is likely to throw NoSuchMethodError at runtime.",
                    spigotOwner, name, descriptor);
        }
        return mapped != null ? mapped : name;
    }

    private static boolean runtimeHasMethod(String spigotOwner, String mappedName, String descriptor) {
        Class<?> runtimeOwner = runtimeClassFor(spigotOwner);
        if (runtimeOwner == null) return true;
        org.objectweb.asm.Type[] arguments;
        try {
            arguments = org.objectweb.asm.Type.getArgumentTypes(descriptor);
        } catch (RuntimeException malformedDescriptor) {
            return true;
        }
        org.objectweb.asm.Type returnType;
        try {
            returnType = org.objectweb.asm.Type.getReturnType(descriptor);
        } catch (RuntimeException malformedDescriptor) {
            return true;
        }
        String key = runtimeOwner.getName() + '#' + mappedName + '#' + descriptor;
        return "true".equals(boundedComputeIfAbsent(ALREADY_CORRECT_CACHE, key,
                ignored -> Boolean.toString(
                        declaresMatchingMethod(runtimeOwner, mappedName, arguments, returnType))));
    }

    private static boolean typeMatches(Class<?> runtime, org.objectweb.asm.Type declared) {
        boolean declaredIsReference = declared.getSort() == org.objectweb.asm.Type.OBJECT
                || declared.getSort() == org.objectweb.asm.Type.ARRAY;
        if (!declaredIsReference || runtime.isPrimitive()) {
            return org.objectweb.asm.Type.getDescriptor(runtime).equals(declared.getDescriptor());
        }
        String internal = declared.getInternalName();
        String mapped = CLASS_MAP.get(internal);
        // A descriptor already written in Mojang names - some plugins ship them - is its own
        // answer, and is recognised by the reverse table rather than assumed.
        if (mapped == null && MOJANG_TO_SPIGOT_CLASS.containsKey(internal)) mapped = internal;
        if (mapped == null) return true;
        return mapped.equals(org.objectweb.asm.Type.getInternalName(runtime));
    }

    private static boolean signatureMatches(java.lang.reflect.Method candidate,
            org.objectweb.asm.Type[] arguments, org.objectweb.asm.Type returnType) {
        Class<?>[] parameters = candidate.getParameterTypes();
        if (parameters.length != arguments.length) return false;
        for (int i = 0; i < parameters.length; i++) {
            if (!typeMatches(parameters[i], arguments[i])) return false;
        }
        return typeMatches(candidate.getReturnType(), returnType);
    }

    private static boolean declaresMatchingMethod(Class<?> runtimeOwner, String name,
            org.objectweb.asm.Type[] arguments, org.objectweb.asm.Type returnType) {
        try {
            for (Class<?> current = runtimeOwner; current != null; current = current.getSuperclass()) {
                for (java.lang.reflect.Method candidate : current.getDeclaredMethods()) {
                    if (candidate.getName().equals(name) && signatureMatches(candidate, arguments, returnType)) {
                        return true;
                    }
                }
                if (interfaceDeclaresMatchingMethod(current.getInterfaces(), name, arguments, returnType)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            return true;
        }
        return false;
    }

    private static boolean interfaceDeclaresMatchingMethod(Class<?>[] interfaces, String name,
            org.objectweb.asm.Type[] arguments, org.objectweb.asm.Type returnType) {
        for (Class<?> iface : interfaces) {
            for (java.lang.reflect.Method candidate : iface.getDeclaredMethods()) {
                if (candidate.getName().equals(name) && signatureMatches(candidate, arguments, returnType)) {
                    return true;
                }
            }
            if (interfaceDeclaresMatchingMethod(iface.getInterfaces(), name, arguments, returnType)) return true;
        }
        return false;
    }

    private String resolveInheritedBytecodeMember(String spigotOwner, String name, String descriptor, boolean method) {
        String mojangOwner = CLASS_MAP.get(spigotOwner);
        if (mojangOwner == null) return name;
        try {
            ClassLoader loader = LunarArcServer.modClassLoader();
            if (loader == null) loader = LunarArcRemapper.class.getClassLoader();
            Class<?> type = Class.forName(mojangOwner.replace('/', '.'), false, loader);
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                String currentSpigot = MOJANG_TO_SPIGOT_CLASS.getOrDefault(
                        current.getName().replace('.', '/'), current.getName().replace('.', '/'));
                Map<MemberKey, String> mappings = method ? METHOD_MAP : FIELD_MAP;
                String lookupDescriptor = toSpigotDescriptor(descriptor);
                String mapped = mappings.get(new MemberKey(currentSpigot, name, lookupDescriptor));
                if (mapped == null) mapped = mappings.get(new MemberKey(currentSpigot, name, descriptor));
                // Descriptor-blind, exactly like the two fallbacks in mapMethodName, and so subject
                // to the same failure: a name that happens to resolve on some superclass is not
                // this call's method. Validate against the class the call site names, not the one
                // this walk is standing on, because that is the class the JVM will resolve against.
                if (mapped == null) {
                    String wildcard = mappings.get(new MemberKey(currentSpigot, name, "*"));
                    if (wildcard != null && (!method || runtimeHasMethod(spigotOwner, wildcard, descriptor))) {
                        mapped = wildcard;
                    }
                }
                if (mapped == null) {
                    Map<MemberNameKey, String> names = method ? METHOD_NAME_MAP : FIELD_NAME_MAP;
                    String unique = names.get(new MemberNameKey(currentSpigot, name));
                    if (unique != null && !AMBIGUOUS.equals(unique)
                            && (!method || runtimeHasMethod(spigotOwner, unique, descriptor))) {
                        mapped = unique;
                    }
                }
                if (mapped != null) return mapped;
                if (method) {
                    String fromInterface = resolveInterfaceBytecodeMember(
                            spigotOwner, current.getInterfaces(), name, descriptor);
                    if (fromInterface != null) return fromInterface;
                }
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
        }
        return name;
    }

    private String resolveInterfaceBytecodeMember(String spigotOwner, Class<?>[] interfaces,
            String name, String descriptor) {
        for (Class<?> iface : interfaces) {
            String spigot = MOJANG_TO_SPIGOT_CLASS.getOrDefault(
                    iface.getName().replace('.', '/'), iface.getName().replace('.', '/'));
            String lookupDescriptor = toSpigotDescriptor(descriptor);
            String mapped = METHOD_MAP.get(new MemberKey(spigot, name, lookupDescriptor));
            if (mapped == null) mapped = METHOD_MAP.get(new MemberKey(spigot, name, descriptor));
            // Descriptor-blind from here down; validated against the call's own owner.
            if (mapped == null) {
                String wildcard = METHOD_MAP.get(new MemberKey(spigot, name, "*"));
                if (wildcard != null && runtimeHasMethod(spigotOwner, wildcard, descriptor)) mapped = wildcard;
            }
            if (mapped == null) {
                String unique = METHOD_NAME_MAP.get(new MemberNameKey(spigot, name));
                if (unique != null && !AMBIGUOUS.equals(unique)
                        && runtimeHasMethod(spigotOwner, unique, descriptor)) {
                    mapped = unique;
                }
            }
            if (mapped != null) return mapped;
            mapped = resolveInterfaceBytecodeMember(spigotOwner, iface.getInterfaces(), name, descriptor);
            if (mapped != null) return mapped;
        }
        return null;
    }


    public String mapRuntimeClassName(String className) {
        if (!remapNms || className == null || className.isEmpty()) return className;
        return mapClassNameString(className);
    }


    public String mapRuntimeFieldName(Class<?> runtimeOwner, String spigotName) {
        if (!remapNms || runtimeOwner == null || spigotName == null) return spigotName;
        String cacheKey = runtimeOwner.getName() + '#' + spigotName;
        return boundedComputeIfAbsent(RUNTIME_FIELD_CACHE, cacheKey,
                ignored -> resolveRuntimeMember(runtimeOwner, spigotName, false));
    }


    public String mapRuntimeMethodName(Class<?> runtimeOwner, String spigotName) {
        return mapRuntimeMethodName(runtimeOwner, spigotName, null);
    }

    public String mapRuntimeMethodName(Class<?> runtimeOwner, String spigotName, Class<?>[] parameterTypes) {
        if (!remapNms || runtimeOwner == null || spigotName == null) return spigotName;
        String parameterKey = parameterTypes == null ? "*" : runtimeParameterDescriptor(parameterTypes);
        String cacheKey = runtimeOwner.getName() + '#' + spigotName + '#' + parameterKey;
        return boundedComputeIfAbsent(RUNTIME_METHOD_CACHE, cacheKey,
                ignored -> resolveRuntimeMethod(runtimeOwner, spigotName, parameterTypes));
    }


    private static boolean namesRuntimeMember(Class<?> runtimeOwner, String name, boolean method) {
        if (runtimeOwner == null || name == null) return false;
        String key = runtimeOwner.getName() + '#' + name + '#' + (method ? 'M' : 'F');
        return "true".equals(boundedComputeIfAbsent(ALREADY_CORRECT_CACHE, key,
                ignored -> Boolean.toString(declaresMember(runtimeOwner, name, method))));
    }

    private static boolean declaresMember(Class<?> runtimeOwner, String name, boolean method) {
        try {
            for (Class<?> current = runtimeOwner; current != null; current = current.getSuperclass()) {
                if (method) {
                    for (java.lang.reflect.Method candidate : current.getDeclaredMethods()) {
                        if (candidate.getName().equals(name)) return true;
                    }
                    if (interfaceDeclaresMethod(current.getInterfaces(), name)) return true;
                } else {
                    for (java.lang.reflect.Field candidate : current.getDeclaredFields()) {
                        if (candidate.getName().equals(name)) return true;
                    }
                }
            }
            if (method) {
                // An interface has no superclass, so the walk above never reaches Object - yet
                // every interface implicitly declares its public methods, and getClass on an NBT
                // tag arrives here exactly that way.
                for (java.lang.reflect.Method candidate : Object.class.getDeclaredMethods()) {
                    if (candidate.getName().equals(name)) return true;
                }
            }
        } catch (Throwable ignored) {
            // A member whose own types are missing cannot be inspected. Treat that as "unknown"
            // rather than "absent": staying quiet is better than a warning we cannot stand behind.
            return true;
        }
        return false;
    }

    private static boolean interfaceDeclaresMethod(Class<?>[] interfaces, String name) {
        for (Class<?> iface : interfaces) {
            for (java.lang.reflect.Method candidate : iface.getDeclaredMethods()) {
                if (candidate.getName().equals(name)) return true;
            }
            if (interfaceDeclaresMethod(iface.getInterfaces(), name)) return true;
        }
        return false;
    }

    /** The loaded Minecraft class behind a Spigot owner name, or null if it cannot be resolved. */
    private static Class<?> runtimeClassFor(String spigotOwner) {
        // getOrDefault, not get: the owner may already be a Mojang name, which is one of the cases
        // this is here to recognise.
        String mojangOwner = CLASS_MAP.getOrDefault(spigotOwner, spigotOwner);
        try {
            ClassLoader loader = LunarArcServer.modClassLoader();
            if (loader == null) loader = LunarArcRemapper.class.getClassLoader();
            return Class.forName(mojangOwner.replace('/', '.'), false, loader);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static String boundedComputeIfAbsent(Map<String, String> cache, String key,
                                                  java.util.function.Function<String, String> mapping) {
        String existing = cache.get(key);
        if (existing != null) return existing;
        // Mapping/reflection inputs can originate in plugin bytecode. Keep memoization
        // bounded so reloads or pathological plugins cannot grow a process-wide cache forever.
        // Skip caching past the cap rather than clearing the whole map: under concurrent
        // plugin/classloading a full clear can race across threads and repeatedly discard
        // still-useful warm entries instead of just capping growth.
        if (cache.size() >= DYNAMIC_CACHE_LIMIT) return mapping.apply(key);
        return cache.computeIfAbsent(key, mapping);
    }

    private static boolean isNmsRuntimeClass(Class<?> runtimeOwner) {
        return runtimeOwner != null && runtimeOwner.getName().startsWith("net.minecraft.");
    }

    private String resolveRuntimeMember(Class<?> runtimeOwner, String spigotName, boolean method) {
        String resolved = lookupRuntimeMember(runtimeOwner, spigotName, method);
        if (resolved != null) return resolved;
        if (isNmsRuntimeClass(runtimeOwner) && !namesRuntimeMember(runtimeOwner, spigotName, method)) {
            LOGGER.warn("No reflective mapping found for {} {}#{} — a plugin's reflective lookup is "
                            + "likely to throw NoSuchFieldException/NoSuchMethodException.",
                    method ? "method" : "field", runtimeOwner.getName(), spigotName);
        }
        return spigotName;
    }

    private String lookupRuntimeMember(Class<?> runtimeOwner, String spigotName, boolean method) {
        for (Class<?> current = runtimeOwner; current != null; current = current.getSuperclass()) {
            String spigotOwner = MOJANG_TO_SPIGOT_CLASS.getOrDefault(current.getName().replace('.', '/'),
                    current.getName().replace('.', '/'));
            Map<MemberNameKey, String> names = method ? METHOD_NAME_MAP : FIELD_NAME_MAP;
            String unique = names.get(new MemberNameKey(spigotOwner, spigotName));
            if (unique != null && !AMBIGUOUS.equals(unique)) return unique;
            if (method) {
                for (Class<?> iface : current.getInterfaces()) {
                    String resolved = lookupRuntimeMember(iface, spigotName, true);
                    if (resolved != null) return resolved;
                }
            }
        }
        return null;
    }

    private String resolveRuntimeMethod(Class<?> runtimeOwner, String spigotName, Class<?>[] parameterTypes) {
        String parameterDescriptor = parameterTypes == null ? null : runtimeParameterDescriptor(parameterTypes);
        String resolved = lookupRuntimeMethod(runtimeOwner, spigotName, parameterTypes, parameterDescriptor);
        if (resolved != null) return resolved;
        if (isNmsRuntimeClass(runtimeOwner) && !namesRuntimeMember(runtimeOwner, spigotName, true)) {
            LOGGER.warn("No reflective mapping found for method {}#{}{} — a plugin's reflective lookup is "
                            + "likely to throw NoSuchMethodException.",
                    runtimeOwner.getName(), spigotName, parameterDescriptor == null ? "(*)" : parameterDescriptor);
        }
        return spigotName;
    }

    private String lookupRuntimeMethod(Class<?> runtimeOwner, String spigotName, Class<?>[] parameterTypes,
                                       String parameterDescriptor) {
        for (Class<?> current = runtimeOwner; current != null; current = current.getSuperclass()) {
            String spigotOwner = MOJANG_TO_SPIGOT_CLASS.getOrDefault(current.getName().replace('.', '/'),
                    current.getName().replace('.', '/'));
            String descriptorMapped = parameterDescriptor == null ? null
                    : findMethodMappingByParameters(spigotOwner, spigotName, parameterDescriptor);
            if (descriptorMapped != null && acceptsRuntimeParameters(runtimeOwner, descriptorMapped, parameterTypes)) return descriptorMapped;

            String unique = METHOD_NAME_MAP.get(new MemberNameKey(spigotOwner, spigotName));
            if (unique != null && !AMBIGUOUS.equals(unique)
                    && acceptsRuntimeParameters(runtimeOwner, unique, parameterTypes)) return unique;
            for (Class<?> iface : current.getInterfaces()) {
                String resolved = lookupRuntimeMethod(iface, spigotName, parameterTypes, parameterDescriptor);
                if (resolved != null) return resolved;
            }
        }
        return null;
    }

    private static boolean acceptsRuntimeParameters(Class<?> owner, String name, Class<?>[] parameters) {
        if (parameters == null) return true;
        try {
            owner.getMethod(name, parameters);
            return true;
        } catch (NoSuchMethodException ignored) {
            for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
                try {
                    current.getDeclaredMethod(name, parameters);
                    return true;
                } catch (NoSuchMethodException missing) {
                }
            }
            return false;
        }
    }

    private static String findMethodMappingByParameters(String spigotOwner, String spigotName,
                                                         String parameterDescriptor) {
        List<Map.Entry<MemberKey, String>> overloads =
                METHOD_OVERLOAD_INDEX.get(new MemberNameKey(spigotOwner, spigotName));
        if (overloads == null || overloads.isEmpty()) return null;
        String resolved = null;
        for (Map.Entry<MemberKey, String> entry : overloads) {
            String descriptor = entry.getKey().descriptor();
            if ("*".equals(descriptor)) {
                if (resolved == null) resolved = entry.getValue();
                else if (!resolved.equals(entry.getValue())) return null;
                continue;
            }
            int close = descriptor.indexOf(')');
            if (close < 0 || !descriptor.substring(0, close + 1).equals(parameterDescriptor)) continue;
            if (resolved == null) resolved = entry.getValue();
            else if (!resolved.equals(entry.getValue())) return null;
        }
        return resolved;
    }

    private static String runtimeParameterDescriptor(Class<?>[] parameterTypes) {
        StringBuilder descriptor = new StringBuilder("(");
        for (Class<?> parameterType : parameterTypes) descriptor.append(runtimeTypeDescriptor(parameterType));
        return descriptor.append(')').toString();
    }

    private static String runtimeTypeDescriptor(Class<?> type) {
        if (type.isArray()) return type.getName().replace('.', '/');
        if (type.isPrimitive()) {
            if (type == void.class) return "V";
            if (type == boolean.class) return "Z";
            if (type == byte.class) return "B";
            if (type == char.class) return "C";
            if (type == short.class) return "S";
            if (type == int.class) return "I";
            if (type == long.class) return "J";
            if (type == float.class) return "F";
            if (type == double.class) return "D";
        }
        String internal = type.getName().replace('.', '/');
        String spigot = MOJANG_TO_SPIGOT_CLASS.getOrDefault(internal, internal);
        return 'L' + spigot + ';';
    }

    private String toSpigotOwner(String owner) {
        String normalized = owner;
        if (CLASS_MAP.containsKey(normalized)) return normalized;
        return MOJANG_TO_SPIGOT_CLASS.getOrDefault(normalized, normalized);
    }

    private static String toSpigotDescriptor(String descriptor) {
        return mapDescriptorClasses(descriptor, MOJANG_TO_SPIGOT_CLASS);
    }

    public byte[] transform(byte[] bytecode, String className) {
        return transformInternal(bytecode, className == null ? "<unknown>" : className);
    }

    public byte[] transform(byte[] bytecode) {
        return transformInternal(bytecode, "<unknown>");
    }

    private byte[] transformInternal(byte[] bytecode, String className) {
        if (bytecode == null || bytecode.length < 8) return bytecode;
        try {
            ClassReader reader = new ClassReader(bytecode);
            ClassWriter writer = new ClassWriter(0);

            boolean classNeedsNms = remapNms && containsNmsReference(bytecode);
            LunarArcRemapper effective = classNeedsNms || !remapNms
                    ? this
                    : new LunarArcRemapper(false);

            ClassVisitor remapper = new ClassRemapper(writer, effective);
            ClassVisitor visitor = remapNms ? new ReflectionMemberVisitor(remapper) : remapper;
            if (io.lunararcdevs.lunararc.common.LunarArcDebug.REMAP) {
                io.lunararcdevs.lunararc.common.LunarArcDebug.remap(
                        "{}: nmsSymbols={} reflectionBridge={}", className, classNeedsNms, remapNms);
            }
            visitor = effective.compatibilityVisitor(visitor, className);
            reader.accept(visitor, 0);
            byte[] remapped = writer.toByteArray();
            return applyPluginPatchers(remapped, className);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to remap plugin class " + className, e);
        }
    }

    private static final java.util.List<io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.PluginPatcher> PLUGIN_PATCHERS =
            io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.LunarArcPluginPatcherLoader.load();

    private static byte[] applyPluginPatchers(byte[] remapped, String className) {
        if (PLUGIN_PATCHERS.isEmpty()) return remapped;
        org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
        new ClassReader(remapped).accept(node, 0);
        boolean matched = false;
        for (io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.PluginPatcher patcher : PLUGIN_PATCHERS) {
            try {
                patcher.handleClass(node, io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.LunarArcGlobalClassRepo.INSTANCE);
                matched = true;
            } catch (RuntimeException e) {
                LOGGER.warn("Plugin patcher {} failed on class {}", patcher.getClass().getName(), className, e);
            }
        }
        if (!matched) return remapped;
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static boolean containsNmsReference(byte[] bytecode) {
        // Class-file constant-pool UTF-8 entries are stored as raw modified-UTF bytes.
        // These ASCII needles therefore safely identify ordinary NMS descriptors,
        // owners, class literals, and reflective class-name strings without parsing
        // or rewriting the class a first time.
        return containsAscii(bytecode, "net/minecraft/")
                || containsAscii(bytecode, "net.minecraft.");
    }

    private static boolean containsAscii(byte[] haystack, String needle) {
        byte[] target = needle.getBytes(StandardCharsets.US_ASCII);
        outer:
        for (int i = 0; i <= haystack.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (haystack[i + j] != target[j]) continue outer;
            }
            return true;
        }
        return false;
    }

    private ClassVisitor compatibilityVisitor(ClassVisitor delegate, String className) {
        return new ClassVisitor(Opcodes.ASM9, delegate) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, method) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
                        // Generic compatibility for the legacy Spigot static accessor. The
                        // modloader still owns the real MinecraftServer; this only redirects
                        // old plugin bytecode to LunarArc's concrete server access method.
                        if (opcode == Opcodes.INVOKESTATIC
                                && "net/minecraft/server/MinecraftServer".equals(owner)
                                && "getServer".equals(methodName)
                                && "()Lnet/minecraft/server/MinecraftServer;".equals(methodDescriptor)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "io/lunararcdevs/lunararc/common/LunarArcServerAccess",
                                    "getMinecraftServer", methodDescriptor, false);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                    }
                };
            }
        };
    }

    private String mapClassNameString(String value) {
        if (value == null || value.isEmpty()) return value;
        boolean binaryName = value.indexOf('.') >= 0 && value.indexOf('/') < 0;
        String internal = binaryName ? value.replace('.', '/') : value;
        if (!internal.startsWith(CRAFTBUKKIT_PREFIX) && !internal.startsWith("net/minecraft/")) return value;
        String mapped = map(internal);
        return mapped.equals(internal) ? value : (binaryName ? mapped.replace('/', '.') : mapped);
    }

    private final class ReflectionMemberVisitor extends ClassVisitor {
        private ReflectionMemberVisitor(ClassVisitor delegate) {
            super(Opcodes.ASM9, delegate);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM9, delegate) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
                    String bridgeOwner = "io/lunararcdevs/lunararc/common/mod/LunarArcReflectionBridge";
                    if (opcode == Opcodes.INVOKESTATIC && "java/lang/Class".equals(owner) && "forName".equals(methodName)) {
                        if ("(Ljava/lang/String;)Ljava/lang/Class;".equals(methodDescriptor)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "forName", methodDescriptor, false);
                            return;
                        }
                        if ("(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;".equals(methodDescriptor)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "forName", methodDescriptor, false);
                            return;
                        }
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL && "java/lang/ClassLoader".equals(owner)
                            && "loadClass".equals(methodName)
                            && "(Ljava/lang/String;)Ljava/lang/Class;".equals(methodDescriptor)) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "loadClass",
                                "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", false);
                        return;
                    }
                    if ("java/lang/Class".equals(owner)) {
                        switch (methodName) {
                            case "getField" -> {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "getField",
                                        "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
                                return;
                            }
                            case "getDeclaredField" -> {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "getDeclaredField",
                                        "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
                                return;
                            }
                            case "getMethod" -> {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "getMethod",
                                        "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
                                return;
                            }
                            case "getDeclaredMethod" -> {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, bridgeOwner, "getDeclaredMethod",
                                        "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
                                return;
                            }
                            default -> {
                            }
                        }
                    }
                    super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                }
            };
        }
    }

    private static String mapDescriptorClasses(String descriptor, Map<String, String> classes) {
        if (descriptor == null || descriptor.isEmpty() || "*".equals(descriptor)) return descriptor;
        StringBuilder out = new StringBuilder(descriptor.length());
        for (int i = 0; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            out.append(c);
            if (c == 'L') {
                int end = descriptor.indexOf(';', i);
                if (end < 0) break;
                String name = descriptor.substring(i + 1, end);
                out.append(classes.getOrDefault(name, name));
                out.append(';');
                i = end;
            }
        }
        return out.toString();
    }

    private record MemberKey(String owner, String name, String descriptor) {
    }

    private record MemberNameKey(String owner, String name) {
    }

    private record PendingMember(boolean method, String spigotOwner, String spigotName,
                                 String mojangDescriptor, String mojangName) {
    }
}
