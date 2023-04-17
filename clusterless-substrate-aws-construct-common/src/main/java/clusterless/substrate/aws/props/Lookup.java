package clusterless.substrate.aws.props;

import software.amazon.awscdk.services.lambda.Architecture;

public class Lookup {
    public static Architecture architecture(LambdaJavaRuntimeProps.Architecture architecture) {
        switch (architecture) {
            case ARM_64: {
                return Architecture.ARM_64;
            }
            case X86_64: {
                return Architecture.X86_64;
            }
        }
        throw new IllegalStateException("unknown architecture: " + architecture);
    }
}
