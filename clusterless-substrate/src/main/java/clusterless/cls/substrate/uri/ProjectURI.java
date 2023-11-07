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
import clusterless.cls.substrate.store.StateStore;
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
 * {@code {providerService}://{stateStore}/{projectName}/{projectVersion}/}
 * <p>
 * Identifier
 * <p>
 * {@code {providerService}://{stateStore}/{projectName}/{projectVersion}/project.json}
 */
@JsonSerialize(using = ProjectURI.Serializer.class)
@JsonDeserialize(using = ProjectURI.DeSerializer.class)
public class ProjectURI extends MetaURI<Project, ProjectURI> {

    public static final String PROJECTS = "projects";

    public static Builder builder() {
        return Builder.builder();
    }

    static class Serializer extends StdScalarSerializer<ProjectURI> {
        protected Serializer() {
            super(ProjectURI.class);
        }

        @Override
        public void serialize(ProjectURI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.template());
        }
    }

    static class DeSerializer extends StdScalarDeserializer<ProjectURI> {
        protected DeSerializer() {
            super(ProjectURI.class);
        }

        @Override
        public ProjectURI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            // The critical path: ensure we handle the common case first.
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                return ProjectURI.parse(p.getText());
            }
            // [databind#381]
//            if (p.hasToken(JsonToken.START_ARRAY)) {
//                return _deserializeFromArray(p, ctxt);
//            }

            return ProjectURI.parse(_parseString(p, ctxt, this));
        }
    }

    protected Project project;

    protected ProjectURI() {
        super(StateStore.Meta);
    }

    protected ProjectURI(ProjectURI other) {
        super(other);
        this.project = other.project;
    }

    public Project project() {
        return project;
    }

    protected ProjectURI copy() {
        return new ProjectURI(this);
    }

    @Override
    public boolean isPath() {
        return project == null || project.name() == null || project.version() == null;
    }

    /**
     * Template may be a URI or an absolute path.
     *
     * @param template
     * @return
     */
    public static ProjectURI parse(String template) {
        Objects.requireNonNull(template, "template is null");

        // {providerService}://{stateStore}/projects/{projectName}/{projectVersion}/project.json
        String[] split = template.split("/");

        Format format = isOnlyPath(PROJECTS, template);
        int index = format.offset();
        String storeName = format == Format.full ? value(split, 2) : null;
        return new ProjectURI()
                .setStoreName(storeName) // the bucket in s3
                .setProject(Project.Builder.builder()
                        .withName(value(split, index++))
                        .withVersion(value(split, index))
                        .build());
    }

    @Override
    public URI uriPrefix() {
        Partition partition = Partition.of(PROJECTS)
                .withNamedTerminal("name", ofNullable(project).map(Project::name))
                .withNamedTerminal("version", ofNullable(project).map(Project::version));

        return createUri(partition.prefix());
    }

    @Override
    public URI uriPath() {
        String path = Partition.of(PROJECTS)
                .withNamedTerminal("name", Optional.ofNullable(project).map(Project::name))
                .withNamedTerminal("version", Optional.ofNullable(project).map(Project::version))
                .with("project.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public URI uri() {
        require(project, "project");

        String path = Partition.of(PROJECTS)
                .withNamed("name", project.name())
                .withNamed("version", project.version())
                .with("project.json")
                .prefix();

        return createUri(path);
    }

    @Override
    public String template() {
        String path = Partition.namedOf("name", ofNullable(project.name()).orElse("{projectName}"))
                .withNamed("version", ofNullable(project.version()).orElse("{projectVersion}"))
                .with("project.json")
                .partition();

        return String.format("s3://%s/%s/%s", storeName.get(), PROJECTS, path);
    }

    protected ProjectURI setProject(Project project) {
        this.project = project;
        return this;
    }

    public ProjectURI withProject(Project project) {
        return copy().setProject(project);
    }

    @Override
    public ProjectURI self() {
        return this;
    }

    public static final class Builder {
        protected Placement placement;
        protected Project project;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPlacement(Placement placement) {
            this.placement = placement;
            return this;
        }

        public Builder withProject(Project project) {
            this.project = project;
            return this;
        }

        public ProjectURI build() {
            ProjectURI projectURI = new ProjectURI();
            projectURI.setPlacement(placement);
            projectURI.setProject(project);
            return projectURI;
        }
    }
}
