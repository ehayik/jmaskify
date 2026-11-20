---
name: github-issues
description: Raises a GitHub issue for a JMaskify bug, or drafts a private GitHub security advisory for a security bug, after classifying it, checking for duplicates, and reproducing it against the public API. Use when a bug turns up outside the current task, when a report lists out-of-scope findings, or when the user asks to report a bug, raise or file an issue, or report a vulnerability.
---

# GitHub Issues

How agents raise bugs in JMaskify. This skill is the single source for the issue format and workflow; the repository's bug report template (`.github/ISSUE_TEMPLATE/bug_report.yml`) mirrors its fields, and other files link here instead of copying it.

## Who files

- **Only the main session (`discovery`) creates issues or advisories, and only after explicit user approval.** An issue or advisory leaves the machine, so it follows the same rule as commits, pushes, and pull requests.
- Every other agent lists bugs it finds outside its task in an **Out-of-scope findings** block in its report (see the [report templates](../../guidelines/orchestration-guidelines.md#4-report-templates)), with `file:line` and a suggested reproduction. They never run `gh issue create` or the advisory API.

## Issue format

Issues [#46](https://github.com/ehayik/jmaskify/issues/46) and [#56](https://github.com/ehayik/jmaskify/issues/56) show the format. Use them as a model for structure only, not content.

| Section            | Content                                                                                                         |
|--------------------|-----------------------------------------------------------------------------------------------------------------|
| Title              | Component and symptom, written as a statement: "JsonMasker shares the builder's mutable map, so built maskers are not immutable" |
| Description        | 2–4 sentences: the defect, where it is (`File.java:line`), and why it matters                                   |
| Steps to reproduce | A minimal Java snippet that uses only the public API                                                            |
| Actual output      | The real output of running the snippet, copied verbatim                                                         |
| Expected output    | What should happen, citing the Javadoc, `README.md`, or contract that says so                                   |
| Notes              | Optional: related code with the same pattern, marked "not reproduced" if you haven't run it                     |
| Environment        | JMaskify version and commit SHA (e.g. `master` at `<sha>`), Java version, and any relevant dependency version   |

The title goes in `--title`. The body uses one `### <Section>` heading per remaining row, in that order, with the snippet in a ```` ```java ```` block and the outputs in ```` ```text ```` blocks.

## Workflow

### 1. Classify

Decide first whether it is a **security bug**. In JMaskify that includes anything that:

1. exposes masked or sensitive input (in output, logs, exception messages or causes, or `toString`);
2. reveals more than the masker's documented contract (for example length, prefix, or suffix), or silently skips masking on malformed input (fail-open);
3. makes masking reversible or predictable enough to recover the original value, including a reversible strategy not documented as reversible;
4. enables ReDoS or other denial of service from crafted input (for example deeply nested JSON or huge allocations);
5. allows injection (input that changes the structure of the masked output, for example JSON);
6. comes from a known vulnerable dependency that can be exploited through JMaskify.

If it is, or you're unsure, go to [step 7](#7-security-route). A security bug never appears in a public issue, comment, discussion, branch name, commit message, PR title or body, `CHANGELOG.md`, release notes, or any external service (SOFA posts or searches, web searches, pastebins).

### 2. Check for duplicates

```bash
gh issue list --state all --search "<keywords>"
```

Try two or three keyword sets (class name, symptom, exception type). If an issue already covers the bug, don't open a new one. Propose a comment with any new detail instead, and post it only with the user's approval. Security bugs skip this step ([step 1](#1-classify) already sent them to [step 7](#7-security-route)). If a public issue already covers one, never add detail to it; tell the maintainer privately.

### 3. Reproduce

Run a minimal public-API snippet and capture what it actually prints. Follow the [reproduction recipe](references/reproduction.md).

**Never write an "Actual output" that you didn't observe.** If the bug doesn't reproduce, say so in your report and don't file. Related code you only read goes under Notes, marked "not reproduced".

Check the actual output against the [step 1](#1-classify) criteria again. If it shows any part of the sensitive input, or you're now unsure, switch to [step 7](#7-security-route).

### 4. Write the body

Write the body to `target/issue_<slug>.md` (`target/` is git-ignored), where `<slug>` is a short kebab-case name such as `jsonmasker-shared-map`. Follow the [issue format](#issue-format).

Use only obviously synthetic data: `s3cr3tValue`, `jane@example.com`, test card number `4111-1111-1111-1111`. Never use real personal data, real card numbers, tokens, or credentials, even when they came up in the task.

### 5. Ask the user

Show the user the title, the body file path, and the command you will run, and wait for explicit approval. If they decline, keep the body file and list the bug as a proposed issue in your summary.

### 6. Create

```bash
gh issue create --title "<title>" --label bug --body-file target/issue_<slug>.md
```

Use the existing `bug` label; don't create labels unless the user asks. Record the issue URL in the plan's follow-ups or the PR description. Don't delete the body file.

### 7. Security route

Draft a **private repository security advisory**. It stays in draft, visible only to repository admins, security managers, and collaborators added to that advisory, until the maintainer publishes it.

1. Check for an existing advisory that already covers it:

   ```bash
   gh api --paginate repos/ehayik/jmaskify/security-advisories
   ```

   If one does, don't draft a new one; give the maintainer any new detail privately. Only admins, security managers, and advisory collaborators can see drafts, so an empty or failed list doesn't prove there is none.
2. Reproduce it locally as in [step 3](#3-reproduce), with synthetic data. Don't push the snippet anywhere.
3. Write the payload to `target/advisory_<slug>.json`:

   ```json
   {
     "summary": "<component and symptom, without exploit details>",
     "description": "<defect, location, impact, steps to reproduce, actual and expected output, environment>",
     "severity": "medium",
     "cwe_ids": ["CWE-209"],
     "vulnerabilities": [
       {
         "package": { "ecosystem": "maven", "name": "io.github.ehayik:jmaskify" },
         "vulnerable_version_range": "<= <latest released version>",
         "patched_versions": null,
         "vulnerable_functions": []
       }
     ]
   }
   ```

   `summary`, `description`, and `vulnerabilities` are required. `severity` is one of `critical`, `high`, `medium`, or `low`; set either `severity` or `cvss_vector_string`, never both. Pick the CWE that fits (for example CWE-200 exposure, CWE-209 sensitive data in error messages, CWE-1333 ReDoS), or leave `cwe_ids` out. End the vulnerable range at the latest released version from `git describe --tags --abbrev=0` (tag `v1.1.1` gives `<= 1.1.1`), not `pom.xml`, which may hold an unreleased bump. Don't add `"start_private_fork": true`; whether to open a temporary private fork is the maintainer's choice.
4. Ask the user, as in [step 5](#5-ask-the-user).
5. Create the draft:

   ```bash
   gh api --method POST repos/ehayik/jmaskify/security-advisories --input target/advisory_<slug>.json
   ```

6. The API needs repository admin or security-manager rights. On a 422 (validation error), fix the payload and retry. **On a 403, a 404, or any other failure**, ask the user to approve submitting a private vulnerability report instead, with the same file:

   ```bash
   gh api --method POST repos/ehayik/jmaskify/security-advisories/reports --input target/advisory_<slug>.json
   ```

   This endpoint works only while private vulnerability reporting is enabled and needs only `summary` and `description`; `severity`, `cwe_ids`, and `vulnerabilities` are optional. If it also fails, stop: give the maintainer the drafted summary and description (or the JSON file path) and ask them to file it privately. **Never fall back to a public issue.**
7. Record the advisory ID or link only in the git-ignored plan or the session summary, never in a PR, commit, or `CHANGELOG.md`.
8. **Stop here.** Don't plan, branch, or fix a security bug yourself; see [step 8](#8-fix-linkage).

Humans report vulnerabilities through the process in [`SECURITY.md`](../../../SECURITY.md).

### 8. Fix linkage

- Fix a filed bug on a branch named `fix/<slug>`, following `AGENTS.md` → [Fixing a Bug](../../../AGENTS.md#fixing-a-bug).
- The fix commit closes the issue with a `Fixes #N` footer ([`conventional-commits`](../conventional-commits/SKILL.md) skill).
- For a security advisory, agents stop after drafting it. The maintainer decides where and when to fix it, for example in the advisory's temporary private fork. Until the advisory is published, no public branch name, commit, PR title or body, `CHANGELOG.md` entry, or release note describes the vulnerability.

## Guardrails

- Ask the user before creating anything: issues, comments, and advisories all leave the machine.
- Only `discovery` files; other agents report out-of-scope findings and stop.
- Reproduce before filing; the actual output comes from a real run, never from reading the code.
- Synthetic data only, in titles, bodies, snippets, and outputs.
- Security bugs go only through the private advisory route, or to the maintainer by hand. Never public.
- Never put details of an unpublished vulnerability in any public place or external service: issues, comments, discussions, branch names, commits, PR titles or bodies, `CHANGELOG.md`, release notes, SOFA posts or searches, web searches, or pastebins.
- Don't change repository settings (labels, private vulnerability reporting); ask the maintainer.
