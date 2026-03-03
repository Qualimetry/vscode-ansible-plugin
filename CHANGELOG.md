# Changelog

## [Unreleased]

- (None.)

## [3.2.6] - 2026-02-28

- Fixed: Import rules from SonarQube now correctly writes rules to settings.json.
- Improved: Import confirmation message shows the exact file path where rules were saved.

## [3.2.5] - 2026-02-22

First public release.

- Real-time diagnostics for `.yml` / `.yaml` files as you type.
- **Ansible: Import rules from SonarQube** – pull quality profile and rule severities from a SonarQube server into settings.
- Configurable per-rule severity and enable/disable via `ansibleAnalyzer.rules`.
- Requires Java 17+ (uses `JAVA_HOME` or `ansibleAnalyzer.java.home`).
