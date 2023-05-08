package clusterless.substrate.aws.resources;

import clusterless.naming.ExportRef;
import clusterless.naming.Stage;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.constructs.Construct;

import java.util.Objects;

public class Vpcs {

    public static final String COMMON_VPC = "CommonVpc";
    public static final String VPC = "vpc";

    public static String bootstrapVPCName(Stage stage) {
        Objects.requireNonNull(stage, "stage is null");

        return stage.asLower()
                .with(COMMON_VPC)
                .lowerHyphen();
    }

    public static String bootstrapVpcIdRef(@NotNull Construct scope) {
        return importValue(scope, ExportRef.idRef());
    }

    public static String bootstrapVpcArnRef(@NotNull Construct scope) {
        return importValue(scope, ExportRef.arnRef());
    }

    @NotNull
    private static String importValue(@NotNull Construct scope, ExportRef qualified) {
        ExportRef ref = ClsBootstrap.bootstrapBase(scope, qualified)
                .withResourceType(VPC)
                .withResourceName(COMMON_VPC);

        return Fn.importValue(ref.exportName());
    }
}
