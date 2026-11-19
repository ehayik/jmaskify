---
description: "Execute or resume a development plan task by task, routing each task to its owning agent with quality gates and human checkpoints"
---

# Development Plan Executor

Execute the development plan at `$ARGUMENTS`, created by `dev-plan-init.md`. The plan file is the only source of progress: running this command again on a partly completed plan resumes it from the first unchecked task.

Execute each task in the order given. The `discovery` agent (the main session) runs this command and orchestrates the other agents.

## Guardrails ⛔

- **Stay in scope.** Execute only the tasks in the plan. If a task turns out to need extra work, stop and propose a plan change instead of doing it silently.
- **Ask before anything that leaves the machine or rewrites history.** No `git commit`, `git push`, `gh pr create`, or force operations without the user's confirmation.
- **Never publish.** Releases follow the `release-prepare` command and the maintainer runs the publish step (see `AGENTS.md` → *Constraints*).
- **Respect agent roles.** `java-test-engineer` edits only `src/test/`, `doc-manager` edits only documentation and Javadoc, and `java-code-reviewer` and `security-auditor` never edit files.
- **No sensitive data.** Never put credentials, tokens, or realistic personal data in plans, briefs, logs, or commit messages.

## Task 1: Load the Plan and Restore State 📂

If `$ARGUMENTS` is empty, list the available plans and ask which one to execute:

```bash
ls -t .agents/artifacts/plans/DEV-PLAN-*.md 2>/dev/null | head -10
```

If there are no plans, suggest running `/docs:dev:dev-plan-init` first and stop.

Read the whole plan file, then check the working tree:

```bash
git rev-parse --abbrev-ref HEAD
git status --short
git log --oneline master..HEAD
```

Restore the state from the plan:
- **Done**: tasks marked `- [x]`
- **Next**: the first task marked `- [ ]`, plus any following `(parallel)` tasks in the same phase
- **Blockers**: entries marked `BLOCKED` in the *Progress Log* that have not been resolved
- **Execution style**: the `**Execution Style**` header (`guided` if missing)

Check for drift before continuing:
- The current branch matches the plan's branch (Task 1.1). If not, ask whether to switch.
- Uncommitted changes that no completed task explains. Ask before continuing.
- Tasks marked done whose changes are not in the working tree or branch history. Ask whether to redo or keep them.

Present the status:

"📂 **Plan: {feature-name}**

• Progress: {done-count}/{total-count} tasks ({current-phase})
• Branch: {branch} ({clean | uncommitted changes})
• Last log entry: {last-progress-log-line | none}
• Next: **Task {id}** `{agent}`: {task-text}
• Blockers: {none | list}

**Continue?**
1. Execute the next task (or parallel group)
2. Execute the rest of the current phase, then check in
3. Edit the plan first
4. Stop

Your choice (1-4):"

Choice 2 overrides the plan's execution style for this run.

## Task 2: Execute Tasks 🔁

Repeat the steps below for each task (or parallel group) until the plan is done, the user stops, or a task is blocked.

### Step 1: Route the Task

Use the agent named in the task. If a task names no agent, infer it from this table and confirm with the user:

| Work                                                                    | Agent                |
|-------------------------------------------------------------------------|----------------------|
| Branches, quality gates, plan updates, git and PRs                      | `discovery` (self)   |
| API design, scaffolding, style enforcement                              | `architect`          |
| Production code in `src/main/`                                          | `java-developer`     |
| Tests in `src/test/`                                                    | `java-test-engineer` |
| Review of the branch diff (`build-review` skill for build and CI files) | `java-code-reviewer` |
| Regexes, logging, reversibility, input validation                       | `security-auditor`   |
| `pom.xml`, CI workflows, dependencies                                   | `devops-engineer`    |
| Javadoc, `README.md`, `CHANGELOG.md`, journals                          | `doc-manager`        |

Do `discovery` tasks directly. A single command, such as running the build, is never delegated.

### Step 2: Brief the Agent

The brief is the agent's only context, so make it complete and narrow. Use the template in the [orchestration guidelines → Delegation Brief](../../../guidelines/orchestration-guidelines.md#3-delegation-brief), filling `Plan` with the plan file path, `Task` with the task id and text, and `Done when` with the plan's acceptance criteria.

- **Reviewers** (`java-code-reviewer`, `security-auditor`) get the diff scope (`git diff master...HEAD`) and the plan, never the orchestrator's opinion of the code.
- **Parallel groups**: launch all `(parallel)` tasks of the group in a single message, but only if their file scopes do not overlap. Otherwise run them in sequence.

### Step 3: Check the Result

Read the agent's report and inspect the changes (`git status --short`, `git diff --stat`). Do not take the report on trust. Show each result to the user with the [orchestration guidelines → Report Templates](../../../guidelines/orchestration-guidelines.md#4-report-templates). The `java-code-reviewer` report must include the verdict (see [§4](../../../guidelines/orchestration-guidelines.md#4-report-templates)).

After any change to Java code or `pom.xml`, run the quality gates:

```bash
./mvnw -B -q spotless:apply
./mvnw -B verify
```

Route failures to the owner:
- Production compile errors, NullAway or ErrorProne errors, failing tests caused by production code → `java-developer`
- Failing or missing tests, coverage below 80% → `java-test-engineer`
- Build or plugin failures → `devops-engineer`

Route review findings:
- `issue (blocking)` → the agent that wrote the code, then run the gates again and re-review
- Non-blocking findings → list them for the user to decide; do not fix them silently

Documentation tasks for user-facing changes follow the [documentation review loop](../../../guidelines/orchestration-guidelines.md#6-documentation-review-loop) (`doc-manager` ↔ `java-code-reviewer`).

If the same gate still fails or the verdict is still `CHANGES REQUESTED` after **three** fix rounds, stop and mark the task `BLOCKED` (see the [fix loop](../../../guidelines/orchestration-guidelines.md#5-fix-loop)).

### Step 4: Record Progress

Update the plan file after every task, so an interrupted session can resume:

- Done: change `- [ ]` to `- [x]` for the task.
- Append a line to the *Progress Log*: `- {YYYY-MM-DD} Task {id} ({agent}): {one-line result}`.
- Blocked: leave the task unchecked and log `- {YYYY-MM-DD} Task {id} ({agent}): BLOCKED: {reason}`.
- Deviation agreed with the user: edit the task text and log the change.

### Step 5: Checkpoint

- **guided**: after every task (or parallel group), show the result and ask before continuing.
- **batch**: continue without asking until the end of the phase, then show a phase summary and ask.
- Always stop and ask on a blocker, a proposed plan change, or before any commit, push, or PR.

Checkpoint format:

"✅ **Task {id} done** `{agent}`

• Changes: {files and one-line summary}
• Gates: {spotless/verify result | not run, no code changes}
• Findings: {none | list}
• Next: **Task {id}** `{agent}`: {task-text}

Continue? (yes / edit plan / stop)"

## Task 3: Finish the Plan 🏁

When every task is checked:

1. Run the quality gates one last time (`./mvnw -B verify`) and confirm a clean `git status`, apart from the intended changes.
2. Check the `AGENTS.md` → *Feature Checklist* against the changes and report any unmet item.
3. Draft the commit message with the `conventional-commits` skill and the PR description following `.agents/guidelines/pull-request-description-guidelines.md`. If the plan header names an issue (`**Issue**: #N`), add a `Fixes #N` footer to the commit and reference the issue in the PR description.
4. Ask the user before committing, pushing, and opening the PR (see `AGENTS.md` → *Pull Request Process*).
5. Suggest `/docs:dev:dev-journal-create` to record decisions made during execution.

Present the summary:

"🏁 **Plan complete: {feature-name}**

• Tasks: {total-count} done, {deviation-count} changed during execution
• Gates: {verify result}, coverage {coverage}
• Review: {blocking findings resolved}, {non-blocking findings deferred}
• Proposed commit: `{commit-header}`

**Next:**
1. Commit, push, and open the PR
2. Commit only
3. Review the diff first
4. Stop here

Your choice (1-4):"

## Error Recovery 🔧

```
IF THE PLAN FILE CANNOT BE PARSED (no "- [ ]" / "- [x]" tasks):
1. Show what was found
2. Offer to convert the plan to the dev-plan-init template, or stop

IF AN AGENT CHANGES FILES OUTSIDE ITS SCOPE OR EXPANDS THE TASK:
1. Do not keep the extra changes silently
2. Show them to the user and ask whether to keep, revert, or add a plan task

IF THE PLAN IS OUTDATED (code changed on master, task no longer makes sense):
1. Mark the affected tasks BLOCKED with the reason
2. Propose plan edits and wait for approval

IF THE SESSION IS INTERRUPTED:
1. Nothing to do: the next run restores state from the plan file (Task 1)
```
