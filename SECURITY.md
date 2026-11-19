# Security Policy

JMaskify protects sensitive data, so we take vulnerabilities in the library seriously. Thank you for helping keep
it and its users safe.

## Supported Versions

Security fixes are made only for the latest release line.

| Version | Supported |
|---------|-----------|
| 1.1.x   | Yes       |
| < 1.1   | No        |

## Reporting a Vulnerability

Report vulnerabilities **privately** through GitHub's private vulnerability reporting:

1. Open the [Report a vulnerability](https://github.com/ehayik/jmaskify/security/advisories/new) form, or go to the
   repository's **Security** tab and click **Report a vulnerability**.
2. Fill in the form and submit the report.

> **WARNING**
> Do not open public issues, comments on existing issues, discussions, or pull requests for vulnerabilities. Public
> reports expose users before a fix is available.

## What to Include

- **Affected version**: the JMaskify version (and Java version) you used.
- **Reproduction**: a minimal example that uses the public API (for example, a `Masker` and the input it fails on).
- **Impact**: what an attacker or a log reader could gain, for example:
  - masked or sensitive input exposed in output, logs, exception messages, or `toString`;
  - more revealed than the masker documents (such as length, prefix, or suffix), or masking silently skipped on
    malformed input;
  - masking that can be reversed or predicted;
  - denial of service from crafted input, such as ReDoS, deeply nested JSON, or huge allocations;
  - injection that changes the structure of the masked output (for example JSON);
  - a known vulnerable dependency that can be exploited through JMaskify.

Use **synthetic data only**. Never include real personal data, card numbers, credentials, or other secrets in a
report.

## What to Expect

- The report is acknowledged and reviewed on a best-effort basis.
- If the issue is confirmed, a fix and its disclosure are coordinated with you through the private advisory.
- Fixes ship as a new patch release. Maven Central releases are immutable, so affected versions cannot be changed;
  upgrade to the patched version.
