# JMaskify Architecture Guidelines

These guidelines describe the architectural principles, core components, and design patterns used in the JMaskify project to ensure consistency, extensibility, and maintainability.

## Table of Contents

- [1. Architectural Principles](#1-architectural-principles)
- [2. Core Components](#2-core-components)
- [3. Design Patterns](#3-design-patterns)
- [4. Extensibility Guidelines](#4-extensibility-guidelines)
- [5. Security Considerations](#5-security-considerations)

## 1. Architectural Principles

**The library is built on principles of simplicity, immutability, and functional programming.**

- **Functional Approach**: The core `Masker<T>` interface extends `java.util.function.Function<T, String>`. Masking operations are treated as transformations from an input type to a masked string representation.
- **Fluent API**: Object creation and configuration are handled through a fluent API, primarily using the Builder pattern, to provide an intuitive and readable interface for users.
- **Immutability**: Once configured and built, `Masker` instances should be immutable and thread-safe to ensure predictable behavior in concurrent environments.
- **Minimal Dependencies**: The library aims to keep its dependency footprint small, relying only on essential libraries like Jackson for JSON and SLF4J for logging.

## 2. Core Components

**The architecture is centered around a few key abstractions that define how masking is performed.**

- **`Masker<T>`**: The central interface for all masking strategies. It is a `sealed interface` to control its hierarchy and ensure that all core implementations are known.
- **Implementations**:
    - `FixedLengthMasker`: Basic string obfuscation with fixed-length output.
    - `JsonMasker`: Specialized masker for JSON content using streaming API (Jackson) for efficiency.
    - `MultilinePatternMasker`: Regex-based masking for unstructured multiline text.
    - `Base64Masker`: Simple encoding-based masking.
    - `CreditCardMasker`: Specialized masking for financial data.
- **`MaskerBuilder<T, R>`**: A generic interface for builders that construct `Masker` instances. It ensures consistency across different builder implementations.

## 3. Design Patterns

**Several design patterns are employed to provide flexibility and ease of use.**

- **Strategy Pattern**: Different masking techniques are implemented as separate classes conforming to the `Masker` interface, allowing users to swap or combine strategies easily.
- **Builder Pattern**: Complex object construction (especially for `JsonMasker` and `MultilinePatternMasker`) is managed by inner `Builder` classes to handle many configuration options cleanly.
- **Static Factory Methods**: The `Masker` interface provides static methods (e.g., `Masker.json()`, `Masker.fixedLength()`) as the primary entry points for creating maskers, hiding implementation details.
- **Decorator / Function Composition**: Since `Masker` extends `Function`, maskers can be composed using `andThen()` to apply multiple masking layers in sequence.

### Example: Masker Composition
- Maskers can be chained: `jsonMasker.andThen(multilineMasker).apply(input)`.

## 4. Extensibility Guidelines

**Adding new masking capabilities should follow the established patterns to maintain architectural integrity.**

- **New Masking Strategies**:
    - Implement the `Masker<String>` interface (or another relevant input type).
    - If the implementation is a core part of the library, update the `permits` list in the `Masker` sealed interface.
    - Provide a static factory method in the `Masker` interface for easy access.
- **Custom Masking**:
    - For one-off or user-specific masking logic, use `Masker.delegate(Function<S, String> delegate)` instead of creating a new class.
- **Builders**:
    - Use the `MaskerBuilder` interface for any new complex masker that requires configuration.
    - Ensure builders are fluent and provide sensible defaults.
- **Error Handling**:
    - Use `MaskingException` for any runtime errors during the masking process to provide a consistent error-handling experience.

## 5. Security Considerations

- **No Logging of Sensitive Data**: Never log unmasked input data, parts of it, or masked data, in library logs. Debug or error logs should only contain metadata and field names.
- **Input Validation**: While the library handles common formats (JSON, multiline text), ensure that new masking strategies validate inputs to prevent potential injection or denial-of-service (DoS) through overly complex regex patterns (Catastrophic Backtracking).
- **Default Masking**: When in doubt, default to irreversible masking strategies. Reversible strategies must be clearly documented as such.
- **Dependency Management**: Regularly audit and update dependencies (especially Jackson) to mitigate vulnerabilities in third-party libraries used for data processing.
