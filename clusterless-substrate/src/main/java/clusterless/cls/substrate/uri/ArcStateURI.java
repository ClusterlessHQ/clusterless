/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.uri;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.store.StateStore;
import clusterless.cls.util.Optionals;
import clusterless.commons.naming.Partition;
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
import java.util.Optional;

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
@JsonSerialize(using = ArcStateURI.Serializer.class)
@JsonDeserialize(using = ArcStateURI.DeSerializer.class)
public class ArcStateURI extends StateURI<ArcState, ArcStateURI> {

    public static final String ARCS = "arcs";

    static class Serializer extends StdScalarSerializer<ArcStateURI> {
        protected Serializer() {
            super(ArcStateURI.class);
        }

        @Override
        public void serialize(ArcStateURI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.template());
        }
    }

    static class DeSerializer extends StdScalarDeserializer<ArcStateURI> {
        protected DeSerializer() {
            super(ArcStateURI.class);
        }

        @Override
        public ArcStateURI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            // The critical path: ensure we handle the common case first.
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                return ArcStateURI.parse(p.getText());
            }
            // [databind#381]
//            if (p.hasToken(JsonToken.START_ARRAY)) {
//                return _deserializeFromArray(p, ctxt);
//            }

            return ArcStateURI.parse(_parseString(p, ctxt, this));
        }
    }

    protected Project project;
    protected String arcName;

    protected ArcStateURI() {
        super(StateStore.Arc);
    }

    protected ArcStateURI(ArcStateURI other) {
        super(other);
        this.project = other.project;
        this.arcName = other.arcName;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    protected ArcStateURI copy() {
        return new ArcStateURI(this);
    }

    @Override
    public boolean isPath() {
        return state == null;
    }

    public static ArcStateURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {providerService}://{stateStore}/arcs/{projectName}/{projectVersion}/{arcName}/{lot}/{state}.arc
        String[] split = template.split("/");

        boolean isOnlyPath = isOnlyPath(template);
        int index = isOnlyPath ? 2 : 4;
        String storeName = isOnlyPath ? null : value(split, 2);
        return new ArcStateURI()
                .setStoreName(storeName)
                .setProject(Project.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index++))
                        .build())
                .setArcName(value(split, index++))
                .setLotId(value(split, index++))
                .setState(ArcState.parse(Optionals.optional(index, split).orElse(null)));
    }

    @Override
    public URI uriPrefix() {
        String prefix = Partition.of(ARCS)
                .withNamedTerminal("project", ofNullable(project).map(Project::name))
                .withNamedTerminal("version", ofNullable(project).map(Project::version))
                .withNamedTerminal("arc", arcName)
                .withNamedTerminal("lot", lotId) // retain case
                .withTerminal(ofNullable(state).map(s -> s.name().concat(".arc")))
                .prefix();

        return createUri(prefix);
    }

    @Override
    public URI uriPath() {
        String path = Partition.of(ARCS)
                .withNamedTerminal("project", Optional.ofNullable(project).map(Project::name))
                .withNamedTerminal("version", Optional.ofNullable(project).map(Project::version))
                .withNamedTerminal("arc", arcName)
                .withNamedTerminal("lot", lotId) // retain case
                .pathUnless(ofNullable(state).map(s -> s.name().concat(".arc")));

        return createUri(path);
    }

    @Override
    public URI uri() {
        require(project, "project");
        require(state != null && lotId == null, "lotId is required if state is set");

        String path = Partition.of(ARCS)
                .withNamed("project", project.name())
                .withNamed("version", project.version())
                .withNamed("arc", arcName)
                .withNamed("lot", lotId) // retain case
                .pathUnless(ofNullable(state).map(s -> s.name().concat(".arc")));

        return createUri(path);
    }

    @Override
    public String template() {
        String path = Partition.namedOf("project", ofNullable(project.name()).orElse("{projectName}"))
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

    protected ArcStateURI setProject(Project project) {
        this.project = project;
        return this;
    }

    protected ArcStateURI setArcName(String arcName) {
        this.arcName = arcName;
        return this;
    }

    public ArcStateURI withProject(Project project) {
        return copy().setProject(project);
    }

    public ArcStateURI withArcName(String arcName) {
        return copy().setArcName(arcName);
    }

    @Override
    public ArcStateURI self() {
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

        public static Builder builder() {
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

        public ArcStateURI build() {
            ArcStateURI arcStateIdentifier = new ArcStateURI();
            arcStateIdentifier.setProject(project);
            arcStateIdentifier.setArcName(arcName);
            arcStateIdentifier.setPlacement(placement);
            arcStateIdentifier.setLotId(lotId);
            arcStateIdentifier.setState(state);
            return arcStateIdentifier;
        }
    }
}
