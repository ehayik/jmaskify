# Pull Request Description Guidelines

These are the general guidelines for writing clear and effective pull request descriptions.

## Table of Contents

- [1. What (Headline)](#1-what-headline)
- [2. Why (Justification)](#2-why-justification)
- [3. How (Implementation Approach)](#3-how-implementation-approach)
- [4. Acceptance Criteria](#4-acceptance-criteria)
- [5. Best Practices](#5-best-practices)

## 1. What (Headline)

The "What" section serves as the **Pull Request title**.

- **Be specific and concise**: Clearly state what needs to be done in a single sentence. Limit the title to around 50 characters if possible.
- **Use imperative mood**: Write the title as if you are giving a command (e.g., "Fix issue" instead of "Fixed issue").
- **Use action verbs**: Start with keywords like "Add," "Fix," "Update," "Remove," etc., to indicate the type of change.
- **Name the affected component/module**: Identify where in the system the change occurs.
- **Avoid redundancy**: Don't repeat information already clear from context. Specify the bug or feature clearly.

## 2. Why (Justification)

- **Explain the business/technical motivation**: Why is this task necessary?
- **Describe the problem being solved**: What issue or requirement does this address?
- **Mention expected benefits**: How will this improve the system or user experience?

## 3. How (Implementation Approach)

- **Outline the implementation strategy**: Break down the solution into logical steps.
- **List key technical changes**: Identify major components that need modification.
- **Set boundaries**: Define the scope of work and what is not included if relevant.

## 4. Acceptance Criteria

- **Define clear, testable outcomes**: What must be true for the task to be considered finished?
- **Include verification steps**: Mention specific tests, manual checks, or edge cases that must be validated.
- **Use checkboxes**: To allow for easy tracking of progress and completion.

## 5. Best Practices

- **Be concise but complete**: Include all necessary information while avoiding unnecessary details.
- **Use bullet points**: For clarity in the "How" section.
- **Include Acceptance Criteria**: Ensure every task has measurable success conditions.
- **Include context**: That helps readers understand the task's importance.
- **Consider dependencies**: Mention related tasks or prerequisites.

## Example

```
### What

Enhance JsonMasker to support masking string values in JSON arrays and nested arrays

### Why

To provide comprehensive masking capabilities for JSON arrays, including mixed-type and nested arrays.
This enhancement ensures that sensitive string data within arrays is properly masked while preserving non-string values,
improving data protection across more complex JSON structures.

### How

- Add functionality to detect and mask string values within JSON arrays, including nested arrays
- Preserve non-string values (numbers, booleans, null) in arrays during masking operations
- Implement logging for fields that are ignored due to missing or unsupported masking strategies
- Expand README documentation with examples demonstrating array and nested array masking capabilities

### Acceptance Criteria

- [ ] String values in flat arrays are correctly masked
- [ ] String values in nested arrays are correctly masked
- [ ] Mixed-type arrays preserve non-string values while masking strings
- [ ] Fields with missing or unsupported masking strategies are logged appropriately
- [ ] README includes clear examples of array and nested array masking use cases
- [ ] All existing tests continue to pass
- [ ] New unit tests cover array masking scenarios (flat, nested, and mixed-type arrays)
```