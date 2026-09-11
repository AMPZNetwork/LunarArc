package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.lang.reflect.Field;

public final class MavenLibraryResolverRegressionTest {
    public static void run() throws Exception {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        Field sessionField = MavenLibraryResolver.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        DefaultRepositorySystemSession session = (DefaultRepositorySystemSession) sessionField.get(resolver);
        DependencySelector selector = session.getDependencySelector();

        Dependency brokenErrorProne = new Dependency(
                new DefaultArtifact("com.google.errorprone:error_prone_annotations:1.0"), JavaScopes.COMPILE);
        check(!selector.selectDependency(brokenErrorProne),
                "com.google.errorprone:error_prone_annotations must stay excluded - a real boot proved "
                        + "log4j-api:2.25.1's own published POM declares this with an unresolved "
                        + "${error-prone.version} placeholder that 404s against every real repository, "
                        + "aborting collection for any plugin library that transitively depends on log4j-api");

        Dependency normalArtifact = new Dependency(
                new DefaultArtifact("com.zaxxer:HikariCP:7.0.2"), JavaScopes.COMPILE);
        check(selector.selectDependency(normalArtifact),
                "the errorprone exclusion must not reject unrelated artifacts");

        Dependency slf4jApi = new Dependency(
                new DefaultArtifact("org.slf4j:slf4j-api:2.0.9"), JavaScopes.COMPILE);
        check(!selector.selectDependency(slf4jApi),
                "org.slf4j:slf4j-api must stay excluded - a real boot proved LuckPerms/ClearLaggEnhanced's "
                        + "bundled HikariCP and WorldGuard/CoreProtect's direct sqlite-jdbc use each pull "
                        + "slf4j-api in transitively, handing every such plugin a second, freshly-loaded copy "
                        + "alongside the platform's own boot module org.slf4j@2.0.9 - two different Class "
                        + "objects for the same type, java.lang.LinkageError: loader constraint violation, "
                        + "the instant a class from one copy called an interface method whose resolution "
                        + "landed on the other");

        System.out.println("MavenLibraryResolver regressions passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
