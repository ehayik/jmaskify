---
description: "Generate comprehensive development progress journals with intelligent mode selection and preview"
---

# Development Progress Journal Generator

Think harder about analyzing the current development session to create structured documentation that captures progress, decisions, and context for both human developers and AI assistants.

Execute each task in the order given to generate a comprehensive development journal.

## Task 1: Analyze Session Complexity and Recommend Mode 📊

Run git commands to understand the current development context:

```bash
git rev-parse --abbrev-ref HEAD
git diff --stat origin/master 2>/dev/null || git diff --stat HEAD~1
git log --oneline -10
```

Analyze session characteristics:
- Count files modified, added, deleted
- Calculate conversation depth and duration
- Identify architectural decisions made
- Assess change complexity

If $ARGUMENTS contains `--quick`, skip to quick mode. Otherwise, present intelligent recommendations:

"📊 **Development Journal Generator**

**Session Analysis:**
• Current branch: {branch-name}
• Files modified: {count}
• Lines changed: +{additions} -{deletions}
• Recent commits: {commit-count}
• Estimated session time: {duration-estimate}

**Recommended Mode:** {recommendation-with-reasoning}

**Your choice:**
1. **Full Journal** (12 comprehensive sections)
   - Technical decisions documented
   - Code examples and rationale
   - Testing and validation details

2. **Quick Summary** (3 focused sections)
   - Key accomplishments only
   - Important decisions captured
   - Next steps identified

3. **Let me decide** based on change analysis

Please choose (1-3):"

Apply intelligent mode selection criteria:
- **Quick Mode recommended** if: session < 1 hour, < 3 files changed, no architectural decisions
- **Full Mode recommended** if: major features, > 5 files changed, test changes, breaking changes

## Task 2: Extract Development Context 🔍

Parse branch name to extract feature context using patterns:

```bash
# Extract feature name from branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "$BRANCH" | sed -E "s#^[a-z]+/##" | tr '-' '_' | tr '[:lower:]' '[:upper:]'
```

Common patterns:
- `feat/auth-system` → `AUTH_SYSTEM`
- `fix/payment-bug` → `PAYMENT_BUG`
- `peop-3994-role-auth` → `ROLE_AUTH`

Get git user information:

```bash
git config user.name
git config user.email
```

Analyze change scope and extract key information:
- Primary components affected
- New vs modified vs deleted files
- Test file changes
- Configuration modifications

Present context summary:

"🔍 **Development Context Extracted:**
• Feature: {extracted-feature-name}
• Developer: {git-user-name}
• Primary scope: {change-scope-analysis}
• Component areas: {affected-components}"

## Task 3: Configure Content Generation Options 🧪

**For Full Mode only**, present test execution choice:

"🧪 **Test Execution Choice:**

Would you like me to run tests and include results in the journal?

**Benefits of including test results:**
- Documents actual test coverage achieved
- Captures performance metrics and benchmarks
- Shows test evolution and new test scenarios
- Provides concrete validation of changes

**Consider skipping if:**
- Tests already documented elsewhere
- No test-related changes made
- Quick documentation needed
- Tests take very long to run

**Your choice:**
1. ✅ Run tests and include detailed results
2. ⏭️ Skip test execution (document testing approach only)

Please choose (1 or 2):"

**If choice 1 (Run tests):**
Execute appropriate test commands based on project type:

```bash
# Detect project type and run tests
if [ -f "package.json" ]; then
    npm test 2>&1
elif [ -f "pom.xml" ]; then
    mvn test 2>&1
elif [ -f "pytest.ini" ] || [ -f "setup.py" ]; then
    pytest --cov 2>&1
fi
```

Capture test metrics, coverage data, and execution results.

**If choice 2 (Skip tests):**
Note that testing approach will be documented without execution results.

## Task 4: Generate Document Content Based on Mode 📝

Create content using the appropriate template based on mode selection:

**For Full Mode, generate 12-section comprehensive journal:**

```markdown
# Development Progress Journal

**Generated**: {current-timestamp}
**Session Context**: {session-description}
**Developer**: {git-user-name}
**Branch**: {current-branch}
**Mode**: Full Documentation

---

## 📋 Table of Contents
1. [Executive Summary](#executive-summary)
2. [Problem Context](#problem-context)  
3. [Implementation Progress](#implementation-progress)
4. [Technical Decisions](#technical-decisions)
5. [Code Changes](#code-changes)
6. [Testing & Validation](#testing-validation)
7. [Known Issues & TODOs](#known-issues-todos)
8. [Next Steps](#next-steps)
9. [Metrics & Impact](#metrics-impact)
10. [Reflections & Learnings](#reflections-learnings)
11. [Collaboration Notes](#collaboration-notes)
12. [Reference Links](#reference-links)

---

{comprehensive-content-based-on-analysis}
```

**For Quick Mode, generate 3-section focused summary:**

```markdown
# Development Progress - Quick Summary

**Generated**: {current-timestamp}
**Branch**: {current-branch}
**Mode**: Quick Summary

---

## 🎯 What Was Done
{1-2-paragraph-summary}

**Key Changes:**
• ✅ {major-accomplishment-1}
• ✅ {major-accomplishment-2} 
• 🔄 {work-in-progress}

---

## 📝 Important Decisions
{decision-documentation}

---

## 🚀 Next Steps
{next-actions-and-blockers}
```

Generate content based on actual git analysis, conversation history, and detected changes.

## Task 5: Present Document Preview 👀

**CRITICAL: Show detailed preview before saving any files.**

Present comprehensive preview:

"📝 **Development Journal Preview**
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

{Show first 50-75 lines of generated content}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📊 **Document Summary:**
• Mode: {selected-mode}
• Sections: {section-count}
• Estimated size: {file-size}KB
• Code examples: {example-count}
• Decisions documented: {decision-count}
• TODOs tracked: {todo-count}
• Test results: {included/not-included}

**Your choice:**
1. [S]ave this journal
2. [M]odify specific sections
3. [C]ancel generation

Please choose (S/M/C):"

**If choice M (Modify):**
Allow section-by-section review and modification.

**If choice C (Cancel):**
Exit without saving any files.

## Task 6: Create Directory Structure and Save Document ✅

After user approval, create the organized directory structure:

```bash
# Create journal directory structure
mkdir -p .agents/artifacts/journal/$(date +%Y%m%d)

# Generate filename with feature context
FILENAME="DEV-JOURNAL-${FEATURE_NAME}.md"
FILEPATH=".agents/artifacts/journal/$(date +%Y%m%d)/${FILENAME}"
```

Perform quality validation before saving:
- Verify all placeholder text has been replaced with actual data
- Confirm proper template was used based on mode selection
- Check that file was created successfully
- Validate directory structure is correct

Write the generated content to the file with proper error handling.

## Task 7: Present Results and Completion Summary 📋

Present comprehensive completion summary:

"✅ **Development journal created successfully!**

📁 **File Details:**
- Path: .agents/artifacts/journal/{YYYYMMDD}/DEV-JOURNAL-{FEATURE-NAME}.md
- Mode: {selected-mode}
- Size: {actual-file-size}KB

📊 **Document Summary:**
• Sections: {section-count}
• Decisions documented: {decision-count}
• Code examples: {example-count}  
• TODOs tracked: {todo-count}
• Test results: {included-status}

📂 **Directory Structure:**
.agents/artifacts/journal/
└── {YYYYMMDD}/
    └── DEV-JOURNAL-{FEATURE-NAME}.md

💡 **Next steps:**
1. Review the generated journal for accuracy
2. Add any missing context or decisions
3. Share with team if collaborative work
4. Reference in future sessions for context

🔍 **Previous journals available at:** .agents/artifacts/journal/

**Integration with workflow:**
- Use this journal as context for future AI sessions
- Reference in PR documentation when ready for review
- Include links in team communications for transparency"

Confirm the development journal generation workflow completed successfully.