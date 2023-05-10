package clusterless.substrate.aws.construct;

import clusterless.naming.Label;
import clusterless.naming.Ref;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.constructs.Construct;

public class OutputConstruct extends Construct {
    private static final Logger LOG = LogManager.getLogger(OutputConstruct.class);
    private final String exportName;

    public OutputConstruct(@NotNull Construct construct, Ref ref, String value, String description) {
        super(construct, Label.of("Output").with(ref.resourceLabel()).with(ref.qualifier()).camelCase());

        exportName = createOutputFor(ref, value, description);
    }

    public String exportName() {
        return exportName;
    }

    protected String createOutputFor(Ref ref, String value, String description) {
        String exportName = ref.exportName();

        LOG.info("creating output for: {}", exportName);

        new CfnOutput(this, ref.resourceLabel().camelCase(), new CfnOutputProps.Builder()
                .exportName(exportName)
                .value(value)
                .description(description)
                .build());

        return exportName;
    }
}
