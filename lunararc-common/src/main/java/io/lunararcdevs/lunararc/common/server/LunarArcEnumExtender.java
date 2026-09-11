package io.lunararcdevs.lunararc.common.server;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class LunarArcEnumExtender {
    private static final Unsafe UNSAFE = findUnsafe();
    private static final long CLASS_ENUM_CONSTANTS = classFieldOffset("enumConstants");
    private static final long CLASS_ENUM_DIRECTORY = classFieldOffset("enumConstantDirectory");

    private LunarArcEnumExtender() {}

    static <E extends Enum<E>> E construct(
            Class<E> enumType, String name, int ordinal,
            Class<?>[] userParameterTypes, Object[] userArguments) throws Throwable {
        if (userParameterTypes.length != userArguments.length) {
            throw new IllegalArgumentException("Enum constructor parameter/argument length mismatch");
        }
        Class<?>[] parameterTypes = new Class<?>[userParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(userParameterTypes, 0, parameterTypes, 2, userParameterTypes.length);

        List<Object> arguments = new ArrayList<>(userArguments.length + 2);
        arguments.add(name);
        arguments.add(ordinal);
        arguments.addAll(Arrays.asList(userArguments));

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(enumType, MethodHandles.lookup());
        MethodHandle constructor = lookup.findConstructor(
                enumType, MethodType.methodType(void.class, parameterTypes));
        @SuppressWarnings("unchecked")
        E value = (E) constructor.invokeWithArguments(arguments);
        return value;
    }

    static <E extends Enum<E>> void append(Class<E> enumType, List<E> additions) {
        if (additions.isEmpty()) return;
        E[] oldValues = enumType.getEnumConstants();
        E[] combined = Arrays.copyOf(oldValues, oldValues.length + additions.size());
        for (int i = 0; i < additions.size(); i++) {
            combined[oldValues.length + i] = additions.get(i);
        }

        Field valuesField = Arrays.stream(enumType.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType().isArray() && field.getType().getComponentType() == enumType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Enum values field not found for " + enumType.getName()));
        Object base = UNSAFE.staticFieldBase(valuesField);
        long offset = UNSAFE.staticFieldOffset(valuesField);
        UNSAFE.putObjectVolatile(base, offset, combined);

        // A JVM class cannot gain new java.lang.reflect.Field objects after it has
        // been defined. Keep Class#getEnumConstants() pinned to the original, real
        // field-backed constants so reflection libraries (notably Gson's enum adapter)
        // never enumerate a dynamic value and then fail on Class#getField(name).
        // Material.values() still reads the replaced $VALUES array above.
        UNSAFE.putObjectVolatile(enumType, CLASS_ENUM_CONSTANTS, oldValues.clone());

        // Enum.valueOf() uses Class#enumConstantDirectory rather than Material.values().
        // Populate that directory explicitly with both original and dynamic values so
        // Bukkit/plugin valueOf semantics remain useful for loader-added materials.
        java.util.Map<String, E> directory = new java.util.HashMap<>(combined.length * 2);
        for (E value : combined) directory.put(value.name(), value);
        UNSAFE.putObjectVolatile(enumType, CLASS_ENUM_DIRECTORY, directory);
    }

    static void setObject(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) return;
        long offset = UNSAFE.objectFieldOffset(field);
        UNSAFE.putObject(target, offset, value);
    }

    static void setBoolean(Object target, String fieldName, boolean value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null || field.getType() != boolean.class) return;
        UNSAFE.putBoolean(target, UNSAFE.objectFieldOffset(field), value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void putStaticMap(Class<?> owner, String fieldName, Object key, Object value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            Object base = UNSAFE.staticFieldBase(field);
            Object map = UNSAFE.getObject(base, UNSAFE.staticFieldOffset(field));
            if (map instanceof java.util.Map target) target.put(key, value);
        } catch (NoSuchFieldException ignored) {
            // Paper/Bukkit revisions may not expose every legacy lookup map.
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
            try {
                return cursor.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    private static long classFieldOffset(String name) {
        try {
            return UNSAFE.objectFieldOffset(Class.class.getDeclaredField(name));
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static Unsafe findUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
