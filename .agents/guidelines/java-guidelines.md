# Java Guidelines

These are the general guidelines for writing Java code.

## Table of Contents

- [1. Naming Conventions](#1-naming-conventions)
- [2. Code Layout](#2-code-layout)
- [3. Best Practices for Classes, Interfaces, and Enums](#3-best-practices-for-classes-interfaces-and-enums)
- [4. Exception Handling](#4-exception-handling)
- [5. Concurrency](#5-concurrency)
- [6. Use of `Optional`](#6-use-of-optional)
- [7. Stream API Best Practices](#7-stream-api-best-practices)
- [8. Collections](#8-collections)
- [9. Date and Time](#9-date-and-time)
- [10. Strings](#10-strings)
- [11. Design Principles](#11-design-principles)
- [12. Defensive Practices](#12-defensive-practices)

## 1. Naming Conventions

Follow the Java naming conventions.

- Package names should be all lowercase, without underscores or other special characters. They should be short, meaningful, and based on the project's domain.
- Class and interface names should be in `PascalCase` and should be nouns or noun phrases.
- Method names should be in `camelCase` and should be verbs or verb phrases.
- Variable names should be in `camelCase` and should be short and meaningful. Avoid single-letter variable names except for loop counters.
- Constant names should be in `UPPER_SNAKE_CASE`.

## 2. Code Layout

- Use 4 spaces for indentation. Do not use tabs.
- Consistent indentation is crucial for readability. Using spaces instead of tabs ensures that the code looks the same on all systems.
- Keep lines of code under 120 characters.
- When wrapping lines, break after a comma or an operator. Indent the new line with 8 spaces.

## 3. Best Practices for Classes, Interfaces, and Enums

- Prefer using Java records for storing data holder classes.
- Prefer immutable classes whenever possible.
- Program to interfaces, not implementations.
- Use enums instead of string constants or integer constants.

## 4. Exception Handling

- Catch specific exceptions instead of `Exception`, `RuntimeException` or `Throwable`.
    - Catching `Exception`, `RuntimeException` or `Throwable` is acceptable only in narrow, well-justified places (top-level handlers, framework boundaries, or when you must guarantee cleanup/logging).
      You should treat such catches as last-resort, log/record full context, and rethrow or terminate appropriately.
- Never ignore exceptions. If you catch an exception, either handle it or rethrow it.

## 5. Concurrency

- Prefer the high-level concurrency utilities in the `java.util.concurrent` package over low-level primitives like `wait()` and `notify()`.
- Use `volatile` only for simple atomic operations. For more complex operations, use `java.util.concurrent.atomic` or locks.

## 6. Use of `Optional`

- Use `Optional` for return types when a method might not return a value.
- Do not use `Optional` for class fields or method parameters.

## 7. Stream API Best Practices

- Avoid side effects in stream operations like `map()` and `filter()`.
- Prefer method references over lambdas when possible.

## 8. Collections

- Choose the right collection for the job. Use `List` for ordered collections, `Set` for unordered collections with no duplicates, and `Map` for key-value pairs.
- Use `isEmpty()` to check if a collection is empty.
- Methods that return collections should return an empty collection instead of `null`.
- Use the diamond operator (`<>`) for generic type inference.
- Prefer the `for-each` loop for iterating over collections.

## 9. Date and Time

- Prefer using the Java 8 Date-Time API (`java.time.*`) over the legacy `java.util.Date`/`java.util.Calendar` APIs.

## 10. Strings

- Use multiline text blocks (`"""`), available since Java 15, for multi-line string literals (e.g., SQL, JSON, XML) instead of concatenation or newline escapes.

## 11. Design Principles

- Follow SRP: each class and method has a single responsibility.
- Follow DRY: extract shared logic instead of copying it.
- Follow KISS: prefer the simplest solution that works.
- Follow YAGNI: do not add features, options, or abstractions "just in case".
- Use names that reveal intent (see §1).
- Keep methods short and focused on one thing (as a guide, about 20–30 lines); split a method when it does more than one thing, not to reach a line count.
- Keep public method parameter lists to three or fewer; for more options, use a builder (see the [architecture guidelines](architecture-guidelines.md#3-design-patterns)).
- Replace magic numbers with named constants.
- Prefer explicit, readable code over clever code.
- Keep interfaces small so callers do not depend on methods they do not use (this applies to instance methods; the static factories on `Masker` are intentional).
- Expose configuration that users need to change through builder or factory parameters, with sensible defaults (see the [architecture guidelines](architecture-guidelines.md#4-extensibility-guidelines)). Do not rely on global mutable state.
- For composition over inheritance and separation of concerns, follow the [architecture guidelines](architecture-guidelines.md).
- Avoid:
    - Premature optimization.
    - Nesting deeper than three levels; use guard clauses or extract methods instead.
    - God classes or methods.
    - Hidden side effects.
    - Tight coupling between classes.
- Apply the rules in this section and §12 to new and changed code. Do not refactor untouched code or change existing public signatures only to meet them.

## 12. Defensive Practices

- Use guard clauses at method entry to handle edge cases early.
- Validate configuration arguments in builders and static factory methods and fail fast, where invalid means what the Javadoc says is not allowed (documented values, such as a fixed length <= 0, stay valid). Throw `IllegalArgumentException` for invalid arguments, `IllegalStateException` for an incomplete builder at `build()`, and `NullPointerException` for null collaborators (maskers, mappers); otherwise keep the method's documented exception for null configuration values. Use `MaskingException` only for failures while masking (see the [architecture guidelines](architecture-guidelines.md#4-extensibility-guidelines)). New validation of an existing parameter is a breaking change that needs a `Changed` entry in `CHANGELOG.md` and a `!` commit header (see [`AGENTS.md` → *Handling breaking changes*](../../AGENTS.md#handling-breaking-changes)).
- Do not add new rejections of `apply` input to existing maskers: return null for null input and keep each masker's documented handling of blank or unexpected values. New maskers document their input handling in Javadoc and report input they cannot process with `MaskingException`. Changing what a public method accepts or throws is a breaking change.
- Exception messages, including those of wrapped causes, must never include the input being masked. Configuration values such as patterns may appear.
- Never log the input being masked or its masked output; log metadata and field names only (see the [architecture guidelines](architecture-guidelines.md#5-security-considerations)).
- Prefer immutability (see §3 and the [immutability and thread-safety principle](architecture-guidelines.md#1-architectural-principles)).
- Handle null explicitly and mark nullable types with JSpecify `@Nullable` (enforced by NullAway).
- Do not return null except where a documented contract requires it, such as the `apply` methods of the built-in maskers (other than `Masker.delegate`, whose result depends on the supplied function), which return null for null input.
- Return empty collections rather than null (see §8).
