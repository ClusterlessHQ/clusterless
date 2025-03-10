/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.Struct;

import java.net.URI;

/**
 * TODO: make this a base class and parameterize the activity properties, see CloudWatchExportActivityProps
 */
public class ActivityProps implements Struct {
    @JsonRequiredProperty
    String schedule;
    @JsonRequiredProperty
    URI pathURI;

    private ActivityProps(Builder builder) {
        schedule = builder.schedule;
        pathURI = builder.pathURI;
    }

    public String schedule() {
        return schedule;
    }

    public URI pathURI() {
        return pathURI;
    }


    /**
     * {@code ActivityProps} builder static inner class.
     */
    public static final class Builder {
        private String schedule;
        private URI pathURI;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code schedule} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code schedule} to set
         * @return a reference to this Builder
         */
        public Builder withSchedule(String val) {
            schedule = val;
            return this;
        }

        /**
         * Sets the {@code pathURI} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code pathURI} to set
         * @return a reference to this Builder
         */
        public Builder withPathURI(URI val) {
            pathURI = val;
            return this;
        }

        /**
         * Returns a {@code ActivityProps} built from the parameters previously set.
         *
         * @return a {@code ActivityProps} built with parameters of this {@code ActivityProps.Builder}
         */
        public ActivityProps build() {
            return new ActivityProps(this);
        }
    }
}
