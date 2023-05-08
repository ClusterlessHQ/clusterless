package clusterless.substrate.aws.bootstrap.vpc;

import clusterless.substrate.aws.managed.Managed;
import clusterless.substrate.aws.resources.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.Collections;

public class VPCConstruct extends Construct implements Managed {
    private static final Logger LOG = LogManager.getLogger(VPCConstruct.class);

    private final Vpc vpc;
    private final String subnetName;
    private String cidrBlock = "10.14.0.0/16";
    private int cidrMask = 19; // 8k hosts per subnet

    public VPCConstruct(@NotNull Construct scope) {
        super(scope, "BootstrapVpc");

        subnetName = Resources.regionallyUniqueName(this, "ClusterlessSubnet");

        LOG.info("creating vpc with subnet: {}, cidrBlock: {}, cidrMask: {}", subnetName, cidrBlock, cidrMask);

        vpc = Vpc.Builder.create(this, "Vpc")
                .ipAddresses(IpAddresses.cidr(cidrBlock))
                .maxAzs(99) // use all AZs, some regions have more than 3
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .subnetConfiguration(Collections.singletonList(
                        SubnetConfiguration.builder()
                                .name(subnetName)
                                // PUBLIC grants access to the internet, need to confirm Batch still requires this to work
                                // PRIVATE_WITH_EGRESS will create a NAT gateway which has a charge
                                .subnetType(SubnetType.PUBLIC)
                                .mapPublicIpOnLaunch(false)
                                .cidrMask(cidrMask)
                                .build()
                ))
                .build();
    }

    public Vpc vpc() {
        return vpc;
    }

    public String vpcArn() {
        return vpc.getVpcArn();
    }

    public String vpcId() {
        return vpc.getVpcId();
    }

    public String subnetName() {
        return subnetName;
    }
}
