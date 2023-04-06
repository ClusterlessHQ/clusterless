package clusterless.substrate.aws.store;

import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Region;
import clusterless.util.Label;

import java.util.Objects;

public class Stores {

    public static String bootstrapStoreName(StateStore stateStore, Placement placement) {
        return bootstrapStoreName(stateStore, placement.account(), Region.of(placement.region()), placement.stage());
    }

    public static String bootstrapStoreName(StateStore stateStore, String account, Region region, String stage) {
        Objects.requireNonNull(stateStore, "stateStore is null");
        Objects.requireNonNull(account, "account is null");
        Objects.requireNonNull(region, "region is null");

        return Label.of(stage)
                .with("Clusterless")
                .with(stateStore)
                .with(account)
                .with(region)
                .lowerHyphen();
    }
}
