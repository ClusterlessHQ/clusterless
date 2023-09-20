/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.manifest;

import clusterless.model.Content;
import clusterless.model.Struct;
import clusterless.model.UriType;
import clusterless.model.deploy.Dataset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

/**
 *
 */
public class Manifest implements Content, Struct {
    public static final String JSON_EXTENSION = "json";

    @JsonProperty(required = true)
    protected ManifestState state;
    protected String comment;
    @JsonProperty(required = true)
    protected String lotId;
    @JsonProperty(required = true)
    protected UriType uriType = UriType.identifier;
    @JsonProperty(required = true)
    protected List<URI> uris;

    public Manifest() {
    }

    public static Builder builder() {
        return Builder.aManifest();
    }

    public ManifestState state() {
        return state;
    }

    public String comment() {
        return comment;
    }

    public UriType uriType() {
        return uriType;
    }

    public String lotId() {
        return lotId;
    }

    public List<URI> uris() {
        return uris;
    }

    @Override
    public String extension() {
        return JSON_EXTENSION;
    }

    @Override
    public String contentType() {
        return "application/json";
    }

    public static final class Builder {
        ManifestState state;
        String comment;
        Dataset dataset;
        String lotId;
        UriType uriType = UriType.identifier;
        List<URI> uris;

        private Builder() {
        }

        public static Builder aManifest() {
            return new Builder();
        }

        public Builder withState(ManifestState state) {
            this.state = state;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder withLotId(String lotId) {
            this.lotId = lotId;
            return this;
        }

        public Builder withUriType(UriType uriType) {
            this.uriType = uriType;
            return this;
        }

        public Builder withUris(List<URI> uris) {
            this.uris = uris;
            return this;
        }

        public Manifest build() {
            Manifest manifest = new Manifest();
            manifest.uris = this.uris;
            manifest.comment = this.comment;
            manifest.state = this.state;
            manifest.uriType = this.uriType;
            manifest.lotId = this.lotId;
            return manifest;
        }
    }
}
