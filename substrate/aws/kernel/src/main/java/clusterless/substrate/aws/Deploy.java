package clusterless.substrate.aws;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "deploy"
)
public class Deploy extends Manage implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("deploy");
        return 0;
    }
}
