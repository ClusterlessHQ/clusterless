package clusterless.model.manifest;

import clusterless.util.Partition;
import clusterless.util.Strings;

import java.util.Locale;

public enum ManifestState implements Partition.EnumPartition {
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
                return true;
            case removed:
            case complete:
                break;
        }

        return false;
    }

    public static ManifestState parse(String partition) {
        if (Strings.emptyToNull(partition) == null) {
            return null;
        }

        String[] split = partition.split("=", 2);

        if (split.length == 1) {
            return valueOf(split[0].toLowerCase(Locale.ROOT));
        }

        return valueOf(split[1].toLowerCase(Locale.ROOT));
    }
}
