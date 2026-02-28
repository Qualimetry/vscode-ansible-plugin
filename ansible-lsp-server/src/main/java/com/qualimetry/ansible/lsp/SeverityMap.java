/*
 * Copyright 2026 SHAZAM Analytics Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qualimetry.ansible.lsp;

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * Maps Sonar severity names to LSP {@link DiagnosticSeverity}.
 */
public final class SeverityMap {

    private SeverityMap() {
    }

    /**
     * Returns the default LSP diagnostic severity for a rule (when not overridden by settings).
     */
    public static DiagnosticSeverity getSeverity(String ruleKey) {
        return DiagnosticSeverity.Warning;
    }

    /**
     * Parses a Sonar severity string to LSP diagnostic severity.
     * Accepts: blocker, critical, major, minor, info (case-insensitive).
     *
     * @param s the severity string from config or SonarQube
     * @return the LSP severity, or null if invalid
     */
    public static DiagnosticSeverity sonarToDiagnosticSeverity(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        switch (s.trim().toLowerCase()) {
            case "blocker":
            case "critical":
                return DiagnosticSeverity.Error;
            case "major":
                return DiagnosticSeverity.Warning;
            case "minor":
                return DiagnosticSeverity.Information;
            case "info":
                return DiagnosticSeverity.Hint;
            default:
                return null;
        }
    }
}
