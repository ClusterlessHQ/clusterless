# Clusterless guidelines

Living engineering knowledge for the clusterless codebase. Skim before any
non-trivial change. Two sections:

- **Project conventions** — how the codebase is shaped. Architectural
  invariants that survive across resolutions, and the DX contract new code
  is held to.
- **Resolution patterns** — workflow, triage filters, and implementation
  gotchas. The "I would have spent two hours on this without the prior
  context" file.

When a resolution teaches something a future session needs that this file
doesn't already say — a new convention, a non-obvious gotcha, a vocabulary
distinction, a debug technique, an "adding X touches N sites" recipe — add a
terse declarative bullet to the relevant section. Don't record specific bug
details (those live in the kata issue + commit). One-time decisions don't
belong here either.

Code is the source of truth. `docs/*.adoc` and `docs/adr/` are partly stale
(see *docs/ drift* below); when they disagree with the code, the code wins
and the doc gets fixed or marked historical. References use
`path::Symbol` rather than line numbers so they survive edits.

Cross-references like `kata #N` resolve via `kata show N` (project
`clusterless`). RDRs live in-repo under `docs/rdr/` (index + Status table in
`docs/rdr/README.md`); ADRs 0001–0005 under `docs/adr/` are the pre-RDR
design history.

---

## Project conventions

### Process and repo

- **Kata is the source of truth for issues.** `kata list`, `kata show <N>`,
  `kata ready` are the discovery surface. Resolutions close the kata with a
  comment naming the resolving short hash. The old `.beads/` tracker is
  retired — never file there.
- **Final/Implemented RDRs are frozen.** Never amend them (Draft RDRs are
  editable — check the `Status:` line first). Divergence goes in a side file
  in the RDR's directory (`docs/rdr/NNNN-slug/`): `deviations.md` for
  implementation-time friction (written by Stage 8 only), `revisited.md` for
  post-use direction changes (always cite a kata). A divergence below the
  record's resolution (a message, a default) needs neither — the kata comment
  and commit are its record.
- **RDRs and kata ids are internal.** They never appear in user-facing
  output: error messages, hints, help text, `cls show` descriptions, or
  `docs/`. Refer to the principle or capability in plain prose; pin the
  RDR/kata reference in a code comment.
- **Commit style: Conventional Commits**, enforced by `.githooks/commit-msg`
  (`git config core.hooksPath .githooks` once per clone). Types: `feat fix
  docs refactor test chore build ci perf revert`; scope optional and maps to
  the module or area (`cli`, `model`, `kernel`, `construct`, `lambda`,
  `build`, `deps`, `rdr`); description lowercase, imperative, no trailing
  period. No `Co-Authored-By` / `Claude-*` trailers — authorship is the
  Author header. Every pushed commit must be signed (`.githooks/pre-push`).
- **Implemented-RDR feat subject:**
  `feat(<scope>): implement RDR NNNN <slug>: <one-line description>`.
  `git log | grep "implement RDR"` enumerates landed RDRs; diverging subjects
  break that grep. RDR-only commits use `docs(rdr): …` (roborev skips them).
- **Only the version branch publishes.** `.github/workflows/wip.yml` runs
  `check` on every `wip-*` push (except `*-scenarios`). Only
  `wip-<clusterless.release.major>` (today `wip-1.0`, read from
  `version.properties`) goes on to the real-AWS scenarios, the jreleaser
  release (which overwrites the `wip-<major>` release), the Homebrew publish,
  and the Netlify docs deploy. Sub-wip branches (`wip-1.0-<topic>`) are
  test-only; to run the AWS scenarios on one without releasing, dispatch
  `gh workflow run wip.yml --ref <branch> -f scenarios=true` (us-west-2,
  after `check`), or push `<branch>-scenarios` (`scenario.yml`, us-east-2,
  scenarios only).
- **NEVER `git stash` in a worktree.** The stash stack lives in the shared
  common `.git` dir and is global across parallel flights; a pop can restore
  a foreign session's stash and make a red/green check report a false GREEN.
  To revert a file for a red/green check:
  `cp <path> /tmp/save && git show <rev>:<path> > <path>` … run …
  `cp /tmp/save <path>`, then `cmp`.
- **Scratch lives in `_*` directories** (gitignored by `_*`). Don't
  `git add -f` them unless asked.

### Architecture: how a command actually runs

- **Three parses, two processes.** `cls` (`clusterless-main`
  `clusterless.cls.Main`) parses argv only to validate it, then hands the
  **raw argv** in-process to each `SubstrateProvider.execute(args)`
  (ServiceLoader). The AWS `Kernel` re-parses with its own picocli tree and
  does the real work in a command of the **same name**. CDK-driven verbs
  then spawn `cdk --app "<cls-aws> -D… synth …"`, which re-enters the Kernel
  a third time through the hidden `synth` command
  (`kernel …/cdk/CDKProcessExec`, `…/cdk/lifecycle/Synth`).
- **Main-side command classes only validate.** Behaviour lives in the
  Kernel. The shared option classes live in `clusterless-main-common`
  `command/**` and are `@Mixin`'d on both sides, so an option added there
  appears on both parsers.
- **Kernel-only options are unreachable from `cls`.** Options declared on a
  Kernel mixin (e.g. `CDKProcessExec`'s `--cdk`, `--cdk-app`,
  `--use-localstack`, `--output-path`) are rejected by Main's parser. Either
  put the option on the shared options class or document `cls-aws` as the
  escape hatch — don't add a Kernel-only option and expect `cls` to accept it.
- **State crosses into the synth child only via env + `-D`.** Env vars
  `CLS_CDK_COMMAND`, `CLS_CDK_OUTPUT_PATH`, `CLS_CDK_PROFILE` are set on the
  **child's** environment; merged config is re-serialized as
  `-D namespace.key=value`. Code running in the parent (e.g. the metadata
  push after `cdk deploy`) cannot read `CLS_CDK_*` — pass values explicitly.
- **Arguments are joined into a shell string.** `--app` is space-joined and
  project lists split on `,`: paths or `-D` values containing spaces or
  commas break. Quote-safe construction is required for any new pass-through.
- **Project verbs pick providers from `/placement/provider`** in the project
  files; all other verbs run every provider selected by `-P`.
- **Stdin `-p -` survives re-parsing only via `replaceWithTemp`**, which
  matches the exact tokens `-p`/`--project` followed by `-`. Any new way of
  naming stdin must go through the same rewrite.
- **Two classes named `CommonCommandOptions`.** `clusterless.cls.command.CommonCommandOptions`
  is the empty dispatch marker used by `Main.run`/`CommandWrapper`;
  `clusterless.cls.command.common.CommonCommandOptions` is the abstract base
  for report commands. Check the import.

### CLI / DX contract

Adopted from the retrofit CLI. Existing code does not yet meet all of it;
**new and touched code must**, and gaps are filed as kata rather than
copied.

- **Never silent.** Every failure reaches the user on stderr with a non-zero
  exit — independent of verbosity. Logging is OFF at verbosity 0
  (`Verbosity::setLoggingLevel`), so a failure reported only through
  `LOG.error` is invisible by default. Log for diagnosis; *report* for the
  user.
- **stdout is data, stderr is everything else.** Logs, progress, prompts,
  advisories, and errors go to stderr. Today `logback.xml`'s console
  appender targets stdout, so `-v --output json` interleaves log lines with
  JSON — don't build on that; fix it when touched.
- **One output gateway.** Tabular/record output goes through
  `Reporter.instance(printer, RecordClass)` and honours the global
  `--output {table,json,csv,tsv}`. Don't `System.out.println` from a command
  (`local` and `config show` are the known exceptions to retire). JSON output
  is a stable machine contract: adding a field is safe, renaming or removing
  one is a breaking change.
- **Exit codes are part of the contract.** Parse errors exit 2
  (`ParameterExceptionHandler`); `ExitCodeException` carries its own code;
  `FileNotFoundException` → 74; anything else → 70
  (`ExitCodeExceptionMapper`). Throw `ExitCodeException` for a deliberate
  code instead of `System.exit`.
- **Errors state what, where, and what next.** Name the offending resource
  (file, project, stack, bucket) and give a recovery action where one exists
  (`Bootstrap`'s "try: cdk bootstrap…" is the model). Wrap external failures
  (missing `cdk` binary, missing credentials, access denied) into a message
  that names the cause — never surface a bare `IOException`/stack trace
  without `-v`, and never map "access denied" or "no credentials" to a
  different diagnosis ("must bootstrap", "no state").
- **Option naming.** Long options are kebab-case. One name, one meaning
  across commands — `--project`, `-o`, and `--dry-run` currently mean
  different things in different commands; don't add more overloads. Global
  options that must work after the subcommand need `scope = INHERIT`.
  Tri-state flags use `Optional<Boolean>` with `fallbackValue = "true",
  arity = "0..1"`.
- **Help is generated; hand-written snapshots rot.** CLI reference docs come
  from picocli `ManPageGenerator` (`:clusterless-main:generateDocs`);
  `cls show` docs come from `@ProvidesComponent`/`@DocumentsModel`
  annotations and `templates/*.hbs`. Fix help text at the annotation, not in
  a copy. The README help snapshot is hand-maintained and stale.
- **Config precedence:** `-D ns.key=value` > nearest `.clsconfig[-ns]`
  walking up from cwd (first hit only) > `~/.cls/config[-ns]`; all TOML;
  merge is shallow (a nested table in a higher layer replaces the whole
  table). There is no env-var layer. `-D` has no `INHERIT` scope — it must
  precede the subcommand.

### Model and project JSON

- **Field-based Jackson binding.** `Struct`/`Config` use
  `@JsonAutoDetect(fieldVisibility = ANY)`; fields are package-private or
  protected with no setters; accessors are record-style (`name()`).
  `Model.toString()` is final and renders JSON.
- **Defaults live in field initializers** (applied at parse); collections
  start empty, never null. An explicit JSON `null` overwrites a default (no
  `Nulls.SKIP`). Implied values that depend on other projects (e.g. a
  `SourceDataset`'s location from its owning `SinkDataset`) are resolved at
  synth time in `DatasetResolver`, and fail loudly when missing/ambiguous.
- **Polymorphism is `Extensible` only**: `@JsonTypeInfo(Id.NAME,
  EXISTING_PROPERTY "type")` resolved by `ExtensibleResolver` against the
  `ComponentServices` registry. Type ids are `aws:core:<camelName>` and are
  positional — `Exportable` splits on `:` to derive the export namespace.
- **`Extensible.equals/hashCode` are final identity** and load-bearing:
  models are keys in `LinkedHashMap<Extensible, …>` (`ComponentServices`,
  `Lifecycle`). Value types (`Dataset`, `Project`, `Placement`) use
  `getClass()` equality; use `Dataset.sameDataset` for cross-subtype
  comparison.
- **`@JsonRequiredProperty` is documentation, not validation.** Jackson only
  enforces `required` on creator properties; these are fields. It drives the
  `Views.Required` template output only. A missing required field binds as
  `null` — validate explicitly where it matters.
- **Unknown properties are rejected everywhere** (Jackson default; no
  `ignoreUnknown`). Project "JSON" accepts `//` and `#` comments.
- **Reuse, don't re-invent:** `URIs` (normalize/asKey/asKeyPath/copyAppend),
  `Lazy`, `Annotations.find`, `Env.toEnv/fromEnv`, `JSONUtil.*Safe`, `Label`
  for every derived name.

### Identity and persisted formats — changes here break deployed users

None of the persisted formats carries a schema version, and readers are
strict. Treat everything below as a wire contract; changing it needs an RDR
and a migration story.

`SynthIdentitySnapshotTest` (kernel module) pins the synthesized identity of
the scenario projects and the bootstrap stack — stack names, logical ids and
types, physical names, handlers, props env keys, export names — against
`src/test/resources/snapshot/synth-identity.json`. A failure there is a
deployed-resource replacement or orphan; regenerate with
`CLS_SNAPSHOT_UPDATE=true` only for a deliberate change, and review the
snapshot diff in the same commit. When a scenario changes, re-render its
fixture under `snapshot/projects/` (jsonnet, stage `test`, account
`000000000000`, region `us-west-2`).

- **Stack names** `{stage}-{project}-{baseId}-{version}-{region}`
  (`Stacks`): grouped stack baseId `ResourceActivityBoundary` (from
  `StackGroups`), arc stacks `Arc<name>`. Renaming a `ModelType`, a stack
  grouping, an arc, or the project name/version orphans deployed stacks
  (precedent: adding Activity renamed `ResourceBoundary`).
- **Construct ids are CloudFormation logical ids**: `model.label()` +
  discriminator (`ExtensibleConstruct::uniqueId`). Renaming a model class, a
  model `name`, or a discriminator replaces the resource; named buckets are
  RETAIN, so a rename orphans or collides.
- **Physical names** are built only through the `construct-common`
  `resources/*` helpers (`Functions` ≤64 chars, `Rules`, `Queues`, `Arcs`)
  from stage/project/name/version/region. State-machine names are
  load-bearing: `arcs exec` rebuilds the ARN from the stored name.
- **Bootstrap contract:** stack `[stage-]clusterless-bootstrap`, exports
  named via `Ref` with scope `bootstrap`, `BOOTSTRAP_VERSION = "1"`,
  imported by every project stack with `Fn.importValue`. Bumping the version
  or renaming a ref breaks every deployed project (CloudFormation refuses to
  drop imported exports). The bootstrap VPC (10.14.0.0/16) is looked up by
  name; any change replaces it. Bootstrap state buckets are
  DESTROY + autoDeleteObjects — destroying bootstrap wipes all state.
- **Bootstrap store buckets** `[stage-]clusterless-{metadata|arc-state|manifest}-{account}-{region}`
  (`Stores`, `StateStore`) and their S3 key layouts (`clusterless-substrate`
  `uri/*URI`: `projects/`, `datasets/`, `arcs/`, `materials/`, arc state
  `….arc`, manifests `…/state=X[/attempt=N]/manifest.json`) are read back by
  reporters, `arcs exec`, dataset resolution, and running lambdas.
- **Enum constant names are persisted** (`ArcState`, `ManifestState`,
  `UriType`). Don't rename them.
- **Published `project.json` embeds type ids.** Removing a component type
  makes old published projects unreadable.
- **Lambda props contract:** constructs write `CLS_<SimpleClassName>_JSON`
  and `CLS_<SimpleClassName>_JAVA`; runtimes read `_JAVA`, whose `__type`
  carries the fully-qualified class name. Renaming or moving a props class
  breaks deployed lambdas until redeployed.
- **Handler class names and asset zips are string contracts.** Constructs
  name handlers by string literal and find zips by filename regex tied to
  Gradle module names (`Assets`). Nothing checks them at compile time — a
  rename fails at deploy/runtime only.
- **Adding a field to a persisted object breaks older readers** (running
  lambdas, older CLIs) because unknown properties are rejected. Either
  deploy CLI + lambdas together, or first add read-tolerance
  (`@JsonIgnoreProperties(ignoreUnknown = true)`) to that type in its own
  release.

### AWS substrate

- **Component registration is ServiceLoader.** Providers carry
  `@ProvidesComponent(type, synopsis, description)`; service interfaces carry
  `@DeclaresComponent(provides, isolation)`; register in
  `clusterless-substrate-aws-construct-core/src/main/resources/META-INF/services/clusterless.cls.managed.component.ComponentService`.
  Duplicate type ids are silently last-wins — grep before choosing one.
- **Extend the typed construct base** (`ResourceConstruct`,
  `Ingress/EgressBoundaryConstruct`, `ActivityConstruct`, `ArcConstruct`),
  not `ExtensibleConstruct` directly — `Lifecycle` wires dependencies and
  registers constructs by those types.
- **Exports:** `exportArnRefFor(model(), <L2 construct>, …)` — pass the L2,
  not the wrapper; a same-app `importArnRef` returns the local construct via
  unchecked cast.
- **SDK wrappers return `Response`, never throw.** Callers must call
  `isSuccessOrThrow`/`isSuccessOrThrowRuntime` (or deliberately
  `isSuccessOrLog`). A wrapper call whose `Response` is ignored is a silent
  failure. Batch APIs that report partial failure in-band (e.g. EventBridge
  `FailedEntryCount`) must be checked explicitly.
- **Every SDK client takes profile *and* region from the command.** Region
  falls back to `AWS_REGION`/`AWS_DEFAULT_REGION` and profile to
  `AWS_PROFILE` only when not supplied; new code passes both from the
  command's placement/options.
- **List APIs paginate.** Use the paginator and consume it before checking
  errors; a single 1000-key page is not a listing.
- **CDK construction errors** (`JsiiException`) are rewrapped as
  `IllegalStateException` by `ErrorsUtil`.
- **`Vpc.fromLookup` writes `cdk.context.json` in cwd.** The committed root
  `cdk.context.json` pins a real account; don't regenerate it casually.

### Lambdas

- **Handler hierarchy:** `StreamHandler` → `EventHandler` (void, writes JSON
  `null`); `StreamResultHandler` → `EventResultHandler` → `ArcEventHandler`
  (returns a map for Step Functions). Each stage logs and rethrows; each
  handler has an observer interface used for logging and test verification.
- **AWS-owned event payloads are read tolerantly.** EventBridge/S3/scheduled
  event models are generated from AWS schemas in the transform module
  (`src/main/json/*.json`, openapi-generator) with
  `@JsonIgnoreProperties(ignoreUnknown = true)`: AWS adds fields without notice
  (S3 "Object Created" gained `event-version`, and every infrequent put
  listener failed until this was tolerated). This is the opposite of our own
  persisted types, which stay strict. Any new AWS event model gets the same
  generator option; `AWSEventCompatibilityTest` holds a current-shape sample.
- **`*-model` modules hold props/payloads shared with constructs.** They must
  stay free of CDK and the Lambda runtime. (They do depend on
  `clusterless-substrate-aws-common`, hence the AWS SDK.)
- **Failures surface through Lambda retries / Step Functions catch only** —
  there are no DLQs or on-failure destinations. A swallowed exception in a
  handler is data loss, not a retry.
- **Logging:** SLF4J API everywhere; Log4j2 with the AWS Lambda appender in
  lambdas (`AWS_LAMBDA_LOG_FORMAT`/`AWS_LAMBDA_LOG_LEVEL`), Logback in the
  CLI.

### Build and test

- **Toolchain:** Gradle wrapper 8.14.5 (checksum-pinned), Java 17 toolchain (foojay resolver),
  Node.js (CDK's jsii runs Node even for in-JVM synth tests). First build
  needs network.
- **Dependency versions live in one place:** the `constraints {}` block of
  `build-logic/…/clusterless.java-common-conventions.gradle.kts`. Modules
  declare dependencies unversioned. Exceptions pinned inline: CDK
  (`construct-common/build.gradle.kts`, glue-alpha pinned to the same
  version), the scenario module's Conductor/Spring Boot, and plugin versions.
- **Verify ladder, cheapest first:**
  - `./gradlew compileJava compileTestJava` — compile (transform module
    generates OpenAPI models first).
  - `./gradlew :<module>:test --tests '<Pattern>'` — one module/test.
  - `./gradlew test` — all unit tests; no Docker, needs Node.
  - `./gradlew integrationTest` — LocalStack via Testcontainers; **needs
    Docker** (only the lambda-{arc,transform,workload} modules have sources).
  - `./gradlew check` / `build` — include `integrationTest`, so they need
    Docker too.
  - `./gradlew installDist` — runnable CLI at
    `clusterless-main/build/install/clusterless/bin/cls`.
  - `./gradlew scenarios` — **deploys real stacks**; needs AWS credentials,
    `cdk` on PATH, and gitignored root `gradle.properties` keys
    `scenario.stage`, `scenario.aws.account`, `scenario.aws.region`. Never in
    the default lane.
- **Tests:** unit `*Test` under `src/test`; handler integration tests under
  `src/integrationTest`, extend `LocalStackBase`, mostly `@TestFactory`;
  shared fixtures in lambda-common `testFixtures` (`TestArcs`,
  `TestDatasets`, `TestLots`, `BootstrapMachine`). **One test per
  `LocalStackBase` class:** its bootstrap recreates the store buckets before
  each test, so a second test in the same class fails with
  `BucketAlreadyOwnedByYouException` — add a new class instead. Kernel synth is covered by
  `KernelTest` (fake account, `CLS_ASSETS_PATH`). Testcontainers fails loudly
  without Docker — keep it that way; a test that skips when its environment
  is missing looks green while proving nothing.
- **Test coverage is thin where DX lives.** `CLITest` is empty; parse and
  exit-code behaviour is untested. Any CLI change adds a test that runs the
  real `Main` entry path and asserts stdout, stderr, and exit code.
- **Style:** no formatter or linter is configured; match surrounding code.
  Nullability via `org.jetbrains:annotations`. MPL-2.0 header on every file
  (copy an existing one). No `module-info.java`.

### Modernization hazards (this branch's purpose)

- **Java 17 appears in three places** — toolchain, CI, and Lambda
  `Runtime.JAVA_17` (`Functions`). Move them together.
- **Gradle 9 blockers:** eager cross-project task
  lookups (`tasks.getByPath`/`findByPath`) and `project.ext` reads in build
  scripts break configuration cache / isolated projects.
- **Jackson typed wire format:** `__type` class names in lambda env vars mean
  package renames and a Jackson 3 migration change the deployed contract.
- **CDK feature flags are not applied** (no `cdk.json`; the shipped
  `etc/context.json` is unused). Enabling recommended flags can change
  logical ids — `cdk diff` against a deployed stack before adopting any.
- **LocalStack stays pinned at `localstack/localstack:3.0.2` — do not bump
  it.** Since 2026-03-23 every current LocalStack image requires an account
  auth token and the free plan is non-commercial only (no Glue); pinned prior
  tags still run without a token. Replacing it with moto server is deferred
  until the modernization is stable (kata 6eta).
- **Scenario module is end-of-life stack** (Spring Boot 2.7, Conductor 3.14,
  Nashorn, slf4j 1.7 pins) and blocks logging/Java upgrades there.
- **External `io.clusterless:clusterless-commons-*`** (naming, `Ref`,
  `ScopedApp`/`ScopedStack`) must be upgraded in lockstep; its source lives
  in the sibling `commons` repo.

### Declared-but-unwired scaffolding

These exist in the model/CLI but have no working path. Don't build on them
without an RDR, and don't "fix" them piecemeal:

- `Barrier` (abstract, no provider — a non-empty `barriers` array fails to
  parse), `Device` components, `Isolation.independent`,
  `ManagedNestedStack`.
- `ImportCommand` (hidden) and the Kernel `Import` (targets the retired
  `ResourceBoundary` stack).
- `graal/` (empty; native-image was abandoned).

### docs/ authoring

- **Standalone.** No links outside the repo, into gitignored `_*` folders, or
  to sibling-repo support material. Pull deferred substance inline.
- **No `kata` anywhere in `docs/`** — reword to "issue".
  `grep -rni kata docs/` must come back empty (excluding `docs/rdr/`).
- **RDR provenance:** short inline `RDR NNNN` cites only; no RDR file links
  from user docs.
- **Intended capability, never current defects.** Write the doc as if an
  open issue were already shipped; the defect goes to a kata.
- **docs/ drift:** `docs/CommandLineUsage.adoc` is a "Proposed" design that
  lists commands that don't exist; `docs/URIFormats.adoc`, ADR 0003's bucket
  and key layouts, and `ApplicationArchitecture.adoc`'s Barrier/per-boundary
  stacks predate the code. Verify against code before citing any of them.

---

## Resolution patterns

### Workflow

- **Build the CLI once and reproduce verbatim.** `./gradlew installDist`,
  then run the issue's `cls …` reproducer from a temp dir and diff against
  the issue's embedded output. Drift is the fastest tell that an issue's
  framing is stale.
- **Check the Kernel side, not just Main.** A command's Main class only
  validates; the behaviour (and most bugs) live in the same-named Kernel
  command. Read both before concluding.
- **Synth before deploy.** `cls verify` (`cdk synth`) or a `KernelTest`-style
  case shows the CloudFormation a change produces. For anything touching
  names, construct ids, CDK, or dependency upgrades, run
  `./gradlew :clusterless-substrate-aws-kernel:test --tests '*SynthIdentitySnapshotTest'`
  — a changed id is a resource replacement.
- **Run the AWS scenarios at milestones, not per commit.** Dispatch them on the
  working branch after a runtime-affecting change lands (dependency layers,
  CDK, Java) and before merging to the version branch.
- **Probe, then delete.** For "what does CDK/jsii/Jackson actually do with
  X", write a throwaway test that logs the value, run it, ground-truth the
  assumption, and delete it before committing.
- **Look up idioms before inventing a shape.** Docs/standards →
  `arc search` on `DevRef` / `CodeMaintenance`. No Java/CDK code corpus
  exists yet; read dependency source from the Gradle cache
  (`~/.gradle/caches/modules-2/files-2.1/`) instead of guessing API shape.
- **Skipped tests as invariant docs.** When an invariant can't be fixed in
  this commit, add the test now with `@Disabled("blocked by kata #N")`; the
  resolving commit removes the annotation.
- **Issue-number comments are the exception.** `kata #N` in code is fine
  only when the kata documents an invariant the reader needs.

### Triage filters

- **Classify the target before designing a defense.** Deployed AWS
  resources and persisted state (buckets, stacks, manifests, arc state) are
  the assets; the project JSON is a developer artifact recoverable via git.
  Gates belong at the deploy/destroy boundary, not at project-file parsing.
- **"Rename X" is an identity change.** Any issue that renames a model
  class, type id, stack grouping, enum constant, props class, or handler
  touches a deployed contract (see *Identity and persisted formats*).
  Resolve with an RDR and a migration, or decline.
- **Multi-claim issues have per-claim merit.** Check each sub-claim against
  the code; resolve the real one narrowly and record refutations in the
  kata comment.
- **"It works with `cls-aws` but not `cls`"** is almost always the
  Main/Kernel option split or argv re-parse — check which parser rejects it.
- **"Silently did nothing"** — first check verbosity (logging OFF at 0), an
  ignored `Response`, or a provider/placement filter (`Loader` drops project
  files whose `/placement/provider` doesn't match; `construct()` skips models
  with no provider).
- **"Wrong account/region"** — check whether the code path runs in the
  parent or the synth child (env vars only exist in the child) and whether
  the SDK client was given the command's profile and region.
- **Dead scaffolding: prefer delete over implement** when the issue concedes
  the wired version would never fire. Verify consumers tolerate the removal.
- **Fixing a bug can unmask a compensating one.** File the unmasked defect
  separately and note the interaction in both.

### Implementation patterns

- **Adding a component of an existing kind** (resource/boundary/activity/arc)
  touches ~7 sites: model class (its simple name becomes a permanent
  construct-id prefix); construct extending the typed base; provider with
  `@ProvidesComponent(type = "aws:core:<camelName>")` (+ `executor()` for
  arcs/activities); the META-INF/services line; names via `resources/*`
  helpers; if a lambda is involved, handler + props class in the `*-model`
  module + literal handler string + asset regex; a `KernelTest` synth case
  and a scenario under `clusterless-scenario/src/main/cls/scenarios/`.
  `cls show` docs come from the annotations.
- **Adding a new model kind (`ModelType`)** touches ~9 sites: the enum,
  base model, `XComponent`, `XComponentService` with `@DeclaresComponent`,
  `Deployable` list field + equals/hashCode,
  `ComponentServices.getExtensibleModelsFor`, `StackGroups` (changes stack
  names — RDR required), `Lifecycle` wiring + `lookupModel`, a `cls show`
  subcommand (and `DatasetResolver` if it owns datasets).
- **Adding a provider-dispatched CLI command** touches 5 sites: options
  class in `clusterless-main-common` `command/<group>/`; wrapper
  `XCommand extends CommandWrapper<XOptions>` in `clusterless-main`;
  registration in `Main` subcommands; a Kernel command with the **identical
  name** mixing in the same options; registration in `Kernel` subcommands.
  Nested subcommands: static inner class with `@ParentCommand` on both
  sides. Add a `CDKCommand` value if it shells out to cdk.
- **Adding a `cls show` element type:** a `ShowComponents`/`BaseShowElements`
  subclass, `ShowCommand` registration, `templates/<type>-cli.hbs` and
  `-adoc.hbs`, and a `generate*` task in `generateDocs`.
- **Adding a lambda module** touches ~7 sites: `settings.gradle.kts`; the
  module build with the `packageAll` Zip + `build.dependsOn`; its `-model`
  module + include; the kernel `distributions` block; construct-core's
  dependency on the model; the `Assets` regex in the construct;
  `testImplementation(testFixtures(":clusterless-substrate-aws-lambda-common"))`.
- **Adding a dependency:** a constraint in the conventions plugin (use
  `implementationAndTestFixture` if fixtures need it) + the unversioned
  declaration in the module.
- **Bumping CDK:** `construct-common/build.gradle.kts` (lib + glue-alpha
  together) and the `npm aws-cdk@` version in both workflows; synth-diff
  before landing.
- **Adding a field to a persisted type:** see *Identity and persisted
  formats* — add read-tolerance first or ship CLI + lambdas together, and
  never make an existing field required after the fact.
- **Switching a component's strategy can collide on physical names.**
  Strategies that reuse a physical name under a new logical id (e.g. the
  frequent/infrequent s3PutListener lambda and rule) make CloudFormation
  create-before-delete fail on the name. Change the name or deploy in two
  steps.
