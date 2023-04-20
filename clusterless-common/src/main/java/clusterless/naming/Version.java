package clusterless.naming;

import java.util.Objects;

public class Version extends Fixed {
    public static Version of(String version) {
        Objects.requireNonNull(version, "version may not be null");
        return new Version(version);
    }

    protected Version(String value) {
        super(value);
    }
}
