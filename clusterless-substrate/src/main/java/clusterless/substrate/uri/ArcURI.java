/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.uri;

import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import clusterless.naming.Partition;
import clusterless.substrate.store.StateStore;
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
public class ArcURI extends MetaURI<Project, ArcURI> {

    public static final String ARCS = "arcs";

    public static Builder builder() {
        return Builder.builder();
    }

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
        super(StateStore.Meta);
    }

    protected ArcURI(ArcURI other) {
        super(other);
        this.project = other.project;
    }

    protected ArcURI copy() {
        return new ArcURI(this);
    }

    @Override
    public boolean isPath() {
        return project == null || project.name() == null || project.version() == null;
    }

    public static ArcURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {providerService}://{stateStore}/arcs/{projectName}/{projectVersion}/{arcName}/arc.json
        String[] split = template.split("/");

        boolean isOnlyPath = isOnlyPath(template);
        int index = isOnlyPath ? 2 : 4;
        String storeName = isOnlyPath ? null : value(split, 2);
        return new ArcURI()
                .setStoreName(storeName)
                .setProject(Project.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index++))
                        .build())
                .withArcName(value(split, index));
    }

    @Override
    public URI uriPrefix() {
        Partition partition = Partition.of(ARCS)
                .withNamedTerminal("project", ofNullable(project).map(Project::name))
                .withNamedTerminal("version", ofNullable(project).map(Project::version))
                .withNamedTerminal("arc", arcName);

        return createUri(partition.prefix());
    }

    @Override
    public URI uriPath() {
        String path = Partition.of(ARCS)
                .withNamedTerminal("project", ofNullable(project).map(Project::name))
                .withNamedTerminal("version", ofNullable(project).map(Project::version))
                .withNamedTerminal("arc", arcName)
                .with("arc.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public URI uri() {
        require(project, "project");

        String path = Partition.of(ARCS)
                .withNamed("project", project.name())
                .withNamed("version", project.version())
                .withNamed("arc", arcName)
                .with("arc.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public String template() {
        String path = Partition
                .namedOf("project", ofNullable(project.name()).orElse("{projectName}"))
                .withNamed("version", ofNullable(project.version()).orElse("{projectVersion}"))
                .withNamed("arc", ofNullable(arcName).orElse("{arcName}"))
                .with("arc.json")
                .partition();

        return String.format("s3://%s/%s/%s", storeName.get(), ARCS, path);
    }

    protected ArcURI setProject(Project project) {
        this.project = project;
        return this;
    }

    public ArcURI setArcName(String arcName) {
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
        protected Project project;
        protected String arcName;
        protected Placement placement;

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

        public ArcURI build() {
            ArcURI arcURI = new ArcURI();
            arcURI.setProject(project);
            arcURI.setArcName(arcName);
            arcURI.setPlacement(placement);
            return arcURI;
        }
    }
}
