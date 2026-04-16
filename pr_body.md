### What
Update `commons-lang3` to version 3.20.0 to address CVE-2025-48924.

### Why
To mitigate a security vulnerability detected in the existing version of `commons-lang3`.

### How
- Update `commons-lang3.version` property in `pom.xml`.
- Update `CHANGELOG.md` with the security fix.

### Acceptance Criteria
- [x] Dependency version updated.
- [x] CHANGELOG reflects the change.
- [x] All unit tests pass.
