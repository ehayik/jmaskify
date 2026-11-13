# JMaskify Project Overview

JMaskify is an open-source Java library designed to safeguard sensitive data with versatile and customizable masking techniques.

## Primary Programming Language
- **Java 17**: The project is built using Java 17 and leverages modern Java features like sealed interfaces.

## Project Structure
- `src/main/java`: Contains the core library logic and masker implementations.
- `src/test/java`: Contains unit and integration tests, ensuring high code quality and reliability.
- `scripts`: Includes automation scripts, such as Git pre-commit hooks for code formatting.
- `target`: Generated build artifacts (not tracked in version control).
- `pom.xml`: Maven configuration file defining dependencies, plugins, and build profiles.
- `README.md`: Main project documentation.
- `CHANGELOG.md`: Record of all notable changes to the project.
- `AGENTS.md`: Tool-agnostic guide for AI coding agents (entry point).
- `.agents`: Tool-agnostic agent definitions, commands, and guidelines. Tool-specific folders (e.g. `.claude`) only link to it.

## Architecture
The library follows a clean and extensible architecture centered around the `Masker<T>` interface.

### Core Components
- **Masker Interface**: A `sealed interface` that extends `java.util.function.Function<T, String>`. It serves as the foundation for all masking strategies.
- **Implementations**:
    - `JsonMasker`: Specifically designed for masking properties within JSON structures.
    - `MultilinePatternMasker`: Uses regular expressions to find and mask sensitive data across multiple lines.
    - `FixedLengthMasker`: Replaces input with a fixed-length string of substitution characters.
    - `Base64Masker`: Uses Base64 encoding as a simple obfuscation technique.
    - `CreditCardMasker`: Specialized masker for protecting credit card numbers while preserving partial visibility.
    - `DelegateMasker`: Allows for custom masking logic by delegating to a provided function.

### Design Patterns
- **Builder Pattern**: Extensively used (e.g., in `JsonMasker.Builder` and `MultilinePatternMasker.Builder`) to provide a fluent and readable API for configuring complex maskers.
- **Factory Pattern**: The `Masker` interface provides static factory methods for easy instantiation of various masker types.

## Development and Quality Tools
- **Maven**: Used for dependency management and build automation.
- **Lombok**: Reduces boilerplate code (e.g., getters, setters, builders).
- **Spotless**: Enforces consistent code formatting using the Palantir Java Format.
- **ErrorProne & NullAway**: Static analysis tools for catching bugs and ensuring null safety at compile time.
- **JUnit 5 & AssertJ**: Frameworks used for comprehensive testing.
- **JaCoCo**: Provides code coverage reports.
