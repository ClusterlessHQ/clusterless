/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.managed.component.DocumentsModel;
import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 */
@DocumentsModel(
        synopsis = "The cloud environment to deploy to.",
        description = """
                Where will the project be deployed in the declared provider environment.

                provider: Currently only "aws". Required.
                                
                account: The AWS account number to deploy to. Required.
                                       
                stage: The stage of the deployment, such as "dev", "test", "prod". Optional.
                       This allows for multiple deployments of the same project to the same provider region.
                       
                region: Any valid AWS region, such as "us-east-1", "us-west-2", etc. Required.
                """
)
@JsonPropertyOrder({"provider", "stage", "account", "region"})
public class Placement implements Struct {
    String provider;
    String stage;
    String account;
    String region;

    public Placement() {
    }

    protected Placement(Placement other) {
        this.provider = other.provider;
        this.stage = other.stage;
        this.account = other.account;
        this.region = other.region;
    }

    private Placement(Builder builder) {
        provider = builder.provider;
        stage = builder.stage;
        account = builder.account;
        region = builder.region;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    @NotNull
    private Placement copy() {
        return new Placement(this);
    }

    public String provider() {
        return provider;
    }

    protected Placement setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public Placement withProvider(String provider) {
        return copy().setProvider(provider);
    }

    public String stage() {
        return stage;
    }

    protected Placement setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public Placement withStage(String stage) {
        return copy().setStage(stage);
    }

    public String account() {
        return account;
    }

    protected Placement setAccount(String account) {
        this.account = account;
        return this;
    }

    public Placement withAccount(String account) {
        return copy().setAccount(account);
    }

    public String region() {
        return region;
    }

    protected Placement setRegion(String region) {
        this.region = region;
        return this;
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

    public static final class Builder {
        String provider;
        String stage;
        String account;
        String region;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder withStage(String stage) {
            this.stage = stage;
            return this;
        }

        public Builder withAccount(String account) {
            this.account = account;
            return this;
        }

        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }

        public Placement build() {
            Placement placement = new Placement();
            placement.account = this.account;
            placement.provider = this.provider;
            placement.region = this.region;
            placement.stage = this.stage;
            return placement;
        }
    }
}
