---
description: "Intelligently update existing development journals with drift detection and change preview"
---

# Intelligent Development Journal Updater

Think harder about updating existing development journals to detect implementation changes and apply intelligent updates while preserving manual edits.

Execute each task in the order given to update an existing development journal with sophisticated change management.

## Task 1: Detect Existing Development Journal 🔍

Run git commands to analyze the current context:

```bash
git rev-parse --abbrev-ref HEAD
git log -1 --format="%H %cr"
```

Search for existing development journals using intelligent detection:

```bash
# Check standard location
find .agents/artifacts/journal -name "DEV-JOURNAL-*.md" -mtime -14 | sort -r

# Extract feature name from current branch for matching
BRANCH=$(git rev-parse --abbrev-ref HEAD)
FEATURE=$(echo "$BRANCH" | sed -E "s#^[a-z]+/##" | tr '-' '_' | tr '[:lower:]' '[:upper:]')

# Search for journals matching current feature
find .agents/artifacts/journal -name "*${FEATURE}*.md" -type f

# Check git history for journal edits
git log --name-only --pretty=format: | grep "DEV-JOURNAL.*\.md" | sort | uniq
```

Apply confidence scoring algorithm:
- Branch/feature pattern match: +40 points
- Recent modification: +30 points
- Contains files currently being edited: +20 points
- Standard location: +10 points

Present the best match with confidence score:

"🎯 **Development Journal Analysis:**

Best match ({confidence}% confidence):
📄 {file-path}
   ✓ Matches current branch/feature pattern
   ✓ Modified {time-ago}
   ✓ Contains files from recent changes
   ✓ Recent TODO references in commits

**Your choice:**
1. Use recommended journal (recommended)
2. Select from other candidates
3. Enter custom path
4. Create new journal instead

Please choose (1-4):"

If choice 1 selected, proceed. If choice 2, show selection menu with all candidates and confidence scores.

## Task 2: Analyze Changes Since Last Update 🎯

Examine the selected journal document and analyze what has changed:

```bash
# Get document last modified time
git log -1 --format="%H %cr" {selected-journal-file}

# Analyze changes since journal creation
git diff --stat {last-journal-commit}..HEAD
git log {last-journal-commit}..HEAD --oneline

# Search for TODO completion evidence
grep -r "TODO\|FIXME" src/ 2>/dev/null | wc -l
find src -name "*Test*" -newer {selected-journal-file} 2>/dev/null | wc -l
```

Assess the scope of changes:
- Count new, modified, and deleted files
- Calculate lines added/removed
- Identify TODO completion evidence
- Detect new test files and components
- Estimate implementation drift

Present change summary:

"📊 **Changes Since Last Update:**
- Journal last updated: {time-ago}
- New commits: {count}
- Files changed: +{new} ~{modified} -{deleted}
- Lines changed: +{additions} -{deletions}
- TODOs in journal: {journal-todo-count}
- Current TODOs in code: {current-todo-count}
- New test files: {new-test-count}
- Scope assessment: {simple/moderate/complex/major-drift}"

If major drift detected (>50% change or substantial architecture shifts), warn:

"⚠️ **Significant Implementation Drift Detected!**

This journal appears substantially out of sync with current implementation:
• Implementation drift: {percentage}%
• Major architectural changes detected
• Multiple TODO completions likely

**Options:**
1. Continue intelligent update (preserve history)
2. Create fresh journal version
3. Review specific changes first

Recommendation: {recommendation-based-on-drift}
Proceed with update? (y/n)"

## Task 3: Interactive Test Execution Decision 🧪

Present the test execution choice:

"🧪 **Test Execution Choice:**

Would you like me to run tests to verify TODO completions and validate current state?

**Benefits of running tests:**
- Confirm completed TODOs actually work
- Verify test coverage improvements claimed in journal
- Validate performance metrics and benchmarks
- Detect new test scenarios and components

**Consider skipping if:**
- Tests already run recently in CI/CD
- No test-related TODOs in journal
- Quick update needed
- Tests take very long to run

**Your choice:**
1. ✅ Run tests and include verification results
2. ⏭️ Skip tests (document changes without verification)

Please choose (1 or 2):"

**If choice 1 (Run tests):**
Execute the test suite and capture results:

```bash
# Detect project type and run appropriate tests
if [ -f "package.json" ]; then
    npm test -- --coverage 2>&1
elif [ -f "pom.xml" ]; then
    mvn test 2>&1
elif [ -f "pytest.ini" ] || [ -f "setup.py" ]; then
    pytest --cov 2>&1
fi
```

Capture and analyze:
- Test execution output and statistics
- Coverage percentages and changes
- Performance metrics if available
- New test files and scenarios

**If choice 2 (Skip tests):**
Note that changes will be documented without test verification.

## Task 4: Generate Change Preview 📋

**CRITICAL: Show detailed preview before making ANY changes to the journal document.**

Implement intelligent reconciliation with confidence scoring:

"📊 **Change Analysis Report**
═══════════════════════════════════════

🔄 **DETECTED CHANGES:**

{For each change with confidence >= 70%}
**{Change-Category}** (Confidence: {confidence}%)
   Journal State: {original-state}
   Current State: {current-state}
   Evidence:
   • {specific-evidence-1}
   • {specific-evidence-2}
   • {specific-evidence-3}

📈 **COMPLETED TODOs** (High Confidence):
{List TODOs with completion evidence}

🆕 **NEW COMPONENTS:**
{List new files and components}

⚠️ **ARCHITECTURAL CHANGES:**
{List architectural shifts with evidence}

**Overall Change Magnitude:** {percentage}% ({severity-level})"

Create comprehensive diff-style preview:

"📝 **Journal Update Preview**
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

{Generate line-by-line diff preview showing:}
- Lines to be added with + prefix
- Lines to be removed with - prefix  
- TODO completions with strikethrough + checkmarks
- New sections to be added
- Metadata updates (timestamps, metrics)
- Architectural decision updates

📊 **Preview Summary:**
• Lines to add: {add-count}
• Lines to remove: {remove-count}
• Lines to modify: {modify-count}
• TODOs completed: {completed-todo-count}
• Sections to update: {updated-sections}
• Change magnitude: {percentage}%

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

👀 **Review these changes carefully!**

**Your choice:**
1. [A]ccept all changes
2. [R]eview section by section
3. [M]odify specific sections
4. [C]ancel update

Please choose (A/R/M/C):"

If option R (Review section by section):
Present each section change for individual approval.

If option M (Modify specific sections):
Show section-by-section modification options.

## Task 5: Apply Approved Changes 📝

After user approval, apply changes using intelligent merging:

```bash
# Create change log directory structure
mkdir -p .agents/artifacts/change-logs/$(date +%Y%m%d)

# Generate change log filename
echo "CHANGELOG-{feature-name}-UPDATE-$(date +%Y%m%d-%H%M%S).md"
```

Apply smart merging strategy:
- Update sections marked with `<!-- AUTOMATED:START -->` and `<!-- AUTOMATED:END -->`
- Preserve all content outside automated sections
- Add visual change indicators:
  - ✨ for NEW items
  - 🔄 for UPDATED items
  - ✅ for COMPLETED TODOs (strikethrough + checkmark)
  - 🗑️ for REMOVED items
- Maintain user comments and custom notes

Apply changes in this order:
1. Update Executive Summary with current metrics
2. Mark completed TODOs with completion evidence
3. Add new components to relevant sections
4. Update Technical Decisions with architectural changes
5. Update Testing section with new test results
6. Update journal metadata (timestamps, metrics)

## Task 6: Update History and Validation ✅

Generate comprehensive change log:

```markdown
# Development Journal Change Log

**Journal**: {journal-filename}
**Journal Date**: {original-date}
**Update Date**: {current-timestamp}
**Update Number**: {sequence-number}
**Confidence Level**: {overall-confidence}% ({confidence-level})

## 📊 Summary of Changes

### ✅ Completed Items ({completed-count})
| TODO | Evidence | Verification |
|------|----------|--------------|
{completed-todos-table}

### 🔄 Updated Sections ({updated-count})
{section-updates-list}

### 📝 Architectural Evolution
{architectural-changes-documentation}

### 🎯 Metrics Evolution
| Metric | Previous | Current | Change |
|--------|----------|---------|---------|
{metrics-evolution-table}

### 🔍 AI-Friendly Context
{context-for-future-sessions}
```

Add entry to journal update history:

```markdown
## 📝 Journal Update History

<!-- AUTOMATED:START -->
| Date | Time | Action | Key Changes | Confidence | Test Status |
|------|------|--------|-------------|------------|-------------|
{existing-entries}
| {current-date} | {current-time} | Updated | {change-summary} | {confidence}% | Tests: {test-status} {status-emoji} |
<!-- AUTOMATED:END -->
```

Perform quality validation:
- Verify all placeholder text replaced with real data
- Confirm proper change indicators applied
- Check that user content was preserved
- Validate markdown syntax
- Ensure evidence links are correct
- Verify test data is current (if tests run)

## Task 7: Present Results and Confirmation 👀

Present the completion summary:

"✅ **Development journal updated successfully!**

📁 **File Details:**
- Updated: {journal-path}
- Change log: .agents/artifacts/change-logs/{date}/CHANGELOG-{feature-name}-UPDATE-{timestamp}.md
- Update confidence: {confidence}% ({confidence-level})

📊 **Summary of Changes Applied:**
• Completed TODOs: {completed-count} (verified with evidence)
• Updated sections: {updated-count}
• New components documented: {new-component-count}
• Architectural changes: {architectural-count}
• Preserved all custom notes and edits

🔄 **Change Indicators Added:**
• ✨ NEW items marked
• 🔄 UPDATED items marked
• ✅ COMPLETED TODOs marked (with strikethrough)
• 🗑️ REMOVED items marked

📝 **Update History:**
Entry added to Journal Update History table for transparency

💡 **Next steps:**
1. Review the highlighted changes in the journal
2. Validate change log for architectural decisions
3. Share updated journal with team if collaborative work
4. Use as context for future development sessions
5. Consider running drift check on other journals

🔍 **Updated journal available at:** {journal-path}
📚 **Change history available at:** .agents/artifacts/change-logs/{date}/

**Integration with workflow:**
- This journal now reflects current implementation state
- Use change log to understand evolution decisions
- Reference in PR documentation when ready for review
- Include in team knowledge sharing sessions"

Confirm the intelligent journal update workflow completed successfully.