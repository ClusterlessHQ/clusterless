package clusterless.naming;

import java.util.Objects;

/**
 * Creates an identifier to use as an export/import key for provider output/export values.
 * <p>
 * * aws:stage:project:version:resourceType:resourceName:name
 */
public final class ExportRef {
    public static ExportRef ref() {
        return new ExportRef();
    }

    public static ExportRef idRef() {
        return new ExportRef().withQualifier(ExportQualifier.Id);
    }

    public static ExportRef arnRef() {
        return new ExportRef().withQualifier(ExportQualifier.Arn);
    }

    public static ExportRef nameRef() {
        return new ExportRef().withQualifier(ExportQualifier.Name);
    }

    final Label provider;
    final Stage stage;
    final Label scope;
    final Fixed scopeVersion;
    final Fixed resourceType;
    final Label resourceName;
    final ExportQualifier qualifier;

    public ExportRef() {
        provider = null;
        stage = Stage.nullStage();
        scope = null;
        scopeVersion = null;
        resourceType = null;
        resourceName = null;
        qualifier = null;
    }

    private ExportRef(Label provider, Stage stage, Label scope, Fixed scopeVersion, Fixed resourceType, Label resourceName, ExportQualifier qualifier) {
        this.provider = provider;
        this.stage = stage == null ? Stage.nullStage() : stage.asLower();
        this.scope = scope;
        this.scopeVersion = scopeVersion;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.qualifier = qualifier;
    }

    public ExportRef withProvider(String provider) {
        Objects.requireNonNull(provider);
        return withProvider(Label.of(provider));
    }

    public ExportRef withProvider(Label provider) {
        Label.requireNonEmpty(provider);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withStage(Stage stage) {
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withScope(String scope) {
        Objects.requireNonNull(scope);
        return withScope(Label.of(scope));
    }

    public ExportRef withScope(Label scope) {
        Label.requireNonEmpty(scope);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withScopeVersion(String scopeVersion) {
        Objects.requireNonNull(scopeVersion);
        return withScopeVersion(Fixed.of(scopeVersion));
    }

    public ExportRef withScopeVersion(Fixed scopeVersion) {
        Label.requireNonEmpty(scopeVersion);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withResourceType(String resourceType) {
        Objects.requireNonNull(resourceType);
        return withResourceType(Fixed.of(resourceType));
    }

    public ExportRef withResourceType(Fixed resourceType) {
        Label.requireNonEmpty(resourceType);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withResourceName(String resourceName) {
        Objects.requireNonNull(resourceName);
        return withResourceName(Label.of(resourceName));
    }

    public ExportRef withResourceName(Label resourceName) {
        Label.requireNonEmpty(resourceName);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public ExportRef withQualifier(ExportQualifier qualifier) {
        Label.requireNonEmpty(qualifier);
        return new ExportRef(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
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

    public Fixed resourceType() {
        return resourceType;
    }

    public Label resourceName() {
        return resourceName;
    }

    public ExportQualifier qualifier() {
        return qualifier;
    }

    public Label label() {
        Objects.requireNonNull(provider, "provider required");
        Objects.requireNonNull(stage, "stage required");
        Objects.requireNonNull(scope, "scope required");
        Objects.requireNonNull(scopeVersion, "scopeVersion required");
        Objects.requireNonNull(resourceType, "resourceType required");
        Objects.requireNonNull(resourceName, "resourceName required");
        Objects.requireNonNull(qualifier, "qualifier required");

        return provider
                .with(stage)
                .with(scope)
                .with(scopeVersion)
                .with(Label.of(resourceType.value()))
                .with(resourceName)
                .with(qualifier);
    }

    public String exportName() {
        return label().lowerColonPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportRef ref = (ExportRef) o;
        return Objects.equals(provider, ref.provider) && Objects.equals(stage, ref.stage) && Objects.equals(scope, ref.scope) && Objects.equals(scopeVersion, ref.scopeVersion) && Objects.equals(resourceType, ref.resourceType) && Objects.equals(resourceName, ref.resourceName) && qualifier == ref.qualifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, stage, scope, scopeVersion, resourceType, resourceName, qualifier);
    }

    public enum ExportQualifier implements Label.EnumLabel {
        Name,
        Id,
        Arn;
    }
}
