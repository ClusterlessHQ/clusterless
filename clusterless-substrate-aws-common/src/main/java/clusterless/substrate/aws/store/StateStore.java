package clusterless.substrate.aws.store;

import clusterless.naming.Fixed;
import clusterless.naming.Label;

public enum StateStore implements Label {
    Meta("Metadata"),
    Arc("ArcState"),
    Manifest("Manifest");

    final String value;

    StateStore(String value) {
        this.value = value;
    }

    @Override
    public String camelCase() {
        return value;
    }

    public Fixed typeKey() {
        return Fixed.of(lowerCamelCase());
    }

    public Label storeKey() {
        return Label.of(this.with("Store").camelCase());
    }
}
