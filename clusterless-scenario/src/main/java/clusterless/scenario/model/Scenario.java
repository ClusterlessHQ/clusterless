package clusterless.scenario.model;

import clusterless.config.Config;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Scenario extends Config {

    boolean enabled = true;

    String name;
    String description;

    List<Path> projectFiles = new LinkedList<>();

    Path projectDirectory;

    List<IngressStore> ingressStores = new LinkedList<>();

    List<WatchedStore> watchedStores = new LinkedList<>();

    public boolean enabled() {
        return enabled;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Path projectDirectory() {
        return projectDirectory;
    }

    public List<Path> projectFiles() {
        return projectFiles;
    }

    public Scenario setProjectDirectory(Path projectDirectory) {
        this.projectDirectory = projectDirectory;
        return this;
    }

    public List<IngressStore> ingressStores() {
        return ingressStores;
    }

    public List<WatchedStore> watchedStores() {
        return watchedStores;
    }
}
