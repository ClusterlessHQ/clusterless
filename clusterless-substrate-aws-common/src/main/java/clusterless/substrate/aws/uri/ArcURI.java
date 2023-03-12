package clusterless.substrate.aws.uri;

import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.store.StateStore;
import clusterless.util.Partition;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import static clusterless.util.Optionals.optional;
import static java.util.Optional.ofNullable;

/**
 * Path
 * <p>
 * {@code {providerService}://{stateStore}/{projectName}/{projectVersion}/{arcName}/}
 * <p>
 * Identifier
 * <p>
 * {@code {providerService}://{stateStore}/{projectName}/{projectVersion}/{arcName}/{lot}/{state}.arc}
 */
@JsonSerialize(using = ArcURI.Serializer.class)
@JsonDeserialize(using = ArcURI.DeSerializer.class)
public class ArcURI extends StateURI<ArcState, ArcURI> {
    static class Serializer extends StdScalarSerializer<ArcURI> {
        protected Serializer() {
            super(ArcURI.class);
        }

        @Override
        public void serialize(ArcURI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.template());
        }
    }

    static class DeSerializer extends StdScalarDeserializer<ArcURI> {
        protected DeSerializer() {
            super(ArcURI.class);
        }

        @Override
        public ArcURI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            // The critical path: ensure we handle the common case first.
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                return ArcURI.parse(p.getText());
            }
            // [databind#381]
//            if (p.hasToken(JsonToken.START_ARRAY)) {
//                return _deserializeFromArray(p, ctxt);
//            }

            return ArcURI.parse(_parseString(p, ctxt, this));
        }
    }

    protected Project project;
    protected String arcName;

    protected ArcURI() {
        super(StateStore.Arc);
    }

    protected ArcURI(ArcURI other) {
        super(other);
        this.project = other.project;
        this.arcName = other.arcName;
    }

    public static Builder builder() {
        return Builder.anArcStateIdentifier();
    }

    protected ArcURI copy() {
        return new ArcURI(this);
    }

    @Override
    public boolean isPath() {
        return state == null;
    }

    public static ArcURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {providerService}://{stateStore}/arcs/{projectName}/{projectVersion}/{arcName}/{lot}/{state}.arc
        String[] split = template.split("/");

        int index = 4; // start after arcs
        return new ArcURI()
                .setStoreName(value(split, 2))
                .setProject(Project.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index++))
                        .build())
                .setArcName(value(split, index++))
                .setLotId(value(split, index++))
                .setState(ArcState.parse(optional(index, split).orElse(null)));
    }

    @Override
    public URI uri() {
        require(project, "project");
        require(state != null && lotId == null, "lotId is required if state is set");

        String path = Partition.of("arcs")
                .withNamed("name", project.name())
                .withNamed("version", project.version())
                .withNamed("arc", arcName)
                .withNamed("lot", lotId) // retain case
                .pathUnless(ofNullable(state).map(s -> s.name().concat(".arc")));

        return createUri(path);
    }

    @Override
    public String template() {
        String path = Partition.namedOf("name", ofNullable(project.name()).orElse("{projectName}"))
                .withNamed("version", ofNullable(project.version()).orElse("{projectVersion}"))
                .withNamed("arc", ofNullable(arcName).orElse("{arcName}"))
                .withNamed("lot", ofNullable(lotId).orElse("{lot}")) // retain case
                .with(ofNullable(state)
                        .map(s -> s.name().concat(".arc"))
                        .orElse("{state}.arc")
                )
                .partition();

        return String.format("s3://%s/arcs/%s", storeName.get(), path);
    }

    protected ArcURI setProject(Project project) {
        this.project = project;
        return this;
    }

    protected ArcURI setArcName(String arcName) {
        this.arcName = arcName;
        return this;
    }

    public ArcURI withProject(Project project) {
        return copy().setProject(project);
    }

    public ArcURI withArcName(String arcName) {
        return copy().setArcName(arcName);
    }

    @Override
    public ArcURI self() {
        return this;
    }

    public static final class Builder {
        private Project project;
        private String arcName;
        private Placement placement;
        private String lotId;
        private ArcState state;

        private Builder() {
        }

        public static Builder anArcStateIdentifier() {
            return new Builder();
        }

        public Builder withProject(Project project) {
            this.project = project;
            return this;
        }

        public Builder withArcName(String arcName) {
            this.arcName = arcName;
            return this;
        }

        public Builder withPlacement(Placement placement) {
            this.placement = placement;
            return this;
        }

        public Builder withLotId(String lotId) {
            this.lotId = lotId;
            return this;
        }

        public Builder withState(ArcState state) {
            this.state = state;
            return this;
        }

        public ArcURI build() {
            ArcURI arcStateIdentifier = new ArcURI();
            arcStateIdentifier.setProject(project);
            arcStateIdentifier.setArcName(arcName);
            arcStateIdentifier.setPlacement(placement);
            arcStateIdentifier.setLotId(lotId);
            arcStateIdentifier.setState(state);
            return arcStateIdentifier;
        }
    }
}