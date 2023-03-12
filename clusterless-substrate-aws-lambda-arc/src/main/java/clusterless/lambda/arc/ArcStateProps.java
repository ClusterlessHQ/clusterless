package clusterless.lambda.arc;

import clusterless.model.Struct;
import clusterless.model.deploy.Project;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.uri.ArcURI;

import java.net.URI;
import java.util.Map;

public class ArcStateProps implements Struct {
    Project project;
    String name;
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;
    ArcURI arcStatePath;
    Map<String, URI> manifestPath;

    public static Builder builder() {
        return Builder.anArcStateProps();
    }

    public Project project() {
        return project;
    }

    public String name() {
        return name;
    }

    public Map<String, SourceDataset> sources() {
        return sources;
    }

    public Map<String, SinkDataset> sinks() {
        return sinks;
    }

    public ArcURI arcStatePath() {
        return arcStatePath;
    }

    public Map<String, URI> manifestPath() {
        return manifestPath;
    }

    public static final class Builder {
        Project project;
        String name;
        Map<String, SourceDataset> sources;
        Map<String, SinkDataset> sinks;
        ArcURI arcStatePath;
        Map<String, URI> manifestPath;

        private Builder() {
        }

        public static Builder anArcStateProps() {
            return new Builder();
        }

        public Builder withProject(Project project) {
            this.project = project;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSources(Map<String, SourceDataset> sources) {
            this.sources = sources;
            return this;
        }

        public Builder withSinks(Map<String, SinkDataset> sinks) {
            this.sinks = sinks;
            return this;
        }

        public Builder withArcStatePath(ArcURI arcStatePath) {
            this.arcStatePath = arcStatePath;
            return this;
        }

        public Builder withManifestPath(Map<String, URI> manifestPath) {
            this.manifestPath = manifestPath;
            return this;
        }

        public ArcStateProps build() {
            ArcStateProps arcStateProps = new ArcStateProps();
            arcStateProps.manifestPath = this.manifestPath;
            arcStateProps.sinks = this.sinks;
            arcStateProps.name = this.name;
            arcStateProps.sources = this.sources;
            arcStateProps.project = this.project;
            arcStateProps.arcStatePath = this.arcStatePath;
            return arcStateProps;
        }
    }
}
