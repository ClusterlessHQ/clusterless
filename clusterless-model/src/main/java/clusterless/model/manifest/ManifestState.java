package clusterless.model.manifest;

import clusterless.model.State;
import clusterless.util.Partition;
import clusterless.util.Strings;

import java.util.Locale;

public enum ManifestState implements State, Partition.EnumPartition {
    complete,
    partial,
    removed;

    @Override
    public String key() {
        return "state";
    }

    public boolean hasAttempts() {
        switch (this) {
            case partial:
            case removed:
                return true;
            case complete:
                break;
        }

        return false;
    }

    public static ManifestState parse(String partition) {
        if (Strings.emptyToNull(partition) == null) {
            return null;
        }

        partition = partition.toLowerCase(Locale.ROOT);

        for (ManifestState value : values()) {
            if (partition.matches(String.format("^(.*[=])?%s([.].*)?$", value))) {
                return value;
            }
        }

        return null;
    }
}
