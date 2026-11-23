# OpenAI Codex CLI Compatibility

**Observed with OpenAI Codex CLI 0.171.0 on 2026-11-22.** This record covers
JMaskify-specific observations, not a guarantee for other Codex versions or
environments.

JMaskify keeps `.agents/` provider-neutral. The root `AGENTS.md` remains the
shared instruction entry point. Canonical command workflows remain in
`.agents/commands/`; thin Codex skill adapter files live in `.codex/skills/`.
Symlink entries in `.agents/skills/` expose those adapter directories through
Codex's documented project skill scan. The current runtime listed all five
adapter skills from `.codex/skills/`; the CLI's interactive picker and skill
activation were not separately tested.

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
| Agents | The probe found the eight shared role definitions in `.agents/agents/`, including the read-only `java-code-reviewer`. Codex documentation describes project-scoped custom agents as TOML files under `.codex/agents/`; this repository now has one thin TOML adapter per shared role, each pointing to its canonical Markdown role file. On 2026-11-22, Codex CLI 0.171.0 accepted native delegation to `security-auditor`; a clean read-only run returned `.agents/agents/security-auditor.md`, and a follow-up returned `1. **Masking Integrity**`, matching the first responsibility heading in that file. | The shared Markdown role definitions alone are not Codex's documented custom-agent file format or location. | The JSON event stream did not expose a distinct receiver thread ID. A further invocation logged `no rollout found` while still returning the matching role-specific heading, so that invocation's thread lifecycle is unclear. |
| Commands | The probe found `.agents/commands/docs/dev/dev-journal-create.md` and inspected its purpose: generating a development progress journal, with preview before saving. A prompt-level mapping to Codex adapter paths is provided below. | Codex CLI 0.171.0 did not recognize `/docs:dev:dev-journal-create` as a custom slash command in the interactive composer. | Codex behavior when a legacy ID is mentioned as ordinary prompt text is not guaranteed; follow the documented adapter path mapping. |
| Skills | OpenAI documents project skill discovery under `.agents/skills/` and following symlinked skill folders. Codex adapter files live under `.codex/skills/`, with matching `.agents/skills/` symlink entries. The current runtime's available-skill catalog listed all five adapter names with paths under `.codex/skills/`, verifying catalog discovery in this runtime. | — | Codex CLI 0.171.0's interactive `/skills` picker and `$skill-name` activation were not manually selected in this probe; those exact interactions remain unverified. |
| Settings | The CLI exposes sandbox choices, approval controls, and configuration overrides. The read-only probe used `--sandbox read-only` and `--ephemeral`. No repository-level `.codex/config.toml` settings file was present during the original probe; `.codex/agents/` now contains the project adapters. | It is not supported to rely on `.claude/settings.json` deny rules as Codex enforcement. | Codex interpretation of `.claude/settings.json` and any project-level Codex settings were not tested. |
| `tools` metadata | The shared role files use Claude Code tool names, as documented in `AGENTS.md`. Codex CLI exposes its own shell, filesystem, and other tools. | Agent `tools` metadata is not an OS-level security boundary; Codex sandbox and approval controls govern command execution. | A mapping from Claude `tools` frontmatter to Codex tool availability is not established. Whether Codex honors this frontmatter and how it maps individual tool names remain unknown. |
| Deploy-deny rules | Shared release guidance in `AGENTS.md` reserves Maven Central publishing for the maintainer. This safeguard is independent of client settings and was not changed. | `.claude/settings.json` is unsupported as a Codex deployment security control in this assessment. | Codex enforcement of deploy-deny rules was not tested. No deployment or publishing command was run. |

## Selected workaround and residual discovery

Use the repository's root `AGENTS.md` as the Codex instruction entry point.
Keep canonical workflows in `.agents/commands/` and Codex adapter pointers in
`.codex/skills/`. The `.agents/skills/` symlinks expose those adapters through
the documented project skill scan. The current runtime also listed the five
skills from their `.codex/skills/` paths. Keep release safeguards in shared
instructions; do not rely on client settings to prevent publishing.

### Prompt-level command mapping

Codex CLI 0.171.0 did not expose custom colon-delimited command IDs in the
interactive slash-command picker. When the user refers to one of these IDs in
ordinary prompt text, use its adapter path as a prompt-level reference and
read that file; this mapping does not create a native slash command or
change the legacy ID into a native slash command. The runtime skill catalog
listed the adapter skill names; invoke those by skill name when available.

| Legacy command ID | Adapter path | Canonical workflow |
|---|---|---|
| `docs:dev:dev-journal-create` | `.codex/skills/docs-dev-journal-create/SKILL.md` | `.agents/commands/docs/dev/dev-journal-create.md` |
| `docs:dev:dev-journal-update` | `.codex/skills/docs-dev-journal-update/SKILL.md` | `.agents/commands/docs/dev/dev-journal-update.md` |
| `docs:dev:dev-plan-execute` | `.codex/skills/docs-dev-plan-execute/SKILL.md` | `.agents/commands/docs/dev/dev-plan-execute.md` |
| `docs:dev:dev-plan-init` | `.codex/skills/docs-dev-plan-init/SKILL.md` | `.agents/commands/docs/dev/dev-plan-init.md` |
| `release:release-prepare` | `.codex/skills/release-prepare/SKILL.md` | `.agents/commands/release/release-prepare.md` |

Codex's native project-agent adapters live in `.codex/agents/*.toml`. Each
adapter points to its canonical role instructions in `.agents/agents/`; keep
role content in the shared Markdown file rather than copying it into TOML. The
Markdown `tools` frontmatter is not a Codex permission mapping, and role
instructions such as “read-only” do not replace the parent session's sandbox
and approval controls. Codex subagents inherit those controls.

The initial runtime probe attempted a harmless invocation of the shared
read-only reviewer, but delegation failed because the runtime could not load
its thread. The 2026-11-22 follow-up successfully invoked the native
`security-auditor` custom agent with `multi_agent_v2` enabled: it returned the
canonical role path, then a role-specific heading matching that file. It also
inspected the shared journal command without running it; a separate composer
probe later confirmed that the colon-delimited command is not recognized as a
custom slash command.

Residual discovery is intentionally accepted:

- `CLAUDE.md` also points to `AGENTS.md`, so Codex may discover duplicate
  instruction content depending on its instruction-file rules.
- `.claude/agents`, `.claude/commands`, and `.claude/skills` are symlinks to
  `.agents/`; Claude may also see the Codex adapter aliases through
  `.claude/skills`. Whether Codex examines the `.claude/` paths was not tested.
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
- On 2026-11-22, `codex exec --enable multi_agent_v2 --ephemeral --sandbox
  read-only --json` natively delegated to `security-auditor`. One completed run
  returned `.agents/agents/security-auditor.md`. A follow-up asked the agent
  to read that file and report its first responsibility heading; the result
  was `1. **Masking Integrity**`, which matches the file. The follow-up event
  stream did not expose a receiver thread ID and included a `no rollout found`
  log, so only the first completed delegation is treated as clean runtime
  evidence; the second response corroborates role-specific behavior.
- On 2026-11-22, this runtime exposed the five Codex command adapter skills in
  its available-skill catalog, with paths under `.codex/skills/`. Their
  `.agents/skills/` symlink entries also resolve to the adapter `SKILL.md`
  files. This verifies metadata discovery in the current runtime; an
  interactive `/skills` selection or `$skill-name` activation was not tested.
- On 2026-11-22, the interactive Codex CLI 0.171.0 composer did not recognize
  `/docs:dev:dev-journal-create` as a custom slash command. Five pointer-only
  adapters were added under `.codex/skills/`, and matching symlink entries
  under `.agents/skills/` target those adapter directories. The current
  runtime listed all five skills; interactive `/skills` selection and
  `$skill-name` activation in CLI 0.171.0 remain unverified.
- An initial invocation from the shell sandbox could not initialize the
  in-process app server (`Operation not permitted`); the read-only probe was
  then run with approval outside that sandbox.

## Official sources

Reviewed 2026-11-22:

- [Codex prompting guide: `AGENTS.md` instructions](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide)
- [Codex CLI](https://developers.openai.com/codex/cli)
- [Codex skills](https://developers.openai.com/codex/skills)
- [Codex subagents](https://developers.openai.com/codex/subagents)
- [Codex configuration reference](https://developers.openai.com/codex/config-reference)
- [Using skills to accelerate OSS maintenance](https://developers.openai.com/blog/skills-agents-sdk)
- [Iterating development workflows with Codex](https://developers.openai.com/cookbook/examples/codex/iterating-development-workflows-with-codex)
