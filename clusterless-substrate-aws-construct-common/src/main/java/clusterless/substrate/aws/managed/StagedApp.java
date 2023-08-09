/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.naming.Label;
import clusterless.naming.Ref;
import clusterless.naming.Stage;
import clusterless.substrate.aws.resources.DeployMeta;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class StagedApp extends App {
    public static StagedApp stagedOf(Construct scope) {
        return (StagedApp) scope.getNode().getRoot();
    }

    Stage stage;

    DeployMeta deployMeta = new DeployMeta();

    Map<Ref, Construct> refConstructs = new HashMap<>();

    public StagedApp(@NotNull AppProps props, @NotNull Stage stage) {
        super(props);
        this.stage = stage;
    }

    public Stage stage() {
        return stage;
    }

    public StagedApp setDeployMeta(DeployMeta deployMeta) {
        this.deployMeta = deployMeta;
        return this;
    }

    public DeployMeta deployMeta() {
        return deployMeta;
    }

    public void addRef(Ref ref, Construct construct) {
        refConstructs.put(ref, construct);
    }

    public Construct resolveRef(Ref ref) {
        return refConstructs.get(ref);
    }

    public Construct resolveRef(String relativeTypeRef) {
        Objects.requireNonNull(relativeTypeRef, "relativeTypeRef must not be null");

        String[] split = relativeTypeRef.split(":");

        if (split.length > 4) {
            throw new IllegalStateException("invalid local ref: " + relativeTypeRef);
        }

        String provider = split.length == 4 ? split[0] : null;
        String resourceNs = split.length == 4 ? split[1] : split.length == 3 ? split[0] : null;
        String resourceType = split.length == 4 ? split[2] : split.length == 3 ? split[1] : split[0];
        String resourceName = split.length == 4 ? split[3] : split.length == 3 ? split[2] : split.length == 2 ? split[1] : split[0];

        Set<Map.Entry<Ref, Construct>> results = refConstructs.entrySet();

        results = results
                .stream()
                .filter(ref -> Label.of(resourceName).camelCase().equalsIgnoreCase(ref.getKey().resourceName().camelCase()))
                .collect(Collectors.toSet());

        if (results.isEmpty()) {
            throw new IllegalArgumentException("no constructs found for: " + relativeTypeRef + ", available: " + refConstructs.keySet());
        }

        if (results.size() == 1) {
            return results.stream().findFirst().get().getValue();
        }

        if (resourceType == null) {
            throw new IllegalArgumentException("too many constructs found for: " + relativeTypeRef + ", available: " + results.stream().map(Map.Entry::getKey).toList());
        }

        results = results
                .stream()
                .filter(ref -> Label.of(resourceType).camelCase().equalsIgnoreCase(ref.getKey().resourceType().camelCase()))
                .collect(Collectors.toSet());

        if (results.isEmpty()) {
            throw new IllegalArgumentException("no constructs found for: " + relativeTypeRef + ", available: " + refConstructs.keySet());
        }

        if (results.size() == 1) {
            return results.stream().findFirst().get().getValue();
        }

        if (resourceNs == null) {
            throw new IllegalArgumentException("too many constructs found for: " + relativeTypeRef + ", available: " + results.stream().map(Map.Entry::getKey).toList());
        }

        results = results
                .stream()
                .filter(ref -> Label.of(resourceNs).camelCase().equalsIgnoreCase(ref.getKey().resourceNs().camelCase()))
                .collect(Collectors.toSet());

        if (results.isEmpty()) {
            throw new IllegalArgumentException("no constructs found for: " + relativeTypeRef + ", available: " + refConstructs.keySet());
        }

        if (results.size() == 1) {
            return results.stream().findFirst().get().getValue();
        }

        if (provider == null) {
            throw new IllegalArgumentException("too many constructs found for: " + relativeTypeRef + ", available: " + results.stream().map(Map.Entry::getKey).toList());
        }

        results = results
                .stream()
                .filter(ref -> Label.of(provider).camelCase().equalsIgnoreCase(ref.getKey().provider().camelCase()))
                .collect(Collectors.toSet());

        if (results.size() == 1) {
            return results.stream().findFirst().get().getValue();
        }

        throw new IllegalArgumentException("no constructs found for: " + relativeTypeRef + ", available: " + refConstructs.keySet());
    }
}
