---
name: client-port
description: Ports JMaskify's agent setup (AGENTS.md and the shared agents, commands, skills, and guidelines in .agents/) to another AI coding client, such as Copilot CLI, Codex, Cursor, or Gemini CLI, by adding a thin entry point, verifying discovery with safe probes, and writing a dated, evidence-labelled compatibility record. Use when adding support for a new AI client, when re-verifying an existing port after a client upgrade, or when reviewing a change to client-specific folders or entry files.
---

# Client Port

A checklist for making JMaskify's agent setup work in another AI coding client without forking it. The skill holds only rules that stay true across clients. Facts about one client (flags, paths, versions, tool names) go in that client's compatibility record, so this file does not go stale when a client changes.

## Scope

| Path                                           | Role in a port                                                                                                 |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `AGENTS.md`, `.agents/`                        | Canonical instructions, agents, commands, skills, and guidelines                                               |
| Client folders and entry files                 | Links, a thin entry file, or client-only settings (`.claude/`, `CLAUDE.md`, `.github/copilot-instructions.md`) |
| `.agents/guidelines/<client>-compatibility.md` | One compatibility record per client                                                                            |

## Invariants

- **`.agents/` is the single source.** Client folders hold only symlinks, a thin entry file that points to `AGENTS.md`, or client-only settings that add protection but are never the only safeguard. For example, `.claude/agents`, `.claude/commands`, and `.claude/skills` link into `.agents/`, `CLAUDE.md` contains only `@AGENTS.md`, `.github/copilot-instructions.md` points to `AGENTS.md`, and `.claude/settings.json` holds Claude Code deny rules.
- **Never duplicate content into client folders.** If a client needs another file name or format, link to or point at the shared file instead of copying it.
- **Safety rules are shared, not client settings.** Never publish, the maintainer-only release step, and the read-only `java-code-reviewer` and `security-auditor` roles live in `AGENTS.md` and `.agents/`. They must not depend on one client's settings file, such as `.claude/settings.json`; those settings are extra protection for that client only.
- **Don't claim isolation without evidence.** A client may also discover another client's folders (for example `.claude/skills`). Record that as residual discovery; never claim strict isolation between clients unless the client's own commands show it.
- **Project rules go in `AGENTS.md` or `.agents/`.** Never add them to vendored third-party files such as `.agents/skills/sofa/SKILL.md`, which has a `SKILL.md.sha256` and is replaced from upstream.

## Must Not Change During a Port

- The deny rules in `.claude/settings.json`.
- The guardrails in [`release-prepare`](../../commands/release/release-prepare.md).
- Agent `tools` lists and the read-only roles in `.agents/agents/`.
- Vendored files and their checksums.

A port is delivered as a pull request and needs human review. Commit, push, and pull request creation need the user's approval first (see [orchestration guidelines §7](../../guidelines/orchestration-guidelines.md#7-communication) and [AGENTS.md → Constraints](../../../AGENTS.md#constraints)). Agents never merge it. If the client seems to need one of these changes, stop and ask the maintainer.

## Getting Started

A new client may not discover `AGENTS.md` or `.agents/skills` yet, so start it with a prompt that works either way. Open a session in the repository root and ask:

> Read `AGENTS.md`, then follow `.agents/skills/client-port/SKILL.md` to port this repository to `<client> <version>`.

If the client already discovers `.agents/skills` (or a client folder that links to it), asking it to load the `client-port` skill is enough.

- **The agent** runs the shell commands it is permitted to run, such as the client's own CLI commands that list instructions, agents, or skills. The [probe limits](#4-probe-safely) and [approval rules](../../guidelines/orchestration-guidelines.md#7-communication) still apply.
- **The user** runs, when the agent asks, the commands that work only inside an interactive session (such as slash commands that list instructions) and pastes the output back. The agent records that output as evidence; it counts as Verified only for what it actually shows. The user changes session settings, such as disabling a duplicate instruction source, only to test or record a workaround for the compatibility record.
- **An entry point may not be needed.** If the client already discovers `AGENTS.md` and `.agents/skills`, add no new client file unless the client needs one to adopt the `discovery` role or to find agents or commands. The port is then the compatibility record.

## Port Checklist

### 1. Record the Client

- [ ] Client name, exact version (from the client's own version command), and the date of the check.
- [ ] Links to the client's official docs for instructions, agents, commands, skills, and settings. Check them with web search or fetch; search only public facts, never repository code or data.

### 2. Add the Entry Point

- [ ] Add the smallest entry point the client needs: a thin instruction file that points to `AGENTS.md`, or symlinks into `.agents/`. Prefer what the client already discovers, such as `AGENTS.md` or `.agents/skills`, over a new file; if it discovers both, add nothing unless the client needs a file to adopt the `discovery` role or to find agents or commands.
- [ ] The entry file adds no rules of its own beyond pointing to `AGENTS.md` and the `discovery` role.

### 3. Verify Discovery

- [ ] Use the client's own commands or listings to check what it finds: instruction files, agents, commands, and skills.
- [ ] Record each area as Verified, Unsupported, or Unknown. Anything you did not test is Unknown.

### 4. Probe Safely

- [ ] Invoke one read-only agent (for example `java-code-reviewer`) with a harmless prompt.
- [ ] Check that one shared command from `.agents/commands/`, for example `/docs:dev:dev-journal-create`, is recognized and shows its purpose, without running it to completion. Record this under commands.
- [ ] Optionally invoke one skill that makes no changes (for example `/conventional-commits` on a sample header). Record this under skills, not commands.
- [ ] Never run `deploy`, publish, `/release:release-prepare` or other release commands, or `gpg` while probing. Never commit, push, open a pull request, or file an issue or security advisory while probing.

### 5. Map Tool Names

- [ ] Agent `tools` frontmatter uses Claude Code names (see [AGENTS.md → Agent Resources](../../../AGENTS.md#agent-resources)). Record how each name maps to the client's tools, and list the names it does not recognize as Unknown.
- [ ] Note that a `tools` list limits what the model is offered; it is not an OS-level security boundary.

### 6. Check Existing Clients

- [ ] Existing clients are unaffected. For Claude Code: `CLAUDE.md`, `.claude/settings.json`, and the `.claude/` symlinks are unchanged (`git status --short`, `ls -la .claude`).

## Compatibility Record

Write one record per client at `.agents/guidelines/<client>-compatibility.md`. Use [`copilot-cli-compatibility.md`](../../guidelines/copilot-cli-compatibility.md) as the worked example. Each record has:

- The client version and the date it was checked, stated as an observation, not a guarantee.
- A matrix (instructions, agents, commands, skills, settings, `tools` metadata, deploy-deny rules) using **Verified**, **Unsupported**, and **Unknown**. Unsupported means the behavior is not available, not merely untested.
- Workarounds, such as disabling a duplicate instruction source.
- Accepted residual discovery: which other clients' folders the client may still read.
- Official sources, with the date they were reviewed.

When a client is upgraded, re-run the checklist, update the version and date, and change labels only when you have new evidence.
