package clusterless.substrate.aws.resources;

import clusterless.naming.ExportRef;
import clusterless.substrate.aws.managed.StagedApp;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

public class ClsBootstrap {
    public static final String BOOTSTRAP = "bootstrap";
    public static final String BOOTSTRAP_VERSION = "1";

    public static ExportRef bootstrapBase(@NotNull Construct scope) {
        return bootstrapBase(scope, ExportRef.ref());
    }

    @NotNull
    public static ExportRef bootstrapBase(@NotNull Construct scope, ExportRef qualifed) {
        return qualifed.withProvider("aws")
                .withStage(StagedApp.stagedOf(scope).stage())
                .withScope(BOOTSTRAP)
                .withScopeVersion(BOOTSTRAP_VERSION);
    }
}
