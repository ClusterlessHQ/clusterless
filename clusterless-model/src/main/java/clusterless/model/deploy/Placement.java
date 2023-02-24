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
}
