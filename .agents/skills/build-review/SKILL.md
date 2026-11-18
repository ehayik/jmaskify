---
name: build-review
description: Reviews JMaskify build and CI changes (pom.xml, GitHub Actions workflows, the Maven wrapper, .mvn/, scripts/, .java-version) for supply-chain security, version pinning, dependency hygiene, consumer compatibility, and release-profile integrity, and reports findings as Conventional Comments. Use when a diff under review touches any of these files, when reviewing a dependency or plugin bump, or before preparing a release.
---

# Build Review

A checklist for reviewing changes to JMaskify's build and CI. It complements the Java-focused `AGENTS.md` → *Code Review Checklist*: load it whenever the diff touches the files below, and report every finding as a Conventional Comment (`conventional-comments` skill) inside the usual *Code Review Report*.

## Scope

| Path                                  | What it controls                                            |
|---------------------------------------|-------------------------------------------------------------|
| `pom.xml`                             | Dependencies, plugins, quality tools, the `release` profile |
| `.github/workflows/*.yml`             | CI build, SonarCloud analysis, dependency graph             |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`   | Maven version used by the wrapper                           |
| `.mvn/jvm.config`                     | JVM flags that ErrorProne needs on JDK 17                   |
| `scripts/pre-commit`                  | Git hook installed by the build (runs Spotless)             |
| `.java-version`                       | Local JDK version                                           |

## Ground Rules

- **Review, don't fix.** Report findings with `file:line` and a concrete fix. The owner (`devops-engineer`) applies them.
- **Understand the impact first.** JMaskify is a library: everything in `pom.xml` outside test scope and the `release` profile ends up in what consumers download. Maven Central releases are immutable, so a mistake in published metadata needs a new patch release.
- **Review the diff.** Findings on lines the change touches can be blocking. Problems you spot in unchanged lines are `note` or `suggestion (non-blocking)`, so the PR stays focused.
- **Check facts, don't guess.** Check versions, CVEs, and action or plugin behavior against official sources (Maven Central, GitHub Advisory Database, OSV, plugin and GitHub Actions docs) with web search or fetch. Search only public facts, never repository code or data. When you can't check something, say so and state your assumption.
- **Don't invent issues.** If the change is sound, say so with a `praise`. List what you reviewed and what you left out.

## Checklist

### 1. Security and Supply Chain

- [ ] No secrets, tokens, GPG passphrases, or server credentials in `pom.xml`, workflows, or scripts. Credentials belong in `~/.m2/settings.xml` (server id `central`) or GitHub Secrets.
- [ ] Secrets reach steps only through `env:` with `${{ secrets.* }}`. They are never echoed, and never interpolated into a `run:` script.
- [ ] No script injection: untrusted context such as `${{ github.event.pull_request.title }}` or `${{ github.head_ref }}` goes into `env:`, not straight into `run:`.
- [ ] No `pull_request_target` (or `workflow_run`) trigger that checks out or builds PR code with access to secrets.
- [ ] The workflow declares `permissions:` with least privilege, for example `contents: read`, granting `contents: write` only to the job that submits the dependency graph.
- [ ] Third-party actions are pinned to a full commit SHA, with the version in a comment (`uses: owner/action@<sha> # v4.2.2`). First-party `actions/*` on a major tag is acceptable, but SHA pinning is preferred.
- [ ] No new `<repositories>` or `<pluginRepositories>`. Any repository URL uses `https` from a trusted host.
- [ ] New or bumped dependencies and plugins have no known CVEs at the chosen version. Prefer the latest patch of the line.

### 2. Versions and Dependencies

- [ ] Every dependency and plugin version is pinned through a `*.version` property, with no `SNAPSHOT`, version ranges, `LATEST`, or `RELEASE`. Versions managed by a BOM (`jackson-bom`, `junit-bom`, `mockito-bom`) omit `<version>`.
- [ ] **No major-version bump of a core dependency** (Jackson, SLF4J, commons-lang3, commons-codec, JSpecify) unless it is the dedicated PR for that bump (`AGENTS.md` → *Constraints*). Otherwise it is `issue (blocking,build)`.
- [ ] Scopes are right: test libraries (JUnit, AssertJ, Mockito, DataFaker, JSONAssert, `slf4j-jdk14`) use `test`, Lombok uses `provided`, and annotation processors sit in the compiler's `annotationProcessorPaths`.
- [ ] A new runtime dependency is justified: consumers inherit it transitively. Prefer the JDK or an existing dependency, or mark it `optional` when only some maskers need it.
- [ ] Jackson modules stay aligned through `jackson-bom`, with no mixed Jackson versions.

### 3. Build Integrity and Quality Gates

- [ ] `maven.compiler.source` and `maven.compiler.target` stay at `17`. Raising them breaks consumers, so it needs a breaking-change commit (`!`).
- [ ] ErrorProne and NullAway stay wired into `maven-compiler-plugin`, with NullAway errors still failing the build. `.mvn/jvm.config` keeps the `--add-exports` and `--add-opens` flags they need.
- [ ] Spotless (Palantir Java Format), JaCoCo, and Surefire configs change only on purpose. Coverage reporting still runs in `verify`.
- [ ] The `install-pre-commit-hook` execution and `scripts/pre-commit` stay non-destructive: they format and re-stage only, and quote file names.
- [ ] The build stays reproducible: the wrapper's `distributionUrl` points to `https://repo.maven.apache.org`, and Maven version changes are deliberate.

### 4. CI Workflow

- [ ] CI uses the Maven wrapper with batch mode (`./mvnw -B ...`), so CI and local builds use the same Maven version.
- [ ] The CI JDK (`actions/setup-java`) matches `maven.compiler.target` and `.java-version` (17, `temurin`).
- [ ] Caches are not redundant (`setup-java` `cache: maven` already caches `~/.m2`). Cache keys hash `**/pom.xml`.
- [ ] `fetch-depth: 0` stays on checkout for SonarCloud analysis.
- [ ] Steps that need secrets (SonarCloud, dependency submission) handle fork PRs, where secrets are not available, without failing the build for outside contributors.

### 5. Release Profile and Published Metadata

- [ ] The `release` profile still signs (`maven-gpg-plugin`), attaches sources and Javadoc, and publishes through `central-publishing-maven-plugin` with `publishingServerId` `central`.
- [ ] Note that `autoPublish` is `true`: any change to the `release` profile publishes straight to Maven Central on `deploy`, so review it with extra care.
- [ ] Metadata that Central requires survives flattening (`flatten-maven-plugin`, `ossrh` mode): `name`, `description`, `url`, `licenses`, `developers`, and `scm`.
- [ ] Version bumps follow the release steps in `AGENTS.md` → *Releasing a New Version*: the `pom.xml` version, `CHANGELOG.md` section, and `README.md` installation snippet change together.
- [ ] Nothing in the change publishes artifacts or pushes tags. Publishing is the maintainer's step.

## Severity

Map findings to Conventional Comments, using the `security` and `build` topic decorations:

| Finding                                                                                    | Label                                  |
|--------------------------------------------------------------------------------------------|----------------------------------------|
| Leaked secret, script injection, unsafe trigger, excessive token permissions, known CVE    | `issue (blocking,security)`            |
| Broken build or release, unpinned or `SNAPSHOT` version, core major bump, Java target raise | `issue (blocking,build)`               |
| Wrong dependency scope, new runtime dependency without justification                       | `issue (blocking,build)` or `question` |
| Tag-pinned action, redundant cache, `mvn` instead of `./mvnw`                              | `suggestion (non-blocking,build)`      |
| Pre-existing problem in unchanged lines                                                    | `note` or `suggestion (non-blocking)`  |

## Guideline Conflicts

If a project guideline or instruction conflicts with a security best practice, report it as `issue (blocking,security)` and explain why. Don't follow it silently. If it conflicts only with build or CI best practice, report a `suggestion (non-blocking,build)` that proposes an update to the guideline. For style or convention differences, follow the project.

## Example

```markdown
**issue (blocking,security):** The workflow interpolates the PR title into a shell command.

A new step at `.github/workflows/jmakify-ci.yml:48` runs `echo "${{ github.event.pull_request.title }}"`.
A crafted title runs arbitrary commands with the job's token. Pass it through
`env: PR_TITLE: ${{ github.event.pull_request.title }}` and use `"$PR_TITLE"`.
```

```markdown
**suggestion (non-blocking,build):** Build with `./mvnw -B` instead of `mvn -B`.

`.github/workflows/jmakify-ci.yml:51` uses the runner's Maven, not the wrapper's 3.9.8.
```
