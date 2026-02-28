# Qualimetry Ansible Analyzer

**Author**: The [Qualimetry](https://qualimetry.com) team at SHAZAM Analytics Ltd

Real-time static analysis for Ansible playbooks and task files (`.yml`, `.yaml`), powered by the same engine as the [Qualimetry Ansible Analyzer for SonarQube](https://github.com/Qualimetry/sonarqube-ansible-plugin). Get feedback as you type - YAML syntax, naming, safety, and best practices - so you can fix issues before committing. Rule keys and severities match the SonarQube plugin for a consistent experience from editor to pipeline.

## Requirements

**Java 17+** is required. The extension looks for Java on your system automatically (`JAVA_HOME` or `PATH`). Most users do not need to configure anything.

## Do I need to change settings?

No. The extension works out of the box. Only change settings if:

- The extension cannot find Java (e.g. you have multiple JDKs) - set `ansibleAnalyzer.java.home` to your Java 17+ path.
- You want to enable/disable rules, change severity, or tune rule options (e.g. max line length) - use `ansibleAnalyzer.rules`.

## Configuration (optional)

All settings are under the `ansibleAnalyzer` namespace:

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `ansibleAnalyzer.enabled` | boolean | `true` | Turn the analyzer on or off. |
| `ansibleAnalyzer.java.home` | string | `""` | Path to Java 17+. Leave empty to use `JAVA_HOME` / `PATH`. |
| `ansibleAnalyzer.rulesReplaceDefaults` | boolean | `false` | When true, only the rules listed in `ansibleAnalyzer.rules` run (e.g. after Import from SonarQube). When false, listed rules are overrides and unlisted rules use the extension default profile. |
| `ansibleAnalyzer.rules` | object | `{}` | Per-rule options: severity, enable/disable, and rule-specific properties. Add only the rule IDs you want to change; unlisted rules use the default unless `rulesReplaceDefaults` is true. |

### How to alter rules

You only need to add **overrides** in settings unless you have imported from SonarQube or want to replace the default set.

- **Normal case** - Set `ansibleAnalyzer.rules` to `{}` or omit it and `ansibleAnalyzer.rulesReplaceDefaults` to `false` to use the default profile. To change only a few rules, add just those keys; all other rules keep the default.
- **Replace mode** - Set `ansibleAnalyzer.rulesReplaceDefaults` to `true`. Only the rules listed in `ansibleAnalyzer.rules` run. Use this after **Ansible: Import rules from SonarQube** or when you maintain your own full list.
- **After an upgrade** - Entries for removed or renamed rules are ignored; you can delete them to tidy up.

Each rule entry can include:

- `enabled` - `true` or `false`
- `severity` - `"blocker"`, `"critical"`, `"major"`, `"minor"`, or `"info"` (Sonar-style)
- Rule-specific properties (e.g. `maxLength`, `maxTasks`) as documented per rule

Example (overriding a few rules only):

```json
{
  "ansibleAnalyzer.java.home": "C:\\path\\to\\java17",
  "ansibleAnalyzer.rules": {
    "qa-max-line-length": { "enabled": true, "severity": "minor", "maxLength": "120" },
    "qa-task-name-min-chars": { "enabled": false }
  }
}
```

### Aligning with a SonarQube quality profile

Use the command **Ansible: Import rules from SonarQube** (Command Palette or right-click). Enter your SonarQube server URL, the Ansible quality profile name (or key), and a token if the server requires authentication. The extension fetches the profile's active rules and severities from the API and writes them to `ansibleAnalyzer.rules` in your workspace or user settings, and sets `ansibleAnalyzer.rulesReplaceDefaults` to `true`.

**Privacy and storage:** The server URL and profile name are saved as soon as you enter them (so they appear pre-filled next time). They are stored in the extension's global state (not in your settings files). Your **token is never stored**; it is used only in memory for that run. If the token input closes when you switch to another app to copy the token, run the command again - URL and profile will already be filled in and you only need to paste the token.

## Rules

A default set of rules is active for YAML validity, style, task structure, naming, safety, and best practices. You can enable additional rules or adjust severity in settings. Rule keys use the `qa-` prefix and match the SonarQube plugin.

## Features

- **Real-time diagnostics** - Squiggles and Problems panel as you type.
- **Configurable rules** - Override severity or disable rules; set properties (e.g. max line length, max tasks per play).
- **Import from SonarQube** - Pull your Ansible quality profile into workspace or user settings; URL and profile remembered, token never stored.

## Companion: SonarQube Plugin

The [Qualimetry Ansible Analyzer for SonarQube](https://github.com/Qualimetry/sonarqube-ansible-plugin) runs the same rules in CI/CD. Use both for consistent quality from editor to pipeline.

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). Copyright 2026 SHAZAM Analytics Ltd.
