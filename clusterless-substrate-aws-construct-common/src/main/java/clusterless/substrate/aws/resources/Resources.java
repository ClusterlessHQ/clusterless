package clusterless.substrate.aws.resources;

import clusterless.naming.Label;
import clusterless.naming.Region;
import clusterless.naming.Stage;
import clusterless.naming.Version;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Objects;

public class Resources {
    public static String globallyUniqueName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();

        return stage.upperOnly()
                .with(name)
                .with(region)
                .lowerHyphen();
    }

    public static String globallyUniqueProjectName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        Label version = Version.of(ManagedProject.projectOf(scope).version());

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(region)
                .lowerHyphen();
    }

    public static String regionallyUniqueName(Construct scope, String name) {
        return regionallyUniqueLabel(scope, Label.of(name), null).lowerHyphen();
    }

    public static Label regionallyUniqueLabel(Construct scope, Label name, Label qualifier) {
        Objects.requireNonNull(name, "name may not be null");

        Stage stage = StagedApp.stagedOf(scope).stage();

        return stage.upperOnly()
                .with(name)
                .with(qualifier);
    }

    public static String regionallyUniqueProjectName(Construct scope, String name) {
        Label label = regionallyUniqueProjectLabel(scope, Label.of(name));

        return label
                .lowerHyphen();
    }

    public static Label regionallyUniqueProjectLabel(Construct scope, Label name) {
        return regionallyUniqueProjectLabel(scope, name, null);
    }

    public static Label regionallyUniqueProjectLabel(Construct scope, Label name, Label qualifier) {
        Objects.requireNonNull(name, "name may not be null");

        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        Label version = Version.of(ManagedProject.projectOf(scope).version());

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(qualifier);
    }
}
