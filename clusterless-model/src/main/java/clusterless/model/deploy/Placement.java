/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Struct;

import java.util.Objects;

/**
 *
 */
public class Placement implements Struct {
    String provider;
    String stage;
    String account;
    String region;

    public Placement() {
    }

    private Placement(Builder builder) {
        provider = builder.provider;
        stage = builder.stage;
        account = builder.account;
        region = builder.region;
    }

    public String provider() {
        return provider;
    }

    public String stage() {
        return stage;
    }

    public String account() {
        return account;
    }

    public String region() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placement placement = (Placement) o;
        return Objects.equals(provider, placement.provider) && Objects.equals(stage, placement.stage) && Objects.equals(account, placement.account) && Objects.equals(region, placement.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, stage, account, region);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Placement{");
        sb.append("provider='").append(provider).append('\'');
        sb.append(", stage='").append(stage).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", region='").append(region).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * {@code Placement} builder static inner class.
     */
    public static final class Builder {
        private String provider;
        private String stage;
        private String account;
        private String region;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code provider} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code provider} to set
         * @return a reference to this Builder
         */
        public Builder withProvider(String val) {
            provider = val;
            return this;
        }

        /**
         * Sets the {@code stage} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code stage} to set
         * @return a reference to this Builder
         */
        public Builder withStage(String val) {
            stage = val;
            return this;
        }

        /**
         * Sets the {@code account} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code account} to set
         * @return a reference to this Builder
         */
        public Builder withAccount(String val) {
            account = val;
            return this;
        }

        /**
         * Sets the {@code region} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code region} to set
         * @return a reference to this Builder
         */
        public Builder withRegion(String val) {
            region = val;
            return this;
        }

        /**
         * Returns a {@code Placement} built from the parameters previously set.
         *
         * @return a {@code Placement} built with parameters of this {@code Placement.Builder}
         */
        public Placement build() {
            return new Placement(this);
        }
    }
}
