package clusterless.substrate.aws;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "verify"
)
public class Verify extends Manage implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("verify");
        return 0;
    }
}
