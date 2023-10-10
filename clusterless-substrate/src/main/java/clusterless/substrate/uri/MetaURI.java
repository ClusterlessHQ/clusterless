/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.uri;

import clusterless.model.Struct;
import clusterless.model.deploy.Placement;
import clusterless.substrate.store.StateStore;
import clusterless.substrate.store.Stores;
import clusterless.util.Lazy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

public abstract class MetaURI<S extends Struct, T extends MetaURI<S, T>> extends BaseURI implements Struct {
    protected StateStore stateStore;
    protected Placement placement;
    protected S struct;
    protected Supplier<String> storeName = Lazy.of(this::storeName);

    public MetaURI(MetaURI<S, T> other) {
        this.stateStore = other.stateStore;
        this.placement = other.placement;
        this.storeName = other.storeName;
        this.struct = other.struct;
    }

    protected MetaURI(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    protected static void require(boolean wrong, String message) {
        if (wrong) {
            throw new IllegalStateException(message);
        }
    }

    public StateStore stateStore() {
        return stateStore;
    }

    public Placement placement() {
        return placement;
    }

    public S struct() {
        return struct;
    }

    protected abstract T copy();

    protected T setStoreName(String storeName) {
        this.storeName = () -> storeName;
        return self();
    }

    protected T setPlacement(Placement placement) {
        this.placement = placement;
        return self();
    }

    protected T setStruct(S struct) {
        this.struct = struct;
        return self();
    }

    protected abstract T self();

    protected String storeName() {
        require(stateStore, "stateBucket");
        require(placement, "placement");

        return Stores.bootstrapStoreName(stateStore, placement);
    }

    public abstract boolean isPath();

    public boolean isIdentifier() {
        return !isPath();
    }

    public abstract URI uriPrefix();

    public abstract URI uriPath();

    public abstract URI uri();

    public abstract String template();

    protected URI createUri(String path) {
        try {
            return new URI("s3", storeName.get(), path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri", e);
        }
    }

    protected void require(Object object, String message) {
        if (object == null) {
            throw new IllegalStateException(message + " is required");
        }
    }

    @Override
    public String toString() {
        return uri().toString();
    }
}
