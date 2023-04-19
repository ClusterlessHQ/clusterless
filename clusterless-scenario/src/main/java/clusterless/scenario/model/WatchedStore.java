package clusterless.scenario.model;

import clusterless.config.Config;

import java.net.URI;

public class WatchedStore extends Config {
    String region;
    URI path;
    int objectCount = 3;
    int pollingSleepSec = 60;
    int timeoutSec = 3 * 15 * 60;

    public String region() {
        return region;
    }

    public URI path() {
        return path;
    }

    public int objectCount() {
        return objectCount;
    }

    public int pollingSleepSec() {
        return pollingSleepSec;
    }

    public int timeoutSec() {
        return timeoutSec;
    }
}
