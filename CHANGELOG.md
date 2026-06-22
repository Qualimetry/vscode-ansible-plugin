# Changelog

## [Unreleased]

- (None.)

## [3.2.14] - 2026-06-23

- Version-alignment release.

## [3.2.13] - 2026-06-17

- In-editor diagnostics now link directly to the documentation for each rule.
- Added full-text, per-rule documentation covering what each rule checks, why it matters, and how to fix it, with compliant and non-compliant examples.

## [3.2.10] - 2026-06-11

- Added: Rules now re-sync automatically from the last-used SonarQube server on startup (`ansibleAnalyzer.sonar.autoSyncOnStartup`).
- Improved: SonarQube tokens are stored securely in VS Code secret storage.

## [3.2.6] - 2026-02-28

- Fixed: Import rules from SonarQube now correctly writes rules to settings.json.
- Improved: Import confirmation message shows the exact file path where rules were saved.

## [3.2.5] - 2026-02-22

First public release.

- Real-time diagnostics for `.yml` / `.yaml` files as you type.
- **Ansible: Import rules from SonarQube** – pull quality profile and rule severities from a SonarQube server into settings.
- Configurable per-rule severity and enable/disable via `ansibleAnalyzer.rules`.
- Requires Java 17+ (uses `JAVA_HOME` or `ansibleAnalyzer.java.home`).
