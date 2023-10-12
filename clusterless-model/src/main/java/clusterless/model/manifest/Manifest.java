/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.manifest;

import clusterless.json.JsonRequiredProperty;
import clusterless.model.Content;
import clusterless.model.Struct;
import clusterless.model.UriType;

import java.net.URI;
import java.util.List;

/**
 *
 */
public class Manifest implements Content, Struct {
    public static final String JSON_EXTENSION = "json";

    @JsonRequiredProperty
    protected ManifestState state;
    protected String comment;
    @JsonRequiredProperty
    protected String lotId;
    @JsonRequiredProperty
    protected UriType uriType = UriType.identifier;
    @JsonRequiredProperty
    protected List<URI> uris;

    public Manifest() {
    }

    public static Builder builder() {
        return Builder.builder();
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
        protected ManifestState state;
        protected String comment;
        protected String lotId;
        protected UriType uriType = UriType.identifier;
        protected List<URI> uris;

        private Builder() {
        }

        public static Builder builder() {
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
            manifest.state = this.state;
            manifest.uriType = this.uriType;
            manifest.comment = this.comment;
            manifest.lotId = this.lotId;
            return manifest;
        }
    }
}
