package clusterless.scenario.model;

import clusterless.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class IngressStore extends Config {

    public static class Record extends Config {
        String header = null;
        String value = "%d,value";
        int numRecords = 20;

        public String header() {
            return header;
        }

        public String value() {
            return value;
        }

        public int numRecords() {
            return numRecords;
        }
    }

    String region;
    @JsonProperty(required = true)
    URI path;
    /**
     * data-{count}-{timestamp}.txt
     */
    String objectName = "data-%04d-%d.txt";
    int uploadDelaySec = 5 * 60;
    int objectCount = 3;
    Record record = new Record();

    public String region() {
        return region;
    }

    public URI path() {
        return path;
    }

    public String objectName() {
        return objectName;
    }

    public int uploadDelaySec() {
        return uploadDelaySec;
    }

    public int objectCount() {
        return objectCount;
    }

    public Record record() {
        return record;
    }
}
