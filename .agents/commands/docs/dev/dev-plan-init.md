---
description: "Generate comprehensive development plans with intelligent feature analysis and human-guided execution"
---

# Development Plan Generator & Executor

Think harder about creating comprehensive development plans for complex features and implementations, with structured execution tracking and human-guided progress management.

Execute each task in the order given to create a trackable development plan and initiate execution.

## Task 1: Feature Analysis and Discovery 🔍

Use $ARGUMENTS as the feature or implementation description. If $ARGUMENTS is empty, prompt:

"What feature or implementation would you like to create a development plan for?

**Examples:**
• 'Email address masker preserving the domain'
• 'XML masker for element and attribute values'
• 'IBAN masker showing only the country code and last digits'
• 'Support nested array masking in JsonMasker'
• 'Configurable mask character for CreditCardMasker'

**Provide feature description:**"

**Context Analysis Process:**
```bash
# List existing maskers and the sealed interface's permits list
ls src/main/java/io/github/ehayik/jmaskify/
grep -n "permits\|static .*Masker" src/main/java/io/github/ehayik/jmaskify/Masker.java

# Search for components related to the feature (replace {keyword} with feature terms)
grep -rli "{keyword}" src/ README.md CHANGELOG.md | head -10

# Review dependencies and build plugins that may be affected
grep -n "<artifactId>" pom.xml
```

**Feature Context Discovery:**

"🔍 **Feature Analysis: {Feature-Name}**

**Context Discovery Results:**
• Related Components: {discovered-components}
• Existing Patterns: {existing-patterns}
• Configuration Found: {config-files}
• Integration Points: {integration-points}

**Feature Scope Confirmation:**
Based on the context analysis, I've identified this as a {complexity-level} implementation.

**Is this the feature you want to plan?**
1. Yes, proceed with comprehensive planning
2. Refine feature scope (provide more specific details)
3. Suggest features from recent context
4. Start with different feature

Your choice (1-4):"

**For Choice 3 (Suggest from Context):**
Analyze conversation history and present recent feature discussions:

"💡 **Recent Feature Discussions Detected:**

**Most Recent (Priority Order):**
1. {most-recent-feature-discussion}
2. {second-recent-feature-discussion}
3. {third-recent-feature-discussion}

**Which recent discussion should I plan?**
1. {option-1}
2. {option-2}
3. {option-3}
4. None, specify new feature

Your choice (1-4):"

## Task 2: Technical Architecture Analysis 🏗️

Analyze how the confirmed feature fits JMaskify's architecture: the `Masker<T>` sealed interface, its implementations, their builders, and the static factory methods (see `.agents/project-overview.md` and `.agents/guidelines/architecture-guidelines.md`).

**Codebase Architecture Discovery:**
```bash
SRC=src/main/java/io/github/ehayik/jmaskify
TEST=src/test/java/io/github/ehayik/jmaskify

# Masker hierarchy: permitted implementations and public factory methods
grep -n "permits" $SRC/Masker.java
grep -n "^    static " $SRC/Masker.java

# Configuration patterns: which maskers use builders (MaskerBuilder / Buildable / Lombok)
grep -ln "Builder\|Buildable\|@Builder" $SRC/*.java

# Security-sensitive constructs the feature may touch: regexes and logging
grep -n "Pattern.compile\|\.matcher(\|replaceAll(" $SRC/*.java
grep -n "log\.\|@Slf4j" $SRC/*.java

# Error handling conventions
grep -n "MaskingException\|throw new" $SRC/*.java | head -10

# Existing tests to mirror
ls $TEST/

# Java release and runtime dependencies (new dependencies need justification)
grep -n "<maven.compiler.source>\|<artifactId>" pom.xml
```

Then read the **closest existing masker** and its test class in full (for example, `CreditCardMasker` for a partial-visibility masker, `MultilinePatternMasker` for a regex-based one, `JsonMasker` for structured input). The new code should mirror its shape unless there is a reason not to.

**Present Architecture Analysis:**

"🏗️ **Technical Architecture Analysis**

**Masker Hierarchy:**
• Permitted implementations: {permits-list}
• Closest existing masker: {analogous-masker} ({why-it-is-analogous})
• Factory methods affected: {new-or-changed-factory-methods}

**Design Fit:**
• Change type: {new masker | option on existing masker | interface/composition change}
• Configuration: {static factory only | builder (MaskerBuilder/Buildable)}
• Input type: {Masker<String> | Masker<other>}
• Immutability and thread safety: {how the design keeps the masker immutable}
• Null handling: {behavior for null input; @Nullable usage for NullAway}

**Security Surface:**
• Regular expressions: {new-or-changed regexes and ReDoS risk | none}
• Reversibility: {irreversible | reversible, must be documented like Base64Masker}
• Information leaks: {preserved length/structure; exception messages and logs must not contain input}

**Public API Impact:**
• Additions: {new types and methods}
• Breaking changes: {none | list, needs `!` commit and CHANGELOG "Changed"}
• Sealed hierarchy note: adding a type to `permits` can break consumers' exhaustive `switch` over `Masker` (Java 21+ pattern matching). Call it out in `CHANGELOG.md` if a new implementation is added.

**Dependencies & Build:**
• New dependencies: {none | list with justification}
• Build/CI changes: {none | pom.xml or workflow changes, routes to `devops`}

**Implementation Complexity Assessment:**
Based on the architecture analysis:

**1. Small**
• Option or overload on an existing masker (e.g. a configurable mask character)
• No new type in the `permits` list
• Agents: `java-developer`, `test-engineer`, `doc-manager`

**2. Moderate**
• New masker implementation following an existing pattern
• New factory method, possibly a builder
• Agents: + `architect` for the API, `java-code-reviewer`, `security-auditor` if regexes or reversibility are involved

**3. Complex**
• Changes to the `Masker` interface, composition (`andThen`), or structured-input traversal (e.g. JSON)
• New dependency, non-trivial regexes, or a breaking change
• Agents: full flow, including a design-stage `security-auditor` review and `devops` for build changes

**Select implementation complexity level (1-3):**"

## Task 3: Plan Generation Configuration 📋

Configure plan generation parameters based on complexity and requirements:

**Plan Configuration Options:**

"📋 **Development Plan Configuration**

**Based on {selected-complexity} complexity level:**

**Plan Detail Level:**
1. **High-Level Milestones** - Major phases with key deliverables
2. **Detailed Tasks** - Granular implementation steps  
3. **Comprehensive Breakdown** - Full task decomposition with sub-tasks

**Execution Style:**
1. **Guided Implementation** - Step-by-step with human approval
2. **Batch Execution** - Complete phases with checkpoints
3. **Full Automation** - Complete implementation plan only

**Quality Gates:**
1. **Essential Only** - Code review and testing
2. **Standard Quality** - + Documentation and security review
3. **Enterprise Grade** - + Performance testing and deployment

**Team Integration:**
1. **Solo Development** - Single developer workflow
2. **Team Collaboration** - Multi-developer coordination
3. **Enterprise Process** - Full SDLC integration

**Select configuration for each aspect:**
• Detail Level (1-3): 
• Execution Style (1-3):
• Quality Gates (1-3):
• Team Integration (1-3):"

## Task 4: Comprehensive Plan Generation 📝

Generate the complete development plan based on configuration:

**Plan Generation Process:**

"📝 **Generating Comprehensive Development Plan**

**Configuration Applied:**
• Detail Level: {selected-detail}
• Execution Style: {selected-execution}
• Quality Gates: {selected-quality}
• Team Integration: {selected-team}

**Plan Structure Generation:**
• Architecture Planning Phase
• Implementation Phase Breakdown
• Testing and Quality Assurance Phase
• Documentation and Deployment Phase

**Generating plan document...**"

**Create Plan Document:**
```bash
# Create plan file with timestamp
mkdir -p .agents/artifacts/plans
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
PLAN_FILE=".agents/artifacts/plans/DEV-PLAN-{feature-slug}-${TIMESTAMP}.md"
```

**Plan Document Template:**
```markdown
# Development Plan: {Feature-Name}

**Generated**: {timestamp}
**Branch**: {current-branch}
**Complexity**: {selected-complexity}
**Execution Style**: {guided | batch}
**Estimated Duration**: {duration-estimate}

## Executive Summary

{2-3 paragraph technical and business overview}

## Technical Architecture

{architecture-overview-with-diagrams}

## Implementation Plan

Each task names the agent that owns it in backticks (`discovery`, `architect`, `java-developer`, `test-engineer`, `java-code-reviewer`, `security-auditor`, `devops`, `doc-manager`). Tasks marked `(parallel)` may run at the same time as the other `(parallel)` tasks in the same phase. `/docs:dev:dev-plan-execute` uses these markers to route the work.

### Phase 1: Foundation Setup
- [ ] **Task 1.1** `discovery`: Create branch `{feat|fix|ref}/{feature-slug}`
- [ ] **Task 1.2** `{agent}`: {foundation-task-2}
- [ ] **Task 1.n** `{agent}`: {foundation-task-n}

### Phase 2: Core Implementation
- [ ] **Task 2.1** `{agent}` (parallel): {core-task-1}
- [ ] **Task 2.2** `{agent}` (parallel): {core-task-2}
- [ ] **Task 2.n** `{agent}`: {core-task-n}

### Phase 3: Integration & Testing
- [ ] **Task 3.1** `discovery`: Run `./mvnw spotless:apply` and `./mvnw -B verify`
- [ ] **Task 3.n** `{agent}`: {integration-task-n}

### Phase 4: Quality & Documentation
- [ ] **Task 4.1** `java-code-reviewer` (parallel): Review the branch diff
- [ ] **Task 4.2** `doc-manager` (parallel): Update Javadoc, `README.md`, and `CHANGELOG.md`
- [ ] **Task 4.n** `{agent}`: {quality-task-n}

## Progress Log

<!-- Appended by /docs:dev:dev-plan-execute: one line per completed task or blocker -->

## Technical Considerations

{detailed-technical-considerations}

## Risk Assessment

{risk-analysis-and-mitigation}

## Success Criteria

{definition-of-done}
```

## Task 5: Plan Validation and Confirmation ✅

Present the generated plan for human review and confirmation:

**Plan Presentation:**

"✅ **Development Plan Generated Successfully**

**Plan Details:**
• File: `.agents/artifacts/plans/DEV-PLAN-{feature-slug}-{timestamp}.md`
• Total Tasks: {total-task-count}
• Estimated Duration: {duration-estimate}
• Complexity Level: {complexity-level}

**Plan Overview:**
• **Phase 1**: {phase-1-summary} ({phase-1-task-count} tasks)
• **Phase 2**: {phase-2-summary} ({phase-2-task-count} tasks)  
• **Phase 3**: {phase-3-summary} ({phase-3-task-count} tasks)
• **Phase 4**: {phase-4-summary} ({phase-4-task-count} tasks)

**Key Implementation Highlights:**
• {highlight-1}
• {highlight-2}
• {highlight-3}

**Plan Review:**
1. Approve plan and start execution
2. Modify plan details (request changes)
3. Regenerate with different configuration
4. Save plan only (no execution)

Your choice (1-4):"

## Task 6: Execution Initiation 🚀

Based on user choice, either start execution or save plan:

**For Execution Start (Choice 1):**

"🚀 **Initiating Plan Execution**

• Plan File: {plan-file-path}
• Execution Mode: {selected-execution-style}

**Starting execution...**"

**Execute the Plan:**
Follow `.agents/commands/docs/dev/dev-plan-execute.md` with the plan file path as its argument. It routes each task to its owning agent, runs the quality gates, records progress in the plan file, and asks for approval at the configured checkpoints.

**For Plan Modification (Choice 2):**

"🔄 **Plan Modification Request**

**What aspects would you like to modify?**
1. Task breakdown (more/fewer tasks)
2. Implementation approach
3. Quality requirements
4. Timeline estimates
5. Technical architecture
6. All of the above

Your choice (1-6):"

**For Regeneration (Choice 3):**
Return to Task 3 with different configuration options.

**For Save Only (Choice 4):**

"💾 **Plan Saved Successfully**

**Plan Location:** `.agents/artifacts/plans/DEV-PLAN-{feature-slug}-{timestamp}.md`

**To Start or Resume Later:**
• Run `/docs:dev:dev-plan-execute {plan-file-path}` (it continues from the first unchecked task)

**Plan Summary:**
• {total-task-count} tasks across {phase-count} phases
• Estimated duration: {duration-estimate}
• Ready for implementation when you are

**Next Steps:**
1. Review the generated plan
2. Run `/docs:dev:dev-plan-execute {plan-file-path}` to start execution
3. Share plan with team for review"

## Task 7: Session Summary and Next Steps 📊

Provide comprehensive session summary and guidance:

**Session Completion Summary:**

"📊 **Development Planning Session Complete**

**Session Results:**
• Feature Analyzed: {feature-name}
• Plan Generated: ✅ {plan-file-name}
• Complexity Assessment: {complexity-level}
• Total Tasks Created: {task-count}
• Execution Status: {started/saved-for-later}

**Plan Characteristics:**
• Architecture: {architecture-summary}
• Technology Stack: {tech-stack}
• Quality Gates: {quality-level}
• Team Integration: {team-level}

**Generated Artifacts:**
• 📋 Development Plan: `.agents/artifacts/plans/{plan-file}`
• 🏗️ Technical Architecture Overview
• ⏱️ Implementation Timeline
• 🎯 Success Criteria Definition

**How to Use This Plan:**

**For Immediate Development:**
1. Run `/docs:dev:dev-plan-execute {plan-file-path}` to continue execution
2. Follow step-by-step implementation with approval gates
3. Track progress with automated plan updates

**For Team Collaboration:**
1. Share plan file with team members
2. Use plan as basis for sprint planning
3. Adapt timeline based on team capacity

**For Future Reference:**
1. Plan serves as implementation guide
2. Update plan as requirements evolve
3. Use as template for similar features

**Related Commands:**
• `/docs:dev:dev-plan-execute` - Start or resume plan execution
• `/docs:dev:dev-journal-create` - Record progress and decisions in a development journal

**Ready for Implementation!** 🚀"

## Error Recovery and Edge Cases 🔧

**Feature Discovery Issues:**
```
IF NO FEATURE PROVIDED AND NO CONTEXT AVAILABLE:
1. Prompt for feature description with examples
2. Offer to analyze current branch for feature clues
3. Provide feature suggestion templates

IF FEATURE TOO VAGUE OR COMPLEX:
1. Request more specific scope definition
2. Offer to break into multiple plans
3. Provide scope refinement questions
```

**Technical Analysis Failures:**
```
IF PROJECT STRUCTURE CANNOT BE DETERMINED:
1. Request manual technology stack specification
2. Use generic implementation patterns
3. Flag areas requiring manual configuration

IF EXISTING PATTERNS CONFLICT:
1. Present pattern options for user selection
2. Document pattern conflicts in plan
3. Include refactoring tasks if needed
```

**Plan Generation Issues:**
```
IF PLAN BECOMES TOO COMPLEX:
1. Offer to split into multiple phases
2. Provide complexity reduction options
3. Generate high-level plan with detailed sub-plans

IF INSUFFICIENT INFORMATION:
1. Generate plan with research tasks
2. Include discovery tasks in early phases
3. Flag assumptions requiring validation
```