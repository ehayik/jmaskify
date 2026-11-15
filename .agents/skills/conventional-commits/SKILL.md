---
name: conventional-commits
description: Writes and checks git commit messages that follow the Conventional Commits 1.0.0 specification (type, optional scope, `!` for breaking changes, body, and git-trailer footers). Use when writing a commit message, choosing a commit type or scope, marking a breaking change, squash-merging or titling a pull request, or reviewing whether existing commits follow the convention.
---

# Conventional Commits

Source: [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/#specification) (CC BY 3.0). This skill restates the specification and applies it to JMaskify. When the two disagree, the specification wins on syntax and `AGENTS.md` wins on project conventions.

## Message structure

```text
<type>[optional scope][!]: <description>

[optional body]

[optional footer(s)]
```

- **Header** (required): a type, an optional scope in parentheses, an optional `!`, then a colon and a single space, then the description.
- **Body** (optional): starts one blank line after the header. Free-form, any number of paragraphs.
- **Footers** (optional): start one blank line after the body (or the header, if there is no body).

## Rules

The keywords MUST, SHOULD, and MAY follow RFC 2119.

1. The header MUST start with a type, followed by an optional scope, an optional `!`, and a required `: ` (colon and space).
2. Use `feat` when the commit adds a feature, and `fix` when it fixes a bug. Both are required by the specification.
3. A scope MAY follow the type. It MUST be a noun naming a section of the codebase, in parentheses: `fix(json-masker): ...`.
4. The description MUST come right after `: `. It is a short summary of the change.
5. The body MUST begin one blank line after the description.
6. Each footer MUST be a token, then `: ` or ` #`, then a value: `Refs: #123`, `Fixes #42`, `Reviewed-by: Jane`.
7. Footer tokens MUST use `-` instead of spaces (`Co-Authored-By`, `Acked-by`). The only exception is `BREAKING CHANGE`.
8. A footer value MAY span spaces and lines. It ends where the next valid `token: ` or `token #` begins.
9. A breaking change MUST be marked by `!` right before the `:` in the header, by a `BREAKING CHANGE: <description>` footer, or both.
10. `BREAKING CHANGE` MUST be uppercase. `BREAKING-CHANGE` means the same thing as a footer token.
11. If the header uses `!`, the footer MAY be omitted. The description then SHALL describe the breaking change.
12. Types other than `feat` and `fix` MAY be used. Apart from `BREAKING CHANGE`, tools treat every part as case-insensitive, but stay consistent.

## Types used in JMaskify

`AGENTS.md` → *Commit Message Format* defines the project's types. Use lowercase.

| Type       | Use for                                                          | SemVer effect | `CHANGELOG.md` section          |
|------------|------------------------------------------------------------------|---------------|---------------------------------|
| `feat`     | A new user-facing capability (new masker, new builder option)    | MINOR         | Added                           |
| `fix`      | A bug fix, including dependency security fixes (`fix(deps)`)     | PATCH         | Fixed                           |
| `docs`     | README, Javadoc-only, agent files, guidelines                    | none          | usually none                    |
| `style`    | Formatting only, no behavior change                              | none          | none                            |
| `refactor` | Code change that neither fixes a bug nor adds a feature          | none          | none                            |
| `test`     | Adding or fixing tests only                                      | none          | none                            |
| `chore`    | Build, CI, release, and tooling (`chore(release)`, `chore(ci)`)  | none          | none                            |
| any + `!`  | A breaking change to the public API or behavior                  | MAJOR         | Changed (call out as breaking)  |

The specification also mentions `build`, `ci`, and `perf` (from the Angular convention). JMaskify folds build and CI work into `chore`, so don't introduce new types without discussing it first.

**Common scopes** (taken from the history): `json-masker`, `deps`, `readme`, `agents`, `release`, `test`. Use the masker's kebab-case name for masker-specific changes. Leave the scope out when the change is cross-cutting.

## Writing a good message

- **Description**: imperative mood ("add", not "added" or "adds"), lowercase first letter, no trailing period. Aim for 72 characters or fewer in the whole header.
- **Body**: explain *what* changed and *why*, not how. Wrap lines at about 72 characters.
- **One change per commit**: if a commit fits more than one type, split it into several commits where possible.
- **Never put sensitive data** (secrets, tokens, real masked values) in any part of the message.
- **Attribution and references** go in footers: `Fixes #42`, `Refs: #123`, `Co-Authored-By: ...`.

## Examples

Short commit with no body:

```text
docs(readme): correct the Base64 masker example
```

Feature with a body and an issue reference:

```text
feat(json-masker): add support for nested array masking

Implement recursive masking for nested arrays in JSON structures.
This allows users to mask sensitive data at multiple levels of nesting.

Fixes #42
```

Breaking change marked in the header only:

```text
feat(api)!: return Optional from Masker.of for unknown types
```

Breaking change with both `!` and a footer:

```text
refactor!: drop the deprecated FixedLengthMasker constructor

BREAKING CHANGE: use Masker.fixedLength() instead of
new FixedLengthMasker().
```

Revert (the specification leaves reverts to tooling; this is its recommended pattern):

```text
revert: restore the previous credit card masking pattern

Refs: 676104e, a215868
```

## Checking a commit

Run this check before committing, or on existing commits during review:

```bash
# List commits on the branch whose header does not match the convention
git log --format=%s master..HEAD \
  | grep -vE '^(feat|fix|docs|style|refactor|test|chore|revert)(\([a-z0-9-]+\))?!?: [^ ].*' \
  || echo "All headers follow Conventional Commits"
```

Then check by hand what the regex can't:

- The type matches the change (a new feature is not a `fix`, and a behavior change is not a `refactor`).
- Every breaking public API change has `!` or a `BREAKING CHANGE:` footer.
- A blank line separates the header, body, and footers.

## Fixing mistakes

- **Wrong type before merge**: rewrite the message (`git commit --amend` for the last commit, or an interactive rebase run by the user, since agents can't use `git rebase -i`).
- **Wrong type after merge**: leave history alone. Fix the `CHANGELOG.md` entry instead.
- **Contributor commits that don't follow the convention**: with "Squash and merge", the maintainer can write a correct message for the squashed commit.
