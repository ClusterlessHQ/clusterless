package clusterless.substrate.aws.resources;

import clusterless.naming.Ref;
import clusterless.substrate.aws.managed.StagedApp;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

public class ClsBootstrap {
    public static final String BOOTSTRAP = "bootstrap";
    public static final String BOOTSTRAP_VERSION = "1";

    @NotNull
    public static Ref bootstrapBase(@NotNull Construct scope, Ref qualified) {
        return qualified.withProvider("aws")
                .withStage(StagedApp.stagedOf(scope).stage())
                .withScope(BOOTSTRAP)
                .withScopeVersion(BOOTSTRAP_VERSION)
                .withResourceNs("meta");
    }
}
