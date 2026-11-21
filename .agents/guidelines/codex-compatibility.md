# OpenAI Codex CLI Compatibility

**Observed with OpenAI Codex CLI 0.171.0 on 2026-11-21.** This record covers
JMaskify-specific observations, not a guarantee for other Codex versions or
environments.

JMaskify keeps `.agents/` provider-neutral. The root `AGENTS.md` remains the
shared instruction entry point, and `.agents/skills/` remains the shared skill
location. No Codex-only instruction file was needed for these resources.

## Verification matrix

Classification means:

- **Verified**: direct CLI observation or explicit official documentation.
- **Unsupported**: behavior is not available or is not a supported scope of
  the relevant control. Unsupported is not used for behaviors that simply have
  not been established.
- **Unknown**: evidence does not establish a general runtime behavior.

| Area | Verified support | Unsupported behavior | Unknown behavior / limitation |
|---|---|---|---|
| Instructions | `codex --version` reported `codex-cli 0.171.0`. The read-only `codex exec` probe ran in the repository and identified its repository instructions; official Codex guidance documents `AGENTS.md` as a repository instruction source. | — | Discovery order and size limits for all possible instruction files were not probed in this version. Whether this setup needs another project-specific instruction entry point was not tested. |
| Agents | The probe found the eight shared role definitions in `.agents/agents/`, including the read-only `java-code-reviewer`. Codex documentation describes project-scoped custom agents as TOML files under `.codex/agents/`; this repository now has one thin TOML adapter per shared role, each pointing to its canonical Markdown role file. | The shared Markdown role definitions alone are not Codex's documented custom-agent file format or location. | A read-only delegation probe without a feature override failed while loading the agent thread (`no rollout found`). With `multi_agent_v2` explicitly enabled, the probe returned the requested canonical role path, but did not expose a distinct agent thread. Whether the adapter was invoked and followed remains unverified; neither result establishes that delegation is unavailable. |
| Commands | The probe found `.agents/commands/docs/dev/dev-journal-create.md` and inspected its purpose: generating a development progress journal, with preview before saving. | — | Codex recognition of `/docs:dev:dev-journal-create` was not established. The probe inspected the shared file but did not invoke the slash command. |
| Skills | OpenAI documents Agent Skills and Codex project skills under `.agents/skills/`. The Codex probe enumerated JMaskify skill files, including `client-port`. | — | The probe did not ask Codex's interactive skill picker to list skills or invoke one by its name; runtime routing of every project skill remains unverified. |
| Settings | The CLI exposes sandbox choices, approval controls, and configuration overrides. The read-only probe used `--sandbox read-only` and `--ephemeral`. No repository-level `.codex/config.toml` settings file was present during the original probe; `.codex/agents/` now contains the project adapters. | It is not supported to rely on `.claude/settings.json` deny rules as Codex enforcement. | Codex interpretation of `.claude/settings.json` and any project-level Codex settings were not tested. |
| `tools` metadata | The shared role files use Claude Code tool names, as documented in `AGENTS.md`. Codex CLI exposes its own shell, filesystem, and other tools. | Agent `tools` metadata is not an OS-level security boundary; Codex sandbox and approval controls govern command execution. | A mapping from Claude `tools` frontmatter to Codex tool availability is not established. Whether Codex honors this frontmatter and how it maps individual tool names remain unknown. |
| Deploy-deny rules | Shared release guidance in `AGENTS.md` reserves Maven Central publishing for the maintainer. This safeguard is independent of client settings and was not changed. | `.claude/settings.json` is unsupported as a Codex deployment security control in this assessment. | Codex enforcement of deploy-deny rules was not tested. No deployment or publishing command was run. |

## Selected workaround and residual discovery

Use the repository's root `AGENTS.md` and `.agents/skills/` as the Codex
instruction and skill sources. Keep release safeguards in shared instructions;
do not rely on client settings to prevent publishing.

Codex's native project-agent adapters live in `.codex/agents/*.toml`. Each
adapter points to its canonical role instructions in `.agents/agents/`; keep
role content in the shared Markdown file rather than copying it into TOML. The
Markdown `tools` frontmatter is not a Codex permission mapping, and role
instructions such as “read-only” do not replace the parent session's sandbox
and approval controls. Codex subagents inherit those controls.

The runtime probe attempted a harmless invocation of the shared read-only
reviewer, but delegation failed because the runtime could not load its thread.
It also inspected the shared journal command without running it. These results
do not establish that Codex can invoke the repository's Markdown agents or
slash commands.

Residual discovery is intentionally accepted:

- `CLAUDE.md` also points to `AGENTS.md`, so Codex may discover duplicate
  instruction content depending on its instruction-file rules.
- `.claude/agents`, `.claude/commands`, and `.claude/skills` are symlinks to
  `.agents/`; whether Codex examines these paths was not tested.
- `.claude/settings.json` remains client-specific and is not a Codex security
  boundary.

This is not strict isolation between clients. Codex sandbox and approval
controls are separate from agent `tools` metadata; the CLI help explicitly
distinguishes its sandbox and approval controls.

## Verification performed

- `codex --version` — reported `codex-cli 0.171.0`.
- `codex --help`, `codex exec --help`, `codex agents --help`, and
  `codex features list` — inspected available CLI surfaces and feature states.
  This CLI reports `multi_agent` as deprecated but enabled and
  `multi_agent_v2` as stable but disabled; `codex agents --help` opens the
  agent command center rather than exposing a non-interactive agent listing.
- `codex exec --ephemeral --sandbox read-only --json ...` — read-only repository
  discovery; found instruction, agent, command, and skill files. Reviewer
  delegation failed in the runtime thread store. No files were changed and no
  checks or release operations were run.
- After adding the TOML adapters, an explicit read-only delegation probe for
  `security-auditor` reached Codex's collaboration tool but failed to load its
  thread (`no rollout found`). This leaves adapter recognition and invocation
  unverified. The probe made no repository changes.
- Retrying with `multi_agent_v2` explicitly enabled returned the requested
  canonical role path, but no distinct agent thread was exposed in the output.
  Adapter invocation therefore remains unverified. The probe made no
  repository changes.
- On 2026-11-22, the interactive CLI with `multi_agent_v2` enabled opened a
  separate Codex conversation through `codex_tui.create_thread`. That
  conversation attempted to create another thread and waited for approval;
  the request was canceled. It did not read the role file, so this did not
  verify invocation of the `.codex/agents/security-auditor.toml` adapter.
- An initial invocation from the shell sandbox could not initialize the
  in-process app server (`Operation not permitted`); the read-only probe was
  then run with approval outside that sandbox.

## Official sources

Reviewed 2026-11-21:

- [Codex prompting guide: `AGENTS.md` instructions](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide)
- [Codex CLI](https://developers.openai.com/codex/cli)
- [Codex skills](https://developers.openai.com/codex/skills)
- [Codex subagents](https://developers.openai.com/codex/subagents)
- [Codex configuration reference](https://developers.openai.com/codex/config-reference)
- [Using skills to accelerate OSS maintenance](https://developers.openai.com/blog/skills-agents-sdk)
- [Iterating development workflows with Codex](https://developers.openai.com/cookbook/examples/codex/iterating-development-workflows-with-codex)
