---
description: "Prepare a JMaskify release: run pre-flight checks and a dry-run build, hand off the Maven Central publish to the maintainer, then tag and draft the GitHub release"
---

# Release Preparation

Prepare the release of JMaskify for Maven Central. Requested version: `$ARGUMENTS`.

Execute each task in the order given. If a task fails, stop, report what failed and how to fix it, and do not continue until the maintainer confirms. (Task 2 runs all its checks before reporting; the task fails if any check fails.)

## Guardrails ⛔

- **Never publish.** Do not run `deploy`, `central-publishing:publish`, or anything else that uploads artifacts. Publishing is done by the maintainer only (see `AGENTS.md` → *Constraints*). In Claude Code, deny rules in `.claude/settings.json` also block these commands. Maven Central releases are immutable: a published version can never be deleted or overwritten.
- **Never expose secrets.** Do not print `~/.m2/settings.xml` contents, tokens, or GPG passphrases. Only check that the required entries exist.
- **Never run `gpg` commands.** Most of them auto-start `gpg-agent`. If it starts inside a sandboxed agent shell, it cannot show the passphrase prompt (pinentry), and the maintainer's later `deploy` fails with `gpg: signing failed: No pinentry`. If that happens, the maintainer fixes it by running `gpgconf --kill gpg-agent` in their own terminal.
- **Never rewrite history or tags.** No force-pushes, and no moving or deleting existing tags.
- Ask for confirmation before any `git push` or `gh release create`.

## Shell Conventions 🐚

Shell variables do not survive between tool calls. Therefore:

- Run each code block below as a **single** shell command.
- Replace the `X.Y.Z` and `<sha>` placeholders at the top of each block with the literal values resolved in Tasks 1 and 2.
- Keep the `${VAR:?}` guard lines: they abort the block if a value is missing, instead of letting checks pass on empty input.

## Task 1: Resolve the Version 🏷️

If a version was requested above, use it. Otherwise read it from `pom.xml` and confirm it with the maintainer:

```bash
./mvnw -B -q help:evaluate -Dexpression=project.version -DforceStdout
```

Validate the resolved version:

```bash
VERSION="X.Y.Z"
: "${VERSION:?VERSION is not set}"
[[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] && echo "format: ok" || echo "format: FAIL (expected X.Y.Z, no qualifiers)"
grep -qF "<version>$VERSION</version>" pom.xml && echo "pom.xml: ok" || echo "pom.xml: FAIL (version mismatch)"
```

## Task 2: Pre-flight Checks ✈️

Run every check below and collect the results. Don't stop after the first failure.

```bash
VERSION="X.Y.Z"
: "${VERSION:?VERSION is not set}"

# 1. On master, clean working tree, up to date with origin
git fetch origin master --tags
echo "branch: $(git branch --show-current)"
echo "dirty files: $(git status --porcelain | wc -l | tr -d ' ')"
echo "HEAD:          $(git rev-parse HEAD)"
echo "origin/master: $(git rev-parse origin/master)"

# 2. CHANGELOG has a dated section for the version
grep -nF "## [$VERSION] - " CHANGELOG.md || echo "CHANGELOG: FAIL"

# 3. README installation snippet uses the version
grep -qF "<version>$VERSION</version>" README.md && echo "README: ok" || echo "README: FAIL"

# 4. Tag does not exist locally or on origin
test -z "$(git tag -l "v$VERSION")$(git ls-remote --tags origin "refs/tags/v$VERSION")" && echo "tag: ok" || echo "tag: FAIL (v$VERSION exists)"

# 5. Version is not already on Maven Central
curl -s -o /dev/null -w "Maven Central HTTP: %{http_code}\n" "https://repo1.maven.org/maven2/io/github/ehayik/jmaskify/$VERSION/"

# 6. CI passed for the release commit
gh run list --commit "$(git rev-parse HEAD)" --workflow jmakify-ci.yml --json status,conclusion \
  --jq 'if length == 0 then "CI: FAIL (no runs)" elif all(.[]; .status == "completed" and .conclusion == "success") then "CI: ok" else "CI: FAIL" end'

# 7. Publishing credentials exist (never print the file)
grep -q "<id>central</id>" ~/.m2/settings.xml && echo "central server: ok" || echo "central server: FAIL"

# 8. A GPG secret key exists (file check only: never start gpg-agent, see Guardrails)
ls "${GNUPGHOME:-$HOME/.gnupg}"/private-keys-v1.d/*.key >/dev/null 2>&1 && echo "GPG key: ok" || echo "GPG key: FAIL"
```

| # | Check | Passes when |
|---|-------|-------------|
| 1 | Branch & tree | Branch is `master`, 0 dirty files, `HEAD` equals `origin/master` |
| 2 | CHANGELOG | A `## [X.Y.Z] - YYYY-MM-DD` section exists (warn if the date is not today) |
| 3 | README | `README: ok` |
| 4 | Tag | `tag: ok` |
| 5 | Maven Central | HTTP `404`. Any other code (e.g. `000` for a network error) is a failure |
| 6 | CI | `CI: ok` |
| 7 | Credentials | `central server: ok` |
| 8 | GPG | `GPG key: ok` |

Record the `HEAD` SHA as the **release SHA**. It is used in Tasks 4 and 5.

If checks 2 or 3 fail, offer to prepare the version bump (`pom.xml`, `CHANGELOG.md`, `README.md`, and the Supported Versions table in `SECURITY.md` for a minor or major version) as a pull request that follows *Git Workflow* in `AGENTS.md`. The release continues only after that PR is merged.

## Task 3: Dry-run Release Build 🧪

Build everything the `release` profile produces, without signing or uploading:

```bash
VERSION="X.Y.Z"
: "${VERSION:?VERSION is not set}"
./mvnw -B -P release clean verify -Dgpg.skip
ls target/jmaskify-"$VERSION".jar target/jmaskify-"$VERSION"-sources.jar target/jmaskify-"$VERSION"-javadoc.jar .flattened-pom.xml
```

- Build succeeds with no new warnings (pay attention to Javadoc warnings).
- All four files listed above exist.
- `.flattened-pom.xml` contains `name`, `description`, `url`, `licenses`, `developers`, and `scm`.

## Task 4: Publish Hand-off 🤝

Present the report below and **stop**. The publish command is for the maintainer only. Never execute it.

"🚀 **JMaskify {VERSION} is ready to publish**

{pre-flight results table}

**Release SHA:** `{release-sha}`

**Maintainer only.** Run this yourself from the repository root, on `master` at the release SHA. It signs with your GPG key and uploads to the Central Portal. `autoPublish` is enabled, so the release goes live without a manual approval step:

```text
./mvnw clean deploy -P release
```

Do not add `-B`: in batch mode the GPG plugin signs non-interactively, so the passphrase prompt cannot appear and signing fails with `gpg: signing failed: No pinentry`. If it still fails that way without `-B`, run `gpgconf --kill gpg-agent` and try again.

Track the deployment at https://central.sonatype.com/publishing/deployments. It can take up to ~30 minutes to appear on `repo1.maven.org`.

**Reply 'published' once the deployment shows as PUBLISHED, and confirm you deployed the release SHA above. I'll then tag the release and draft the GitHub release.**"

## Task 5: Post-release ✅

Only after the maintainer confirms the publish and the release SHA.

### 5.1 Verify Availability on Maven Central

```bash
VERSION="X.Y.Z"
: "${VERSION:?VERSION is not set}"
# Expect HTTP 200. On 404, retry later (it can take ~30 minutes); anything else is an error
curl -s -o /dev/null -w "Maven Central HTTP: %{http_code}\n" "https://repo1.maven.org/maven2/io/github/ehayik/jmaskify/$VERSION/jmaskify-$VERSION.pom"
```

### 5.2 Compose the Release Notes 📝

Write the GitHub release notes to `target/release-notes-X.Y.Z.md`, following the format of earlier releases (see `gh release view v1.1.0`).

1. Take the items from the `## [X.Y.Z]` section of `CHANGELOG.md` and group them:

   | CHANGELOG section | Release notes heading |
   |-------------------|-----------------------|
   | `### Added`       | `## ⭐ New Features`  |
   | `### Changed`     | `## 🚀 Enhancements`  |
   | `### Fixed`       | `## 🐛 Bug Fixes`     |
   | `### Deprecated`  | `## ⚠️ Deprecations`  |

   Keep the sections in the order above, and leave out sections that have no items.
2. End each item with a reference to the change that introduced it. Use the pull request number when there is one (e.g. `(#16)`). For a commit pushed directly to `master`, use a short-SHA link (e.g. `([1af3a2f](https://github.com/ehayik/jmaskify/commit/<full-sha>))`). Find the references with:

   ```bash
   git log --oneline "$(git describe --tags --abbrev=0 <release-sha>^)..<release-sha>"
   gh pr list --state merged --base master --limit 30 --json number,title,mergeCommit
   ```
3. Always end with the installation section:

````markdown
## 📦 Installation

Available on Maven Central:

```xml
<dependency>
    <groupId>io.github.ehayik</groupId>
    <artifactId>jmaskify</artifactId>
    <version>X.Y.Z</version>
</dependency>
```
````

Show the composed notes to the maintainer before creating the release.

### 5.3 Tag and Draft the GitHub Release

After asking the maintainer for confirmation, tag the release SHA and create a **draft** GitHub release titled with the tag name:

```bash
VERSION="X.Y.Z"
RELEASE_SHA="<sha>"
: "${VERSION:?VERSION is not set}" "${RELEASE_SHA:?RELEASE_SHA is not set}"
test -s "target/release-notes-$VERSION.md" || { echo "release notes: FAIL (missing or empty)"; exit 1; }
git tag -a "v$VERSION" "$RELEASE_SHA" -m "Release $VERSION"
git push origin "v$VERSION"
gh release create "v$VERSION" --draft --verify-tag --title "v$VERSION" --notes-file "target/release-notes-$VERSION.md"
```

> **NOTE**
> Do not try to delete the release notes temporary file.

Finish with a summary: the version, the tag, the draft GitHub release URL (the maintainer publishes it), and the Maven Central URL `https://central.sonatype.com/artifact/io.github.ehayik/jmaskify/{VERSION}`.
