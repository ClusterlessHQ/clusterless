/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.scoped;

import clusterless.cls.naming.Label;
import clusterless.cls.naming.Ref;
import clusterless.cls.naming.Stage;
import clusterless.cls.naming.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
public class ScopedApp extends App {
    public static ScopedApp stagedOf(Construct scope) {
        return (ScopedApp) scope.getNode().getRoot();
    }

    private final Stage stage;
    private final Label name;
    private final Version version;

    private final ScopedMeta scopedMeta;

    private final Map<Ref, Construct> refConstructs = new HashMap<>();

    public ScopedApp(@NotNull AppProps props, @NotNull Stage stage, @NotNull Label name, @NotNull Version version) {
        this(props, stage, name, version, new ScopedMeta());
    }

    protected ScopedApp(@Nullable AppProps props, Stage stage, @NotNull Label name, @NotNull Version version, @NotNull ScopedMeta scopedMeta) {
        super(props);
        this.stage = stage;
        this.name = name;
        this.version = version;
        this.scopedMeta = scopedMeta;
    }

    public Stage stage() {
        return stage;
    }

    public Label name() {
        return name;
    }

    public Version version() {
        return version;
    }

    public ScopedMeta stagedMeta() {
        return scopedMeta;
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
