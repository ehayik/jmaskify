# GitHub Copilot CLI Compatibility

**Observed with GitHub Copilot CLI 1.0.114 on 2026-11-21.** This matrix
documents JMaskify-specific observations, not a guarantee for other CLI
versions or environments.

JMaskify keeps `.agents/` provider-neutral. `.claude/agents`,
`.claude/commands`, and `.claude/skills` link to shared resources.

## Verification matrix

Classification means:

- **Verified**: direct CLI observation or explicit bundled/public documentation.
- **Unsupported**: behavior is not available or is not a supported scope
  of the relevant control. Unsupported is not used for behaviors that simply
  have not been established.
- **Unknown**: evidence does not establish a general runtime behavior.

| Area | Verified support | Unsupported behavior | Unknown behavior / limitation |
|---|---|---|---|
| Instructions | `copilot instruction list --json` found `.github/copilot-instructions.md`, `AGENTS.md`, and `CLAUDE.md` as enabled repository sources. The session `/instructions` control can enable or disable individual discovered instruction files, including `CLAUDE.md`. | `/instructions` does not exclude agents or commands. `--no-custom-instructions` is not a Claude-only switch: it disables custom instructions broadly. There is no verified repository/session-wide control that excludes every `.claude/` resource. | Discovery and precedence in other environments or later CLI releases are not established here. |
| Agent | `java-code-reviewer` was invoked with `copilot --agent java-code-reviewer` and returned `READY`. Public docs support `.github/agents/*.agent.md`; bundled CLI docs also identify `.claude/agents`. | — | Portability of arbitrary custom-agent frontmatter or schema features is unknown. Exact handling of Claude-style agent frontmatter fields other than the tested `tools` availability is unknown. |
| Command | `/conventional-commits` was discovered and returned `VALID`; `/docs:dev:dev-journal-create` was recognized and displayed its purpose without making changes. Bundled CLI docs identify `.claude/commands`. | There is no verified comprehensive opt-out for `.claude/commands`. | Runtime behavior for command frontmatter and every shared command is unknown. |
| Skill | `copilot skill list` listed project skills, including `build-review`, `conventional-comments`, `conventional-commits`, `github-issues`, and `sofa`. Public docs support project skills in `.github/skills`, `.claude/skills`, and `.agents/skills`. | Skills disabled by name or excluded by `ignoredSkillsLocations` do not provide a repository/session-wide exclusion of agents or commands. | The current runtime may differ from future CLI releases; skill execution details for all project skills were not exercised. |
| Settings | The CLI session and permission controls are verified separately: `--allow-tool`, `--deny-tool`, and available/excluded tool sets. Official CLI documentation places user settings and persisted permissions under `~/.copilot/settings.json` and `~/.copilot/permissions-config.json`. | It is not supported to rely on repository `.claude/settings.json` deploy-deny semantics as a Copilot enforcement mechanism. | Repository-level handling of `.claude/settings.json` for deploy-deny rules is unknown. Its documented repository support is for hooks, enabled plugins, and extra marketplaces. `ignoredSkillsLocations` is documented under user settings and is not a repo-wide all-resource exclusion. |
| `tools` metadata | In safe probes, agent `tools` metadata affected the tools offered or attempted in the tested cases: `Read`→`view`, `Grep`→`rg`, `Glob`→`glob`, `Bash`→`bash`, `Edit`/`Write`→`apply_patch`, and `WebFetch`→`web_fetch`. Agent tool availability is model-facing and separate from OS-level permissions. CLI `--deny-tool` can still block matching requests, including a harmless shell request and an edit, even with an allow rule. | It is not an OS-level security boundary. | Claude-style frontmatter such as `tools: Read, Edit, ...` is not established as generally supported by the Copilot runtime. `WebSearch` mapping is unknown: it was not exposed in two probes, including an explicit available-tool set. |
| Deploy-deny rules | Shared release instructions prohibit agent publishing and reserve the publish step for the maintainer. That safeguard is independent of provider settings and was not changed to depend on `.claude/settings.json`. | No repository `.claude/settings.json` deploy-deny behavior is established for Copilot CLI. It is therefore unsupported as a deployment security control in this compatibility assessment. | Whether Copilot interprets or enforces deploy-deny rules was not established, and no deployment/publish command was run. |

## Selected workaround and residual discovery

For a Copilot CLI session, use `/instructions` to disable the discovered
`CLAUDE.md` entry when that control is available. This is the approved
partial workaround for instruction loading. It does not disable
`AGENTS.md`, `.github/copilot-instructions.md`, agents, commands, skills,
or settings.

In this checkout, `CLAUDE.md` contains only `@AGENTS.md`, so leaving both
instruction sources enabled loads the same shared instructions twice. This
duplication concern applies to Copilot sessions that discover both files.

Residual discovery is intentionally accepted:

- `.claude/agents` may expose shared agents.
- `.claude/commands` may expose shared commands.
- `.claude/skills` may expose shared skills.
- `.claude/settings.json` may be interpreted only for documented Claude
  settings behaviors; deploy-deny semantics in Copilot remain unknown.

This boundary is not strict isolation and does not assert repository-enforced
Copilot isolation. Do not use `.claude/settings.json` as the sole release
safeguard; the shared maintainer-only release rule applies across clients.

## Runtime tool controls

- `--available-tools` restricts what the model can request.
- `--excluded-tools` removes specified tools from the model-facing set.
- `--allow-tool` permits matching requests without prompting.
- `--deny-tool` denies matching requests and takes precedence over allow rules.

The CLI documentation warns that shell commands run with the user's account
permissions. An agent's `tools` list or a CLI availability set is therefore
not a substitute for OS sandboxing or least-privilege account controls.

## Official sources

Reviewed 2026-11-21:

- [GitHub Copilot CLI overview](https://docs.github.com/en/copilot/how-tos/copilot-cli)
- [Adding custom instructions](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/add-custom-instructions)
- [Invoking custom agents](https://docs.github.com/en/copilot/how-tos/copilot-cli/use-copilot-cli/invoke-custom-agents)
- [Custom agents configuration reference](https://docs.github.com/en/copilot/reference/custom-agents-configuration)
- [Adding skills](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/add-skills)
- [Allowing and denying tools](https://docs.github.com/en/copilot/how-tos/copilot-cli/use-copilot-cli/allowing-tools)
- [Configuring Copilot CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli/set-up-copilot-cli/configure-copilot-cli)
