package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

public class StateURIs {
    public static ManifestURI manifestPath(@NotNull ManagedConstruct managedConstruct, ManifestState state, Dataset dataset) {
        Placement placement = placementFor(managedConstruct);
        return ManifestURI.builder()
                .withPlacement(placement)
                .withDataset(dataset)
                .withState(state)
                .build();
    }

    @NotNull
    public static Project projectFor(@NotNull ManagedConstruct managedConstruct) {
        ManagedProject managedProject = ManagedProject.projectOf(managedConstruct);

        return Project.Builder.builder()
                .withName(managedProject.name().lowerHyphen())
                .withVersion(managedProject.version())
                .build();
    }

    @NotNull
    public static Placement placementFor(@NotNull Construct scope) {
        String account = Stack.of(scope).getAccount();
        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();

        return Placement.Builder.builder()
                .withAccount(account)
                .withRegion(region)
                .withStage(stage.lowerHyphen())
                .build();
    }
}
