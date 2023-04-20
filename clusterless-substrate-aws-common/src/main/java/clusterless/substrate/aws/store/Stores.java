package clusterless.substrate.aws.store;

import clusterless.model.deploy.Placement;
import clusterless.naming.Region;
import clusterless.naming.Stage;

import java.util.Objects;

public class Stores {

    public static String bootstrapStoreName(StateStore stateStore, Placement placement) {
        return bootstrapStoreName(stateStore, placement.account(), Region.of(placement.region()), Stage.of(placement.stage()));
    }

    public static String bootstrapStoreName(StateStore stateStore, String account, Region region, Stage stage) {
        Objects.requireNonNull(stateStore, "stateStore is null");
        Objects.requireNonNull(account, "account is null");
        Objects.requireNonNull(region, "region is null");

        return stage.asLower()
                .with("Clusterless")
                .with(stateStore)
                .with(account)
                .with(region)
                .lowerHyphen();
    }
}
