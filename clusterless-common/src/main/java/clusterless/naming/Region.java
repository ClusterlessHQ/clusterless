package clusterless.naming;

import java.util.Objects;

public class Region extends Fixed {
    public static Region of(String region) {
        Objects.requireNonNull(region, "region may not be null");
        return new Region(region);
    }

    protected Region(String value) {
        super(value);
    }
}
