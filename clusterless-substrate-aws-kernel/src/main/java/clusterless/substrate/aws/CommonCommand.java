package clusterless.substrate.aws;

import clusterless.config.CommonConfig;
import clusterless.substrate.aws.cdk.CDK;
import picocli.CommandLine;

public class CommonCommand {
    @CommandLine.ParentCommand
    protected Kernel kernel;

    protected CommonConfig getCommonConfig() {
        return kernel.configurations().get("common");
    }

    protected AwsConfig getProviderConfig() {
        return kernel.configurations().get(CDK.PROVIDER);
    }
}
