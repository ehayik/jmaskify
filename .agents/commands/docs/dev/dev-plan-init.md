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

Perform comprehensive technical analysis for the confirmed feature:

**Codebase Architecture Discovery:**
```bash
# Analyze project structure and technology stack
find . -name "pom.xml" -o -name "package.json" -o -name "build.gradle" -o -name "requirements.txt" | head -5
grep -r "spring\|react\|express\|django" --include="*.xml" --include="*.json" . | head -5

# Identify existing patterns and conventions
find . -name "*Service.java" -o -name "*Controller.java" -o -name "*Repository.java" | head -10
grep -r "class.*Controller\|class.*Service\|class.*Repository" src/ | head -10

# Database and configuration analysis
find . -name "*.sql" -o -name "schema.sql" -o -name "*migration*" | head -5
grep -r "datasource\|database\|mongodb" --include="*.yml" --include="*.properties" . | head -5
```

**Present Architecture Analysis:**

"🏗️ **Technical Architecture Analysis**

**Technology Stack Detected:**
• Framework: {detected-framework}
• Database: {detected-database}
• Build System: {detected-build-system}
• Testing Framework: {detected-testing}

**Existing Patterns:**
• Service Layer: {service-pattern-count} services found
• Controller Layer: {controller-pattern-count} controllers found
• Data Layer: {repository-pattern-count} repositories found
• Configuration: {config-pattern-description}

**Integration Architecture:**
• External Services: {external-integrations}
• Security Framework: {security-framework}
• Monitoring/Logging: {monitoring-setup}

**Implementation Complexity Assessment:**
Based on the architecture analysis:

**1. Simple Implementation** (1-3 days)
• Basic CRUD operations
• Minimal external integrations
• Standard patterns only

**2. Moderate Implementation** (1-2 weeks)
• Complex business logic
• Multiple service integrations
• Security considerations
• Testing requirements

**3. Complex Implementation** (2-4 weeks)
• Advanced architecture patterns
• Multiple external systems
• Performance optimization
• Comprehensive testing strategy

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
**Estimated Duration**: {duration-estimate}

## Executive Summary

{2-3 paragraph technical and business overview}

## Technical Architecture

{architecture-overview-with-diagrams}

## Implementation Plan

### Phase 1: Foundation Setup
- [ ] **Task 1.1**: {foundation-task-1}
- [ ] **Task 1.2**: {foundation-task-2}
- [ ] **Task 1.n**: {foundation-task-n}

### Phase 2: Core Implementation  
- [ ] **Task 2.1**: {core-task-1}
- [ ] **Task 2.2**: {core-task-2}
- [ ] **Task 2.n**: {core-task-n}

### Phase 3: Integration & Testing
- [ ] **Task 3.1**: {integration-task-1}
- [ ] **Task 3.2**: {integration-task-2}
- [ ] **Task 3.n**: {integration-task-n}

### Phase 4: Quality & Deployment
- [ ] **Task 4.1**: {quality-task-1}
- [ ] **Task 4.2**: {quality-task-2}
- [ ] **Task 4.n**: {quality-task-n}

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

**Execution Framework Loading:**
Loading embedded execution framework from `embedded/execute-plan.md`

**Framework Configuration:**
• Plan File: {plan-file-path}
• Execution Mode: Human-guided step-by-step
• Quality Gates: Enabled
• Progress Tracking: Active

**Starting execution framework...**"

**Execute Embedded Framework:**
Execute the complete embedded execution framework from `embedded/execute-plan.md`:

**Framework Execution:**
- Framework handles plan detection and loading
- Applies systematic task-by-task execution methodology  
- Provides human review and approval workflow
- All execution logic contained within embedded framework

**Self-Contained Execution:** The embedded framework is fully self-executing and requires no additional coordination from this command.

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

**To Resume Later:**
• Use `/user:dev-plan-resume` command
• Or reference the plan file directly

**Plan Summary:**
• {total-task-count} tasks across {phase-count} phases
• Estimated duration: {duration-estimate}
• Ready for implementation when you are

**Next Steps:**
1. Review the generated plan
2. Use `/user:dev-plan-resume` to start execution
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
1. Use `/user:dev-plan-resume` to continue execution
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
• `/user:dev-plan-resume` - Continue plan execution
• `/user:feature-explain` - Deep dive into specific components
• `/user:pr-create` - Generate PR documentation for completed phases

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