---
name: conventional-comments
description: Writes review feedback in the Conventional Comments format (`<label> [decorations], <subject>` plus optional discussion), using labels such as praise, issue, suggestion, question, nitpick, todo, and chore, and decorations such as blocking, non-blocking, and if-minor. Use when reviewing code, pull requests, plans, or documentation, when posting PR review comments, or when reporting review findings so each comment's intent and severity are clear.
---

# Conventional Comments

Source: [Conventional Comments](https://conventionalcomments.org/) (CC BY 3.0). This skill restates the format and applies it to JMaskify reviews.

A label at the start of each comment tells the author what kind of feedback it is and whether it must be resolved. That makes the comment's intent and tone clear, prompts more actionable feedback, and lets tools parse it.

## Format

```text
<label> [decorations]: <subject>

[discussion]
```

- **label** (required): one label saying what kind of comment this is.
- **decorations** (optional): extra labels in parentheses, comma-separated with no spaces, such as `(security,non-blocking)`.
- **subject** (required): the main message, in one line.
- **discussion** (optional): the reasoning, context, and next steps the author needs to resolve the comment.

In Markdown (GitHub PR comments), bold the label and decorations:

```markdown
**suggestion (test,if-minor):** Add a case for an empty JSON array.

`JsonMaskerTests` covers objects and nested arrays, but not `[]`.
```

## Labels

| Label        | Meaning                                                                                 | Blocking?           |
|--------------|-----------------------------------------------------------------------------------------|---------------------|
| `praise`     | Something positive. Must be sincere: no false praise.                                    | Never               |
| `nitpick`    | A trivial, preference-based request.                                                     | Never               |
| `suggestion` | A proposed improvement. Say what to change and why it's better.                          | Use a decoration    |
| `issue`      | A specific problem (user-facing or internal). Pair it with a suggestion where possible.  | Usually             |
| `todo`       | A small, trivial, but necessary change.                                                  | Usually             |
| `question`   | A possible concern you aren't sure about. Ask the author to clarify or investigate.      | Use a decoration    |
| `thought`    | An idea that came up while reviewing. Could lead to a follow-up or mentoring.            | Never               |
| `chore`      | A process task needed before acceptance. Link the process docs so the author can do it.  | Usually             |
| `note`       | Something the reader should be aware of.                                                 | Never               |

Optional labels for extra nuance:

- `typo`: like `todo`, for a misspelling.
- `polish`: like `suggestion`, when nothing is wrong but quality can be improved right away.
- `quibble`: like `nitpick`, with a lighter tone.

**Choosing between them:**

- If you're sure something is wrong, use `issue`. If you're not sure, use `question`.
- If the change is small and mechanical, use `todo`. If it needs thought or discussion, use `suggestion` or `issue`.
- If it's only your taste, use `nitpick`.

## Decorations

- `(blocking)`: must be resolved before the change is accepted.
- `(non-blocking)`: shouldn't stop the change from being accepted.
- `(if-minor)`: resolve it only if the fix turns out to be minor or trivial.

Topic decorations group comments that share a label. Use only this minimal set so they stay unambiguous: `security`, `test`, `docs`, `performance`, `api` (public API or backward compatibility), `null-safety`, and `build` (`pom.xml`, CI, dependencies).

Use at most two decorations per comment: one blocking decoration and one topic. More than that hurts readability.

## JMaskify conventions

- **Be explicit about blocking.** On every `suggestion`, `question`, and `issue`, add `(blocking)` or `(non-blocking)`. This matches the reviewer's duty to separate blocking issues from suggestions.
- **Order by severity.** List blocking comments first, then non-blocking ones, then `praise`, `thought`, and `note`.
- **Anchor every comment** to a `file:line` reference (or post it inline on that line).
- **Include at least one `praise`** per review when something genuinely deserves it.
- **Map checklist failures to labels.** A failed item from `AGENTS.md` → *Code Review Checklist* is an `issue (blocking)`: missing tests, NullAway or null-safety problems, sensitive data in logs or exceptions, ReDoS-prone regexes, or a breaking API change without `!`. Process items such as a missing `CHANGELOG.md` entry, unformatted code (`mvn spotless:apply`), or a commit header that breaks the convention (see the `conventional-commits` skill) are `chore` or `todo`.
- **Never quote real sensitive values** in a comment. Describe the data shape instead.

## Examples

```markdown
**issue (blocking,security):** `CreditCardMasker` logs the raw input when parsing fails.

`CreditCardMasker.java:58` passes `input` to `log.warn`. Log only the input length,
or drop the value from the message.
```

```markdown
**suggestion (non-blocking):** Use `String.repeat` instead of the manual loop.

`"*".repeat(length)` is shorter and has the same behavior (Java 11+).
```

```markdown
**question (blocking,performance):** Can `(\d+\s?)+` backtrack catastrophically on a long digit run?

If so, an atomic group or a possessive quantifier would bound it.
```

```markdown
**chore:** Run `mvn spotless:apply`. `JsonMasker.java` isn't formatted.
```

```markdown
**nitpick:** `maskedVal` → `maskedValue` for consistency with the rest of the class.
```

```markdown
**thought:** This builder pattern could also serve `MultilinePatternMasker`. Worth a follow-up issue?
```

```markdown
**praise:** These parameterized tests cover the boundary cases very cleanly.
```

## Reviewing a plan or document

The same format works for plans, RFCs, and documentation reviews. Use `question` for unclear requirements, `issue` for steps that are wrong or missing, and `suggestion` for alternatives.
