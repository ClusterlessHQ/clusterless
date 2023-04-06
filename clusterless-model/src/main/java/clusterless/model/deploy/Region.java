package clusterless.model.deploy;

import clusterless.util.Label;
import clusterless.util.Strings;

public class Region implements Label {

    private final String region;

    public static Region of(String region) {
        return new Region(region);
    }

    protected Region(String region) {
        this.region = region;
    }

    @Override
    public String camelCase() {
        return Strings.lowerHyphenToUpperCamel(region);
    }

    @Override
    public String lowerHyphen() {
        return region;
    }
}
