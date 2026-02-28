# Qualimetry Ansible Analyzer - VSCode Plugin

[![CI](https://github.com/Qualimetry/vscode-ansible-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/Qualimetry/vscode-ansible-plugin/actions/workflows/ci.yml)

**Author**: The [Qualimetry](https://qualimetry.com) team at SHAZAM Analytics Ltd

A VS Code extension that provides real-time static analysis for Ansible playbooks and task files (`.yml`, `.yaml`), powered by the same analysis engine as the [Qualimetry Ansible Analyzer for SonarQube](https://github.com/Qualimetry/sonarqube-ansible-plugin). It brings the same rules directly into the editor so developers can identify and fix quality issues *before* committing - YAML syntax, naming, safety, and best practices at the earliest point in the workflow.

Where the SonarQube plugin enforces standards as part of the CI/CD quality gate, this extension provides the same feedback loop in real time as you write. The rules and their keys match the SonarQube plugin, so teams using both tools get a consistent experience from editor to pipeline.

## Installation

### From GitHub Releases

Download the latest `.vsix` from [GitHub Releases](https://github.com/Qualimetry/vscode-ansible-plugin/releases), then install via **Extensions: Install from VSIX...** (Command Palette).

### From Marketplace (when published)

Install the extension from the [VS Code Marketplace](https://marketplace.visualstudio.com/) and ensure Java 17+ is available.

## Requirements

- **Java 17+** - The analyzer runs on a Java-based language server. Java is auto-detected from `JAVA_HOME` or `PATH`. To set the path explicitly:

  ```json
  {
    "ansibleAnalyzer.java.home": "C:\\path\\to\\java17"
  }
  ```

## Configuration

Settings are under the `ansibleAnalyzer` namespace:

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `ansibleAnalyzer.enabled` | boolean | `true` | Enable or disable the analyzer. |
| `ansibleAnalyzer.java.home` | string | `""` | Path to a Java 17+ installation. When empty, the extension uses `JAVA_HOME` or `PATH`. |
| `ansibleAnalyzer.rules` | object | `{}` | Per-rule configuration. List rules to override or replace the default set (see below). |
| `ansibleAnalyzer.rulesReplaceDefaults` | boolean | `false` | When `true`, only the rules listed in `ansibleAnalyzer.rules` run (e.g. after Import from SonarQube). When `false`, listed rules are overrides and unlisted rules use the extension default profile. |

### How to customize rules

You can either **override** a few rules on top of the default profile, or **replace** the active set entirely (e.g. after importing from SonarQube).

- **Override mode** (default) – Set `ansibleAnalyzer.rulesReplaceDefaults` to `false` or omit it. Add only the rule IDs you want to change (e.g. severity or `enabled`); all other rules use the extension default. You do **not** need to list all rules.
- **Replace mode** – Set `ansibleAnalyzer.rulesReplaceDefaults` to `true`. Only the rules listed in `ansibleAnalyzer.rules` run. Use this when you have imported a profile from SonarQube or maintain your own full list.
- **Full default** – Omit `ansibleAnalyzer.rules` or set it to `{}` with `rulesReplaceDefaults` false to use the built-in default profile.
- **After an upgrade** – Entries for removed or renamed rules are ignored; you can delete them to tidy up.

Each rule can have `enabled`, `severity` (`blocker` \| `critical` \| `major` \| `minor` \| `info`), and rule-specific properties (e.g. `maxLength`, `maxTasks`).

### Example `settings.json`

```json
{
  "ansibleAnalyzer.enabled": true,
  "ansibleAnalyzer.java.home": "",
  "ansibleAnalyzer.rulesReplaceDefaults": false,
  "ansibleAnalyzer.rules": {
    "qa-max-line-length": {
      "enabled": true,
      "severity": "minor",
      "maxLength": "120"
    },
    "qa-task-name-min-chars": {
      "enabled": false
    }
  }
}
```

### Aligning with a SonarQube quality profile

Use the command **Ansible: Import rules from SonarQube** (Command Palette). Enter your SonarQube server URL, the Ansible quality profile name or key, and a token if required. The extension fetches the profile's active rules and severities and writes them to `ansibleAnalyzer.rules` and sets `ansibleAnalyzer.rulesReplaceDefaults` to `true`, so only those rules run (matching your SonarQube profile).

The last-used URL and profile are remembered (in the extension's global state, not in settings). Your **token is never stored**; it is used only for that run. To switch back to the extension default and only override a few rules, set `ansibleAnalyzer.rulesReplaceDefaults` to `false`.

## Features

- **Real-time diagnostics** - Issues appear as you type, with squiggly underlines and Problems panel entries.
- **Same rules as SonarQube** - Rule keys and behavior align with the Qualimetry Ansible plugin so editor and CI findings match.
- **Configurable rules** - Override severity or disable rules; set rule-specific properties (e.g. max line length, max tasks per play).
- **Import from SonarQube** - Pull your Ansible quality profile (rules and severities) into workspace or user settings; URL and profile remembered, token never stored. Bearer then Basic auth for compatibility.

## Companion: SonarQube plugin

This extension shares its analysis engine with the **[Qualimetry Ansible Analyzer for SonarQube](https://github.com/Qualimetry/sonarqube-ansible-plugin)** - a SonarQube plugin that runs the same rules as part of your CI/CD quality gate. Together they provide a consistent quality standard from the developer's editor through to the build pipeline:

| Tool | When it runs | Purpose |
|------|--------------|---------|
| **This VS Code extension** | As you type | Catch issues before you commit |
| **SonarQube plugin** | On CI/CD build | Enforce quality gate across the team |

Both tools use the same rule keys and severities, so findings are directly comparable.

## Building from source

### Prerequisites

- JDK 17+ and Maven 3.6+
- Node.js and npm

### Build steps

1. **Build the Java modules** (analyzer + LSP server):

   ```bash
   mvn clean package -pl ansible-lsp-server -am -DskipTests
   ```

2. **Build the VS Code client**:

   ```bash
   cd vscode-client
   npm install
   npm run compile
   ```

3. **Package as VSIX** (copy LSP JAR into client, then package):

   ```bash
   mkdir -p vscode-client/server
   cp ansible-lsp-server/target/ansible-lsp-server-*-shaded.jar vscode-client/server/ansible-lsp-server.jar
   cd vscode-client
   npx vsce package
   ```

   This produces a `.vsix` file in the `vscode-client/` directory.

## Contributing

Issues and feature requests are welcome. This project does not accept pull requests, commits, or other code contributions from third parties; the repository is maintained by the Qualimetry team only.

## License

This extension is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Copyright 2026 SHAZAM Analytics Ltd
