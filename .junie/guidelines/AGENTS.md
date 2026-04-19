# JMaskify – AI Agent’s Guide

This document outlines coding standards, contribution practices, and development workflows for the JMaskify project.

## Table of Contents

- [Quick-start checklist](#quick-start-checklist)
- [Feature Checklist](#feature-checklist)
- [Code Style & Quality](#code-style--quality)
- [Testing Guidelines](#testing-guidelines)
- [Git Workflow](#git-workflow)
- [Documentation](#documentation)
- [Security Best Practices](#security-best-practices)
- [Common Development Tasks](#common-development-tasks)
- [Code Review Checklist](#code-review-checklist)
- [Constraints](#constraints)

## Quick-start checklist

- [ ] Read this file and `README.md` before acting to:
  - Familiarize yourself with the library's core concepts and use cases
  - Explore the available maskers and their use cases
  - Understand the security considerations and guidelines for consumers

## Feature Checklist

- [ ] Simple, clean, concise code
- [ ] Follows the [java guidelines](java-guidelines.md)
- [ ] Follows the [architecture guidelines](architecture-guidelines.md)
- [ ] Error handling + logging
- [ ] Tests added and passing
- [ ] Documentation updated (`README.md`) 
- [ ] Code formatted (`mvn spotless:apply`)
- [ ] No warnings
- [ ] `CHANGELOG.md` updated for user-facing changes

**If something is unclear, ask before making assumptions.**

## Code Style & Quality

### Formatting Standards

The project uses **Spotless** with **Palantir Java Format** for code formatting:

- **Indentation**: 4 spaces
- **Line Endings**: Unix-style (LF)
- **Trailing Whitespace**: Removed
- **File Ending**: Must end with newline
- **Import Organization**: Automatic via Spotless

### Format Code Automatically

```bash  
# Format all Java and POM files  
mvn spotless:apply  
  
# Check formatting without applying  
mvn spotless:check  
```  

### Null Safety & Error Detection

The project uses **ErrorProne** with **NullAway** for compile-time static analysis:

- All production code is considered non-nullable by default. Use `@Nullable` to mark nullable types explicitly (via JSpecify).
- Null checks are enforced at compile time
- NullAway errors are treated as compilation failures
- Test code is excluded from null-checking requirements

### Code Quality Tools

- **SonarCloud**: Continuous quality monitoring
    - See: [SonarCloud Dashboard](https://sonarcloud.io/summary/new_code?id=ehayik_jmaskify)
- **JaCoCo**: Code coverage reporting
- **Error Prone**: Static bug detection

### Annotations

Use JSpecify annotations for null-safety:

```java  
import org.jspecify.annotations.Nullable;  
 
// Nullable parameter and return type  
public @Nullable String process(@Nullable String data) {  
    return null;
}  
```  

## Testing Guidelines

### Testing Framework

- **Framework**: JUnit 5 (Jupiter)
- **Assertions**: AssertJ for fluent assertions
- **Mocking**: Mockito for test doubles
- **Data Generation**: DataFaker for realistic test data
- **JSON Testing**: JSONAssert for JSON comparisons

### Test Organization

Tests should be organized as follows:

- **Location**: `src/test/java/io/github/ehayik/jmaskify/`
- **Naming**: `*Tests.java` (e.g., `CreditCardMaskerTests.java`)
- **Structure**: One test class per masker type
- **Scope**: Cover happy paths, edge cases, and error scenarios

### Writing Effective Tests

1. **Use Descriptive Names**: Test method names should clearly describe what is being tested
   
```java  
   void shouldMaskCreditCardPreservingLastFourDigits() {}
   void shouldThrowExceptionForInvalidJson() {}  
   void shouldHandleNullInputGracefully() {} 
 ```  

2. **Follow GWT Pattern**: Given, When, Then

```java
@Test  
   void shouldMaskEmail() {  
       // Given
       var email = "john.doe@example.com";       
       var masker = Masker.fixedLength();  
       
       // When
       var result = masker.apply(email);  
       
       // Then  
       assertThat(result).isEqualTo("*".repeat(email.length()));  
   }
```

3. **Test Coverage Requirements**
  - Aim for >80% code coverage
  - Cover boundary conditions and edge cases
  - Test error handling and exceptions

4. **Use Parameterized Tests**: For testing multiple scenarios
   
```java  
   @ParameterizedTest  
   @ValueSource(strings = { "test1", "test2", "test3" })  
   void shouldMaskVariousInputs(String input) {  
       // Test implementation  
   }  
```  

## Git Workflow

### Branch Naming Convention

- **Features**: `feat/description-of-feature`
- **Bug Fixes**: `fix/description-of-bug`
- **Refactorings**: `ref/fixed-length-masker`
- **Documentation**: `docs/description-of-docs`

### Commit Message Format

Follow the conventional commit format:

```  
<type>(<scope>): <subject>  
  
<body>  
  
<footer>  
```  

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example**:

```  
feat(json-masker): add support for nested array masking  
  
Implement recursive masking for nested arrays in JSON structures.  
This allows users to mask sensitive data at multiple levels of nesting.  
  
Fixes #42  
```  

#### Handling breaking changes

- Appends a `!` after the type/scope
- Or use `BREAKING CHANGE: <description>` footer
- If included in the type/scope prefix, breaking changes MUST be indicated by a `!` immediately before the `:`. 
- If `!` is used, BREAKING CHANGE: MAY be omitted from the footer section, and the commit description SHALL be used to describe the breaking change.

**Examples**:

- Commit message with `!` to draw attention to breaking change

```
feat!: send an email to the customer when a product is shipped
```

- Commit message with scope and `!` to draw attention to breaking change

```
feat(api)!: send an email to the customer when a product is shipped
```

- Commit message with description and breaking change footer

```
feat: allow provided config object to extend other configs

BREAKING CHANGE: `extends` key in config file is now used for extending other config files
```

### Pull Request Process

1. **Read [Pull Request Description Guidelines](pull-request-description-guidelines.md) before creating a PR**, to
   ensure the PR title and description adhere to the project's guidelines

2. **Create a branch from the `master` branch**

```bash  
git checkout -b feat/my-feature 
```

3. **Make Changes & Commit**

Follow the [Commit Message Format](#commit-message-format) for all commits.

```bash  
git add .   
git commit -m "feat: add new masking strategy" 
```  

4. **Push to Repository**

```bash  
git push origin feat/my-feature  
```  

5. **Open a Pull Request**

Create a Pull Request to the `master` branch on GitHub. It is recommended to use a temporary file for the PR body to handle multi-line content correctly.

```bash
# Create a temporary file with the PR body
cat <<EOF > target/pr_body.md
### What
<Description of the changes>

### Why
<Justification for the changes>

### How
<Implementation details>

### Acceptance Criteria
- [ ] Task 1
- [ ] Task 2
EOF

# Create the pull request
gh pr create --base master --head feat/my-feature --title "feat: add new masking strategy" --body-file target/pr_body.md

# Clean up
rm target/pr_body.md
```

Alternatively, use the `--fill` flag to automatically use the commit message:

```bash
gh pr create --base master --head feat/my-feature --fill
```

6. **Address Review Comments**
    - Keep the conversation respectful and collaborative
    - Respond to all feedback before merging
    - Update the PR with new commits

7. **Merge Strategy**
    - Use "Squash and merge" for small feature branches
    - Use "Create a merge commit" for larger changes
    - Ensure the CI/CD pipeline passes

## Documentation

### Code Documentation

1. **Javadoc Comments**
    - Document all public classes and methods
    - Include `@param`, `@return`, and `@throws` tags
    - Provide usage examples in Javadoc where helpful
    - Use `@Deprecated(forRemoval = true, since = "<version")` annotation for deprecated public classes, constructors, and methods
    - Write clear and concise descriptions

2. **Implementation Notes**
    - Use `@since` tags to document API changes
    - Use `@deprecated` tags for deprecated APIs when appropriate
    - Use `@inheritDoc` to inherit Javadoc from superclasses and interfaces when appropriate
    - Use `@apiNote` tags to document API usage and behavior where helpful
    - Use `@implNote` tags to document implementation details where helpful
    - Use `@implSpec` tags for specification (default implementation) details where helpful
    - Add inline comments for complex logic only
    - Avoid redundant or excessive implementation notes and inline comments

3. **Example Javadoc**

```java
/**
 * Picks the winners from the specified set of players.
 * <p>
 * The returned list defines the order of the winners, where the first
 * prize goes to the player at position 0. The list will not be null but
 * can be empty.
 *
 * @deprecated Use {@link #pickWinners(Stream)} instead.
 * @apiNote This method was added after the interface was released in
 *          version 1.0. It is defined as a default method for compatibility
 *          reasons.
 * @implSpec The default implementation will consider each player a winner
 *           and return them in an unspecified order.
 * @implNote This implementation has a linear runtime and does not filter out
 *           null players.
 * @throws NullPointerException if the specified set of players is null          
 * @param players
 *            the players from which the winners will be selected
 * @return the (ordered) list of the players who won; the list will not
 *         contain duplicates
 * @since 1.1
 */
@Deprecated(forRemoval = true, since = "1.2")
default List<String> pickWinners(Set<String> players) {
	return new ArrayList<>(players);
}
```

### README Updates

Update `README.md` when:
- Adding new masking strategies
- Changing public API signatures
- Adding new features or examples
- Fixing documentation errors

### CHANGELOG

Maintain a `CHANGELOG.md` documenting:
- New features (under "Added")
- Bug fixes (under "Fixed")
- Breaking changes (under "Changed")
- Deprecations (under "Deprecated")

## Security Best Practices

- **Input Validation**: Always validate and sanitize user input
- **Null Safety**: Consider all types non-nullable by default. Use `@Nullable` to mark nullable types explicitly.
- **Error Handling**: Provide meaningful error messages without exposing sensitive data
- **Dependencies**: Keep dependencies up to date and monitor for vulnerabilities

## Common Development Tasks

### Adding a New Masking Strategy

1. Create a branch: `git checkout -b feat/masker-name`
2. Create a new class implementing the `Masker<String>` interface (or another relevant input type)
3. Update the `permits` list in the `Masker` sealed interface
4. Implement the `apply(@Nullable String input)` method
5. Provide a static factory method in the `Masker` interface for easy access
6. Add unit tests in `src/test/java`
7. Add/Update Javadoc accordingly
8. Update documentation in `README.md`
9. Update `CHANGELOG.md`
10. Create a Pull Request with the new feature

### Fixing a Bug

1. Create a branch: `git checkout -b fix/bug-description`
2. Add a failing test that reproduces the bug
3. Implement the fix
4. Ensure all tests pass
5. Update `CHANGELOG.md`
6. Create a Pull Request with the fix

## Code Review Checklist

When reviewing code, verify:

- ✅ Tests are comprehensive and meaningful
- ✅ Code follows project style guidelines
- ✅ Null-safety annotations are correctly applied
- ✅ Error handling is appropriate
- ✅ Documentation is accurate and complete
- ✅ No security vulnerabilities
- ✅ Performance is acceptable
- ✅ Changes are backward-compatible (unless breaking change)

## Constraints

### MUST NOT DO

- Bump major versions of core dependencies without a dedicated PR and discussion
- Publish new versions to Maven Central
- Merge a pull request without proper review and approval