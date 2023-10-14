/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.uri;

import clusterless.cls.model.State;
import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.store.StateStore;
import clusterless.cls.substrate.store.Stores;
import clusterless.cls.util.Lazy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

public abstract class StateURI<S extends State, T extends StateURI<S, T>> extends BaseURI implements Struct {
    protected StateStore stateStore;
    protected Placement placement;
    protected String lotId;
    protected S state;
    protected Supplier<String> storeName = Lazy.of(this::storeName);

    public StateURI(StateURI<S, T> other) {
        this.stateStore = other.stateStore;
        this.placement = other.placement;
        this.lotId = other.lotId;
        this.state = other.state;
        this.storeName = other.storeName;
    }

    protected StateURI(StateStore stateStore) {
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

    public String lotId() {
        return lotId;
    }

    public S state() {
        return state;
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

    protected T setLotId(String lotId) {
        this.lotId = lotId;
        return self();
    }

    protected T setState(S state) {
        this.state = state;
        return self();
    }

    protected abstract T self();

    public T withLot(String lotId) {
        return copy().setLotId(lotId);
    }

    public T withState(S state) {
        return copy().setState(state);
    }

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
