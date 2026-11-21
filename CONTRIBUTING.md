# Contributing to JMaskify

Thanks for your interest in JMaskify. This guide explains how to set up the project, how to propose a change, and how
to use the AI agent setup that ships with the repository. You can make every change by hand; the agent setup is
optional.

The detailed rules live in [AGENTS.md](AGENTS.md) and the files it links to. This guide points to them instead of
copying them, so check those files for the full details.

## Table of Contents

- [Before You Start](#before-you-start)
- [Development Setup](#development-setup)
- [Working with AI Agents](#working-with-ai-agents)
- [Development Lifecycle](#development-lifecycle)
- [Branches, Commits, and Pull Requests](#branches-commits-and-pull-requests)
- [Using Other AI Tools](#using-other-ai-tools)

## Before You Start

- **Open an issue first.** For a bug, use the
  [Bug report](https://github.com/ehayik/jmaskify/issues/new?template=bug_report.yml) form with a minimal
  reproduction. For a new feature or a larger change, use the
  [Feature request](https://github.com/ehayik/jmaskify/issues/new?template=feature_request.yml) form before you
  write code, so we can agree on the approach.
- **Report security problems privately.** Never put a vulnerability in a public issue or pull request.
  Follow [SECURITY.md](SECURITY.md) instead.
- **Use synthetic data only.** Never paste real personal data, card numbers, or credentials into issues, tests, or
  commits.
- **You are responsible for what you submit.** You may use any tool to write your change, including AI agents, but
  you are accountable for every line in your pull request. Read and understand it before you open the PR.

## Development Setup

You need **JDK 17** (the version in [`.java-version`](.java-version) and in CI) and Git. Maven is not required: use
the Maven Wrapper (`./mvnw`, or `mvnw.cmd` on Windows).

```bash
# Build, run the tests, and run the static analysis (ErrorProne and NullAway)
./mvnw verify

# Format Java and POM files with Spotless (Palantir Java Format)
./mvnw spotless:apply

# Check formatting without changing files
./mvnw spotless:check
```

The build installs a Git pre-commit hook for you. Any build that reaches the `generate-sources` phase, including
`./mvnw verify`, copies [`scripts/pre-commit`](scripts/pre-commit) into `.git/hooks/`, replacing any existing
pre-commit hook. The hook runs `./mvnw spotless:apply` and re-stages your staged files.

> **WARNING**
> Once you have built the project, the hook re-stages whole files on every commit, so if you staged only part of a
> file, its unstaged changes are committed too.

See [AGENTS.md → Code Style & Quality](AGENTS.md#code-style--quality) and
[AGENTS.md → Testing Guidelines](AGENTS.md#testing-guidelines) for the coding and testing standards.

## Working with AI Agents

The repository ships an agent setup built for [Claude Code](https://code.claude.com/docs). It is optional. Other
tools get [best-effort support](#using-other-ai-tools).

### Where Things Live

All agent files are tool-agnostic and live in [`.agents/`](.agents). The `.claude/agents`, `.claude/commands`, and
`.claude/skills` folders are symlinks into `.agents/`, so **edit the files in `.agents/`, never the links**. On
Windows, symlinks need extra setup: see the note in [AGENTS.md → Agent Resources](AGENTS.md#agent-resources).

| Path                                                         | Contents                                                      |
|--------------------------------------------------------------|---------------------------------------------------------------|
| [AGENTS.md](AGENTS.md)                                       | Entry point for agents: standards, workflows, and constraints |
| [`.agents/project-overview.md`](.agents/project-overview.md) | Project purpose, structure, and architecture                  |
| [`.agents/guidelines/`](.agents/guidelines)                  | Java, architecture, orchestration, and PR description rules   |
| [`.agents/agents/`](.agents/agents)                          | Agent roles                                                   |
| [`.agents/commands/`](.agents/commands)                      | Slash commands for plans, journals, and releases              |
| [`.agents/skills/`](.agents/skills)                          | Skills that agents load on demand                             |
| `.agents/artifacts/`                                         | Plans and journals; git-ignored, so they stay on your machine |
| [`.claude/settings.json`](.claude/settings.json)             | Claude Code settings: default agent and blocked commands      |

### The Agents

[`.claude/settings.json`](.claude/settings.json) sets `"agent": "discovery"`, so every Claude Code session in this
repository starts as **`discovery`**. It researches, plans, and orchestrates: for coding tasks it delegates the work
to the other agents instead of writing the code itself.

| Agent                                                        | Role                                                                 |
|--------------------------------------------------------------|----------------------------------------------------------------------|
| [`discovery`](.agents/agents/discovery.md)                   | Main session: explores the code, plans, and coordinates other agents |
| [`architect`](.agents/agents/architect.md)                   | Reviews designs, scaffolds new maskers, enforces the guidelines      |
| [`java-developer`](.agents/agents/java-developer.md)         | Writes production code, features, bug fixes, and refactorings        |
| [`java-test-engineer`](.agents/agents/java-test-engineer.md) | Writes unit tests and improves coverage                              |
| [`java-code-reviewer`](.agents/agents/java-code-reviewer.md) | Reviews changes, including build and CI files (read-only)            |
| [`security-auditor`](.agents/agents/security-auditor.md)     | Audits masking logic, ReDoS risks, and logging (read-only)           |
| [`devops-engineer`](.agents/agents/devops-engineer.md)       | Maintains `pom.xml`, GitHub Actions, SonarCloud, and dependencies    |
| [`doc-manager`](.agents/agents/doc-manager.md)               | Maintains Javadoc, `README.md`, `CHANGELOG.md`, and journals         |

How `discovery` delegates, reviews, and runs fix loops (at most 3 rounds before it stops and asks you) is described
in the [orchestration guidelines](.agents/guidelines/orchestration-guidelines.md).

### Where You Approve

The agents stop and ask you at these points:

- **Ad-hoc coding tasks** (no plan): `discovery` shows an
  [orchestration preview](.agents/guidelines/orchestration-guidelines.md#2-orchestration-preview) of which agents
  will do what, and waits for your approval before delegating.
- **Plan-driven work**: you approve the plan, then confirm at the execution checkpoints. See the
  [commands](#development-lifecycle) for the details.
- **Before commit, push, PR, or issue**: committing, pushing, opening a PR, and filing a GitHub issue or security
  advisory always need your confirmation.

`discovery` handles trivial work, such as running the build or creating a branch, directly.

### What the Agents Won't Do

- **Publish releases.** Only the maintainer publishes to Maven Central. The deny rules in
  [`.claude/settings.json`](.claude/settings.json) also block `deploy` and `central-publishing` commands.
- **Merge pull requests.** Every PR needs a human review and approval.
- **File issues on their own.** Agents list bugs they find outside the task as out-of-scope findings; `discovery`
  offers to raise them, and only with your approval.

See [AGENTS.md → Constraints](AGENTS.md#constraints).

### Skills

Agents load these skills when the task needs them. You can also ask for one by name.

| Skill                                                                    | Use                                                                       |
|--------------------------------------------------------------------------|---------------------------------------------------------------------------|
| [`conventional-commits`](.agents/skills/conventional-commits/SKILL.md)   | Write and check commit messages                                           |
| [`conventional-comments`](.agents/skills/conventional-comments/SKILL.md) | Write review feedback with clear labels and severity                      |
| [`build-review`](.agents/skills/build-review/SKILL.md)                   | Review changes to `pom.xml`, CI workflows, the Maven wrapper, and scripts |
| [`github-issues`](.agents/skills/github-issues/SKILL.md)                 | Raise a bug issue, or draft a private security advisory                   |
| [`client-port`](.agents/skills/client-port/SKILL.md)                     | Port the agent setup to another AI client, or re-verify an existing port  |
| [`sofa`](.agents/skills/sofa/SKILL.md)                                   | Search Stack Overflow for Agents (optional, see below)                    |

### Stack Overflow for Agents (Optional)

The `sofa` skill lets agents search a shared knowledge base of agent-validated answers. You don't need it to
contribute. To use it, provide an API key through the `SOFA_API_KEY` environment variable or `.sofa/credentials.json`.
The `.sofa/` folder is git-ignored. **Never commit credentials.** See
[AGENTS.md → Stack Overflow for Agents (SOFA)](AGENTS.md#stack-overflow-for-agents-sofa).

## Development Lifecycle

The slash commands follow the folder layout under [`.agents/commands/`](.agents/commands). Each command file describes
its steps and approval points in full. Here they are in the order you use them:

| Command                    | When to use it                                       | Example                                  | Output                                    |
|----------------------------|------------------------------------------------------|------------------------------------------|-------------------------------------------|
| [`dev-plan-init`][init]    | Plan a feature, bug fix, or refactoring              | `/docs:dev:dev-plan-init #42`            | Plan file in `.agents/artifacts/plans/`   |
| [`dev-plan-execute`][exec] | Run a plan, or resume it later                       | `/docs:dev:dev-plan-execute <plan-file>` | Code on your branch; progress in the plan |
| [`dev-journal-create`][jc] | Record progress and decisions after a session        | `/docs:dev:dev-journal-create`           | Journal in `.agents/artifacts/journal/`   |
| [`dev-journal-update`][ju] | Bring a journal up to date after more work           | `/docs:dev:dev-journal-update`           | Updated journal and a change log          |
| [`release-prepare`][rel]   | **Maintainer only**: pre-flight checks for a release | `/release:release-prepare 1.2.0`         | Pre-flight report and draft release       |

[init]: .agents/commands/docs/dev/dev-plan-init.md
[exec]: .agents/commands/docs/dev/dev-plan-execute.md
[jc]: .agents/commands/docs/dev/dev-journal-create.md
[ju]: .agents/commands/docs/dev/dev-journal-update.md
[rel]: .agents/commands/release/release-prepare.md

### Planning: `dev-plan-init`

Use it before a feature, bug fix, or refactoring that touches more than a few lines. Pass a free-text description, an
issue number, or an issue URL:

```text
/docs:dev:dev-plan-init Email address masker preserving the domain
/docs:dev:dev-plan-init #42
/docs:dev:dev-plan-init https://github.com/ehayik/jmaskify/issues/42
```

The command analyzes the code, asks you a few questions about scope and settings, and shows the plan for your
approval. Each task in the plan names the agent that owns it. The plan is saved in `.agents/artifacts/plans/`.

If the request looks like a security bug, the command does not plan it publicly: it switches to the private route of
the `github-issues` skill and stops.

### Executing and Resuming: `dev-plan-execute`

Use it to run an approved plan. Pass the plan file, or no argument to pick one of the latest plans:

```text
/docs:dev:dev-plan-execute .agents/artifacts/plans/DEV-PLAN-email-masker-20261120_101500.md
```

The command routes each task to its agent and stops at checkpoints for your confirmation. The plan file is the only
record of progress: to resume, run the command again on the same file and it continues from the first unchecked task.
At the end, it drafts the commit message and PR description and asks before committing, pushing, or opening the PR.

### Ad-hoc Requests

For small changes you don't need a plan. Describe the task to `discovery` in plain language. It shows the
orchestration preview, waits for your approval, and then runs the same quality gates, code review, and documentation
steps (for user-facing changes) as plan-driven work.

### Journals: `dev-journal-create` and `dev-journal-update`

Journals are local notes about what was done and why. They are useful as context for your next session.

- **Create** one with `dev-journal-create` at the end of a session. Pass `--quick` for a short summary. It is saved in
  `.agents/artifacts/journal/` after you approve a preview.
- **Update** it with `dev-journal-update` after more work on the same feature. It picks the journal to update with
  you, shows a preview, keeps your manual edits, and saves a change log in `.agents/artifacts/change-logs/`.

> **NOTE**
> Plans, journals, and change logs are saved under `.agents/artifacts/`, which is git-ignored. They are local working
> notes: don't commit them or add them to a pull request.

### Releases: `release-prepare`

For the maintainer only. It runs pre-flight checks and a dry-run build, then stops so the maintainer can publish. See
[AGENTS.md → Releasing a New Version](AGENTS.md#releasing-a-new-version).

### Example: Adding a New Masker from Issue #42

1. Start Claude Code in the repository root and run `/docs:dev:dev-plan-init #42`.
2. Confirm the scope and pick a complexity level (a new masker is usually *Moderate*).
3. Choose the plan settings, such as the execution style (`guided` asks after every task, `batch` after every phase).
4. Approve the plan and start execution, or save it and run `/docs:dev:dev-plan-execute <plan-file>` later. Either
   way, the plan is saved in `.agents/artifacts/plans/`.
5. `discovery` creates the `feat/...` branch, then routes each task: `architect` for the API, `java-developer` for the
   masker and its factory method in `Masker`, `java-test-engineer` for the tests, `java-code-reviewer` for the review,
   and `doc-manager` for Javadoc, `README.md`, and `CHANGELOG.md`. If the masker uses regexes, `security-auditor`
   also reviews it.
6. Confirm at each checkpoint. If you stop, run the same command on the plan file later to resume.
7. At the end, review the proposed commit (with a `Fixes #42` footer) and PR description, then confirm.
8. Optionally, run `/docs:dev:dev-journal-create` to record the decisions.

The manual steps for the same change are in
[AGENTS.md → Adding a New Masking Strategy](AGENTS.md#adding-a-new-masking-strategy).

## Branches, Commits, and Pull Requests

- **Branches**: create them from `master` with a `feat/`, `fix/`, `ref/`, or `docs/` prefix.
- **Commits**: follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). The
  [`conventional-commits` skill](.agents/skills/conventional-commits/SKILL.md) has the types and rules. A commit that
  fixes an issue ends with a `Fixes #N` footer.
- **Pull requests**: target `master`, and follow the
  [pull request description guidelines](.agents/guidelines/pull-request-description-guidelines.md).
- **CHANGELOG**: add an entry to [CHANGELOG.md](CHANGELOG.md) under `[Unreleased]` for every user-facing change.
- **Before you push**: run `./mvnw spotless:apply` and `./mvnw verify`.

The full workflow is in [AGENTS.md → Git Workflow](AGENTS.md#git-workflow).

## Using Other AI Tools

Other AI coding tools are supported on a best-effort basis:

- Tools that support the `AGENTS.md` convention read [AGENTS.md](AGENTS.md) as their entry point. It links to
  everything in `.agents/`.
- Agent `tools` lists in the `.agents/agents/*.md` frontmatter use Claude Code tool names (`Read`, `Edit`, `Write`,
  `Bash`, `Grep`, `Glob`). If your tool uses other names, treat the list as the agent's intended permissions: agents
  without `Edit` or `Write` are read-only.
- On Windows, enable symlink support before you clone; see [AGENTS.md → Agent Resources](AGENTS.md#agent-resources).
- To add support for another client, start a session in the repository root and ask: "Read `AGENTS.md`, then follow
  `.agents/skills/client-port/SKILL.md` to port this repository to `<client> <version>`." The
  [`client-port` skill](.agents/skills/client-port/SKILL.md) adds a thin entry point if one is needed, verifies
  discovery with safe probes, and records the results in `.agents/guidelines/<client>-compatibility.md` (see the
  [Copilot CLI record](.agents/guidelines/copilot-cli-compatibility.md)). Its
  [Getting Started](.agents/skills/client-port/SKILL.md#getting-started) section explains which commands you may need
  to run yourself.
