# Changelog

## [Unreleased]

- (None.)

## [3.2.5] - 2026-02-22

First public release.

- Real-time diagnostics for `.yml` / `.yaml` files as you type.
- **Ansible: Import rules from SonarQube** – pull quality profile and rule severities from a SonarQube server into settings.
- Configurable per-rule severity and enable/disable via `ansibleAnalyzer.rules`.
- Requires Java 17+ (uses `JAVA_HOME` or `ansibleAnalyzer.java.home`).
