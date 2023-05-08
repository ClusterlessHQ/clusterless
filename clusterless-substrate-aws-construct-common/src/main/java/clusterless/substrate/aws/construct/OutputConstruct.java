package clusterless.substrate.aws.construct;

import clusterless.naming.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.constructs.Construct;

public class OutputConstruct extends Construct {
    private static final Logger LOG = LogManager.getLogger(OutputConstruct.class);
    private final String exportName;

    public OutputConstruct(@NotNull Construct construct, Label name, String value, String description) {
        super(construct, Label.of("Output").with(name).camelCase());

        exportName = createOutputFor(name, value, description);
    }

    public String exportName() {
        return exportName;
    }

    protected String createOutputFor(Label name, String value, String description) {
        String exportName = name.lowerColonPath();

        LOG.info("creating output for: {}", exportName);

        new CfnOutput(this, name.camelCase(), new CfnOutputProps.Builder()
                .exportName(exportName)
                .value(value)
                .description(description)
                .build());

        return exportName;
    }
}
