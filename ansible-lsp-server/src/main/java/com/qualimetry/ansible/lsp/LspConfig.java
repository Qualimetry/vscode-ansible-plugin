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

import com.qualimetry.sonar.ansible.analyzer.checks.CheckList;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule configuration for the LSP server.
 * Supports rules object (ruleKey -> { enabled, severity, ... }) and rulesReplaceDefaults:
 * when true, only listed rules run; when false, listed rules are overrides and unlisted use default profile.
 */
public final class LspConfig {

    private final Set<String> disabledRuleKeys;
    private final Set<String> enabledRuleKeys;
    private final boolean rulesReplaceDefaults;
    private final Map<String, Object> rulesObject;
    private final Map<String, DiagnosticSeverity> severityOverrides;

    public LspConfig(Set<String> disabledRuleKeys, Set<String> enabledRuleKeys,
                     boolean rulesReplaceDefaults, Map<String, Object> rulesObject,
                     Map<String, DiagnosticSeverity> severityOverrides) {
        this.disabledRuleKeys = disabledRuleKeys == null ? Set.of() : Set.copyOf(disabledRuleKeys);
        this.enabledRuleKeys = enabledRuleKeys == null || enabledRuleKeys.isEmpty()
                ? null : Set.copyOf(enabledRuleKeys);
        this.rulesReplaceDefaults = rulesReplaceDefaults;
        this.rulesObject = rulesObject == null ? Map.of() : Map.copyOf(rulesObject);
        this.severityOverrides = severityOverrides == null ? Map.of() : Map.copyOf(severityOverrides);
    }

    public static LspConfig defaults() {
        return new LspConfig(Set.of(), null, false, Map.of(), Map.of());
    }

    /**
     * Returns whether the rule is enabled based on rules object, rulesReplaceDefaults, and default profile.
     */
    public boolean isRuleEnabled(String ruleKey) {
        Set<String> defaultKeys = new HashSet<>(CheckList.getDefaultRuleKeys());
        if (disabledRuleKeys.contains(ruleKey)) return false;
        if (enabledRuleKeys != null) {
            return enabledRuleKeys.contains(ruleKey);
        }
        return isEnabledFromRulesObject(ruleKey, defaultKeys);
    }

    private boolean isEnabledFromRulesObject(String key, Set<String> defaultKeys) {
        if (rulesObject != null && rulesObject.containsKey(key)) {
            Object val = rulesObject.get(key);
            if (val instanceof Map) {
                Object enabled = ((Map<?, ?>) val).get("enabled");
                if (enabled instanceof Boolean) {
                    return (Boolean) enabled;
                }
                return true;
            }
        }
        if (rulesReplaceDefaults && rulesObject != null && !rulesObject.isEmpty()) {
            return false;
        }
        return defaultKeys.contains(key);
    }

    /**
     * Returns the effective diagnostic severity for a rule (user override or default).
     */
    public DiagnosticSeverity getSeverity(String ruleKey) {
        if (severityOverrides != null && severityOverrides.containsKey(ruleKey)) {
            return severityOverrides.get(ruleKey);
        }
        return SeverityMap.getSeverity(ruleKey);
    }

    public Set<String> getDisabledRuleKeys() {
        return Collections.unmodifiableSet(disabledRuleKeys);
    }

    public Set<String> getEnabledRuleKeys() {
        return enabledRuleKeys == null ? null : Collections.unmodifiableSet(enabledRuleKeys);
    }

    /**
     * Parses LspConfig from the workspace/configuration response section (ansibleAnalyzer).
     * Supports:
     * - rulesReplaceDefaults (boolean)
     * - rules (object): ruleKey -> { enabled?, severity?, ... }
     * Legacy: rules.disabled and rules.enabled arrays are still supported.
     */
    @SuppressWarnings("unchecked")
    public static LspConfig fromConfiguration(Object section) {
        if (section == null) return defaults();
        if (!(section instanceof Map)) return defaults();
        Map<String, Object> map = (Map<String, Object>) section;

        boolean rulesReplaceDefaults = false;
        if (map.get("rulesReplaceDefaults") instanceof Boolean) {
            rulesReplaceDefaults = (Boolean) map.get("rulesReplaceDefaults");
        }

        Object rulesObj = map.get("rules");
        Set<String> disabled = new HashSet<>();
        Set<String> enabled = new HashSet<>();
        Map<String, Object> rulesObject = new HashMap<>();
        Map<String, DiagnosticSeverity> severityOverrides = new HashMap<>();

        if (rulesObj instanceof Map) {
            Map<String, Object> rules = (Map<String, Object>) rulesObj;
            for (Object v : toList(rules.get("disabled"))) {
                if (v instanceof String) disabled.add((String) v);
            }
            for (Object v : toList(rules.get("enabled"))) {
                if (v instanceof String) enabled.add((String) v);
            }
            for (Map.Entry<String, Object> e : rules.entrySet()) {
                if ("disabled".equals(e.getKey()) || "enabled".equals(e.getKey())) continue;
                if (e.getValue() instanceof Map) {
                    Map<String, Object> ruleEntry = (Map<String, Object>) e.getValue();
                    rulesObject.put(e.getKey(), ruleEntry);
                    Object sev = ruleEntry.get("severity");
                    if (sev instanceof String) {
                        DiagnosticSeverity ds = SeverityMap.sonarToDiagnosticSeverity((String) sev);
                        if (ds != null) severityOverrides.put(e.getKey(), ds);
                    }
                }
            }
        }

        return new LspConfig(
                disabled,
                enabled.isEmpty() ? null : enabled,
                rulesReplaceDefaults,
                rulesObject,
                severityOverrides
        );
    }

    private static List<?> toList(Object o) {
        if (o instanceof List) return (List<?>) o;
        return new ArrayList<>();
    }
}
