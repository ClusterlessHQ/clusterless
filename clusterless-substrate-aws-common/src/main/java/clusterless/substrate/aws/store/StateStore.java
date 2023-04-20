package clusterless.substrate.aws.store;

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

    public Label storeNameKey() {
        return this.with("StoreName");
    }
}
