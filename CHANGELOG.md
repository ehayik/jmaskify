# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - TBD

### Fixed
- Fix split behavior for dashed credit card numbers

### Changed
- Introduce a new `MaskerBuilder` interface to standardize masker builder functionality
- Migrate to JSpecify annotations for Null Safety

### Deprecated
- Deprecate `FixedLengthMasker` default constructor in favor of `FixedLengthMasker#builder()` or `Masker#fixedLength()`

### Added
- Add custom substitution character configuration in fixed-length masker
- Add support for prefix and suffix preservation in `FixedLengthMasker`
- Add support for ignoring specific characters during masking in `FixedLengthMasker`
- Add support to configure masking strategies in multiline text masker

## [1.0.1] - 2025-04-18

### Changed
- Refactor masker builders to streamline method chaining

### Added
- Add support for masking array values in JSON

## [1.0.0] - 2025-04-12

### Added
- Versatile masking strategies for sensitive data protection
- Fixed-length anonymization for consistent data obfuscation
- Credit card number masking with customizable patterns
- Base64 encoding for binary data security
- JSON field masking with property-specific strategies
- Multiline text pattern masking for complex documents
- Support for custom masking strategy implementations
- Fluent API design for intuitive configuration
- Compatible with Java 17+ applications
- Integration with Jackson for seamless JSON processing
- Performance-optimized implementation for production use
- Comprehensive documentation with usage examples
