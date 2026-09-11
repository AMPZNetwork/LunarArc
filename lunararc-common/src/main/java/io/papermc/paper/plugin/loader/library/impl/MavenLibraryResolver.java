package io.papermc.paper.plugin.loader.library.impl;

import io.papermc.paper.plugin.loader.library.ClassPathLibrary;
import io.papermc.paper.plugin.loader.library.LibraryLoadingException;
import io.papermc.paper.plugin.loader.library.LibraryStore;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.internal.impl.synccontext.named.NameMapper;
import org.eclipse.aether.internal.impl.synccontext.named.NameMappers;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MavenLibraryResolver implements ClassPathLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenLibraryResolver.class);
    public static final String MAVEN_CENTRAL_DEFAULT_MIRROR = defaultCentral();
    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> repositories = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();

    public MavenLibraryResolver() {
        this.repository = new RepositorySystemSupplier() {
            @Override
            protected Map<String, NameMapper> getNameMappers() {


                HashMap<String, NameMapper> result = new HashMap<>();
                result.put(NameMappers.STATIC_NAME, NameMappers.staticNameMapper());
                result.put(NameMappers.GAV_NAME, NameMappers.gavNameMapper());
                result.put(NameMappers.FILE_GAV_NAME, NameMappers.fileGavNameMapper());
                result.put(NameMappers.FILE_HGAV_NAME, NameMappers.fileHashingGavNameMapper());
                return result;
            }
        }.get();
        if (this.repository == null) throw new IllegalStateException("Maven Resolver did not create a RepositorySystem");
        this.session = MavenRepositorySystemUtils.newSession();
        this.session.setSystemProperties(System.getProperties());
        this.session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        // Neither Aether nor the HttpTransporter it hands requests to default to any timeout on
        // their own - a plugin declaring a library against a repository that is slow, offline, or
        // simply never replies (dead mirror, a firewall dropping packets rather than resetting the
        // connection) blocks this call forever, and since it runs on the main thread during
        // PluginClassLoader construction, that means the whole server, with no error and no way to
        // recover short of killing the process. These are the same config keys real Aether-based
        // resolvers (including Maven itself) use for exactly this.
        this.session.setConfigProperty(ConfigurationProperties.CONNECT_TIMEOUT, 10_000);
        this.session.setConfigProperty(ConfigurationProperties.REQUEST_TIMEOUT, 30_000);
        // MavenRepositorySystemUtils.newSession() only excludes scope=test/provided EDGES from the
        // final dependency graph - it does not skip reading those artifacts' own POMs, and reading
        // an artifact's POM requires fully resolving its <parent> chain and every <dependencyManagement>
        // import-scope BOM it declares, regardless of scope, since there is no way to know a BOM is
        // unused without reading it. A widely-used library with a big multi-module parent POM (real
        // org.slf4j:slf4j-parent, observed directly) can pull in dozens of unrelated BOMs
        // (junit-bom, mockito-bom, spring-framework-bom...) this way. Aether's default collector
        // ("df") resolves these one at a time; the built-in "bf" collector resolves independent
        // siblings concurrently (real, documented Aether behavior - confirmed directly against
        // org.eclipse.aether.internal.impl.collect.bf.BfDependencyCollector's real source, not
        // guessed), which is the actual fix for the slow-but-not-actually-stuck case the 45s
        // timeout above exists to contain.
        this.session.setConfigProperty("aether.dependencyCollector.impl", "bf");
        this.session.setConfigProperty("aether.dependencyCollector.bf.threads", 8);
        // A real boot proved log4j-api:2.25.1's own published POM (a common transitive of plugin
        // libraries like HikariCP) declares a dependency on com.google.errorprone:error_prone_annotations
        // using an unresolved ${error-prone.version} property - that property only exists inside
        // log4j's own multi-module reactor build, never in the standalone consumer POM a plugin
        // resolver reads, so fetching this artifact's descriptor always 404s and aborts the whole
        // collection (confirmed: BfDependencyCollector.filter() checks the dependency selector before
        // ever attempting the descriptor fetch, so excluding it here skips the broken lookup entirely
        // rather than just failing later). error_prone_annotations is Google's static-analysis
        // annotation library - compile-time-only, never needed at runtime - so excluding it
        // unconditionally cannot remove anything a plugin actually uses.
        this.session.setDependencySelector(new AndDependencySelector(
                new ScopeDependencySelector("test", "provided"),
                new OptionalDependencySelector(),
                new ExclusionDependencySelector(List.of(
                        // Exclusion's own constructor turns a null classifier/extension into "" (an
                        // exact-match empty string), not a wildcard - matches(String, String) only
                        // treats the literal "*" as "any value". A real jar artifact's extension is
                        // "jar", never "", so passing null here would build an exclusion that can
                        // never match any real artifact at all - caught by
                        // MavenLibraryResolverRegressionTest actually exercising selectDependency()
                        // against a synthetic jar artifact rather than assuming the constructor
                        // treated null as "any".
                        new Exclusion("com.google.errorprone", "error_prone_annotations", "*", "*"),
                        // A real boot proved this the hard way: LuckPerms/ClearLaggEnhanced's bundled
                        // HikariCP and WorldGuard/CoreProtect's direct sqlite-jdbc use both declare
                        // org.slf4j:slf4j-api as a transitive dependency, so this resolver was handing
                        // each of those plugins a *second*, freshly-loaded copy of slf4j-api inside
                        // their own TransformingPluginLibraryClassLoader - alongside the slf4j-api the
                        // platform itself already ships as the boot module org.slf4j@2.0.9 (confirmed
                        // directly in the crash: "org.slf4j.ILoggerFactory is in module org.slf4j@2.0.9
                        // of loader 'MC-BOOTSTRAP'"). The plugin's own copy defines LoggerFactory, but
                        // LoggerFactory.getLogger's interface-method resolution for ILoggerFactory
                        // still lands on the platform's copy through normal parent delegation - two
                        // different Class objects for the same type, java.lang.LinkageError: loader
                        // constraint violation, on every plugin that pulls slf4j-api in transitively.
                        // Excluding it here means every plugin instead resolves org.slf4j.* through
                        // normal parent delegation to the one copy the platform already provides -
                        // the same copy ILoggerFactory was already resolving to anyway.
                        new Exclusion("org.slf4j", "slf4j-api", "*", "*")))));
        this.session.setLocalRepositoryManager(this.repository.newLocalRepositoryManager(this.session, new LocalRepository("libraries")));
    }
    public void addDependency(Dependency dependency) { dependencies.add(dependency); }
    public void addRepository(RemoteRepository repository) { repositories.add(repository); }
    @Override public void register(LibraryStore store) throws LibraryLoadingException {
        try {
            List<RemoteRepository> repos = repository.newResolutionRepositories(session, repositories);
            LOGGER.info("Collecting dependency tree for {} declared librar{} (POM/parent/BOM traversal)...",
                    dependencies.size(), dependencies.size() == 1 ? "y" : "ies");
            long collectStart = System.nanoTime();
            CollectResult collected;
            try {
                collected = repository.collectDependencies(session,
                        new CollectRequest((Dependency) null, dependencies, repos));
            } catch (DependencyCollectionException ex) {
                throw new LibraryLoadingException("Error collecting library dependency tree", ex);
            }
            long collectMillis = (System.nanoTime() - collectStart) / 1_000_000;
            LOGGER.info("Collected dependency tree in {}ms, downloading artifacts...", collectMillis);
            long resolveStart = System.nanoTime();
            DependencyResult result = repository.resolveDependencies(session,
                    new DependencyRequest(collected.getRoot(), null));
            long resolveMillis = (System.nanoTime() - resolveStart) / 1_000_000;
            LOGGER.info("Downloaded artifacts in {}ms", resolveMillis);
            for (ArtifactResult artifact : result.getArtifactResults()) {
                File file = artifact.getArtifact().getFile();
                if (file != null) store.addLibrary(file.toPath());
            }
        } catch (DependencyResolutionException ex) {
            throw new LibraryLoadingException("Error resolving libraries", ex);
        } finally {
            // RepositorySystemSupplier owns named-lock/lifecycle resources. Paper's
            // classpath builder resolves each library set once, so release them as soon
            // as the concrete library paths have been produced.
            repository.shutdown();
        }
    }
    private static String defaultCentral() {
        String central = System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY");
        if (central == null) central = System.getProperty("org.bukkit.plugin.java.LibraryLoader.centralURL");
        return central != null ? central : "https://maven-central.storage-download.googleapis.com/maven2";
    }
}
