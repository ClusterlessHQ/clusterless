package clusterless.substrate.aws.cdk.bootstrap;

import clusterless.substrate.aws.resources.DeployMeta;

public class BootstrapMeta extends DeployMeta {
    String version;

    public String version() {
        return version;
    }

    public BootstrapMeta setVersion(String version) {
        this.version = version;
        return this;
    }
}
