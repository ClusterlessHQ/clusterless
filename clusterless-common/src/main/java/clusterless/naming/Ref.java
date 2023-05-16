/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.naming;

import java.util.Objects;

/**
 * Creates an identifier to use as an export/import key for provider output/export values.
 * <p>
 * * aws:stage:project:version:resourceType:resourceName:name
 */
public final class Ref {
    public static Ref ref() {
        return new Ref();
    }

    public static Ref idRef() {
        return new Ref().withQualifier(Qualifier.Id);
    }

    public static Ref arnRef() {
        return new Ref().withQualifier(Qualifier.Arn);
    }

    public static Ref nameRef() {
        return new Ref().withQualifier(Qualifier.Name);
    }

    public static boolean isRef(String value) {
        return value != null && value.startsWith("ref:");
    }

    public static String provider(String value) {
        return value != null ? value.split(":")[1] : null;
    }

    final Label provider;
    final Qualifier qualifier;
    final Stage stage;
    final Label scope;
    final Fixed scopeVersion;
    final Label resourceNs;
    final Fixed resourceType;
    final Label resourceName;

    public Ref() {
        provider = null;
        stage = Stage.nullStage();
        scope = null;
        scopeVersion = null;
        resourceNs = null;
        resourceType = null;
        resourceName = null;
        qualifier = null;
    }

    private Ref(Label provider, Qualifier qualifier, Stage stage, Label scope, Fixed scopeVersion, Label resourceNs, Fixed resourceType, Label resourceName) {
        this.provider = provider;
        this.qualifier = qualifier;
        this.stage = stage == null ? Stage.nullStage() : stage.asLower();
        this.scope = scope;
        this.scopeVersion = scopeVersion;
        this.resourceNs = resourceNs;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public Ref withProvider(String provider) {
        Objects.requireNonNull(provider);
        return withProvider(Label.of(provider));
    }

    public Ref withProvider(Label provider) {
        Label.requireNonEmpty(provider);
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withStage(Stage stage) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withScope(String scope) {
        return withScope(Label.of(scope));
    }

    public Ref withScope(Label scope) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withScopeVersion(String scopeVersion) {
        return withScopeVersion(Fixed.of(scopeVersion));
    }

    public Ref withScopeVersion(Fixed scopeVersion) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withResourceNs(String resourceNs) {
        return withResourceNs(Label.of(resourceNs));
    }

    public Ref withResourceNs(Label resourceNs) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withResourceType(String resourceType) {
        return withResourceType(Fixed.of(resourceType));
    }

    public Ref withResourceType(Fixed resourceType) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withResourceName(String resourceName) {
        return withResourceName(Label.of(resourceName));
    }

    public Ref withResourceName(Label resourceName) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Ref withQualifier(Qualifier qualifier) {
        return new Ref(provider, qualifier, stage, scope, scopeVersion, resourceNs, resourceType, resourceName);
    }

    public Label provider() {
        return provider;
    }

    public Stage stage() {
        return stage;
    }

    public Label scope() {
        return scope;
    }

    public Fixed scopeVersion() {
        return scopeVersion;
    }

    public Label resourceNs() {
        return resourceNs;
    }

    public Fixed resourceType() {
        return resourceType;
    }

    public Label resourceName() {
        return resourceName;
    }

    public Qualifier qualifier() {
        return qualifier;
    }

    public Label resourceLabel() {
        requireNonNull(resourceNs, "resourceNs required");
        requireNonNull(resourceType, "resourceType required");
        requireNonNull(resourceName, "resourceName required");

        return Label.NULL
                .with(resourceNs)
                .with(Label.of(resourceType.value()))
                .with(resourceName);
    }

    public Label label() {
        requireNonNull(provider, "provider required");
        requireNonNull(qualifier, "qualifier required");
        requireNonNull(scope, "scope required");
        requireNonNull(scopeVersion, "scopeVersion required");
        requireNonNull(resourceNs, "resourceNs required");
        requireNonNull(resourceType, "resourceType required");
        requireNonNull(resourceName, "resourceName required");

        return Label.of("ref")
                .with(provider)
                .with(qualifier)
                .with(stage)
                .with(scope)
                .with(scopeVersion)
                .with(resourceNs)
                .with(Label.of(resourceType.value()))
                .with(resourceName);
    }

    private static void requireNonNull(Label label, String message) {
        Objects.requireNonNull(label, message);

        if (label.isNull()) {
            throw new NullPointerException(message);
        }
    }


    public String exportName() {
        return label().lowerColonPath();
    }

    @Override
    public String toString() {
        return exportName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;
        return Objects.equals(provider, ref.provider) && Objects.equals(stage, ref.stage) && Objects.equals(scope, ref.scope) && Objects.equals(scopeVersion, ref.scopeVersion) && Objects.equals(resourceNs, ref.resourceNs) && Objects.equals(resourceType, ref.resourceType) && Objects.equals(resourceName, ref.resourceName) && qualifier == ref.qualifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, stage, scope, scopeVersion, resourceNs, resourceType, resourceName, qualifier);
    }

    public enum Qualifier implements Label.EnumLabel {
        Name,
        Id,
        Arn;
    }
}
