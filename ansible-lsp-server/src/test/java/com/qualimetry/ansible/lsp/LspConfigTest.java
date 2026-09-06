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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LspConfigTest {

    @Test
    void defaults_allowsAllRules() {
        LspConfig cfg = LspConfig.defaults();
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isTrue();
        assertThat(cfg.getDisabledRuleKeys()).isEmpty();
        assertThat(cfg.getEnabledRuleKeys()).isNull();
    }

    @Test
    void disabledRule_isNotEnabled() {
        LspConfig cfg = new LspConfig(Set.of("qa-spaces-not-tabs", "qa-max-line-length"), null, false, Map.of(), Map.of());
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isFalse();
        assertThat(cfg.isRuleEnabled("qa-max-line-length")).isFalse();
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.getDisabledRuleKeys()).containsExactlyInAnyOrder("qa-spaces-not-tabs", "qa-max-line-length");
    }

    @Test
    void enabledRuleKeys_onlyThoseEnabled() {
        LspConfig cfg = new LspConfig(Set.of(), Set.of("qa-valid-yaml", "qa-spaces-not-tabs"), false, Map.of(), Map.of());
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isTrue();
        assertThat(cfg.isRuleEnabled("qa-max-line-length")).isFalse();
        assertThat(cfg.getEnabledRuleKeys()).containsExactlyInAnyOrder("qa-valid-yaml", "qa-spaces-not-tabs");
    }

    @Test
    void disabledTakesPrecedenceOverEnabled() {
        LspConfig cfg = new LspConfig(Set.of("qa-spaces-not-tabs"), Set.of("qa-valid-yaml", "qa-spaces-not-tabs"), false, Map.of(), Map.of());
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isFalse();
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
    }

    @Test
    void fromConfiguration_nullReturnsDefaults() {
        LspConfig cfg = LspConfig.fromConfiguration(null);
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.getDisabledRuleKeys()).isEmpty();
    }

    @Test
    void fromConfiguration_nonMapReturnsDefaults() {
        LspConfig cfg = LspConfig.fromConfiguration("not-a-map");
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
    }

    @Test
    void fromConfiguration_parsesDisabledList() {
        Map<String, Object> section = Map.of(
                "rules", Map.of("disabled", List.of("qa-spaces-not-tabs", "qa-max-line-length"))
        );
        LspConfig cfg = LspConfig.fromConfiguration(section);
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isFalse();
        assertThat(cfg.isRuleEnabled("qa-max-line-length")).isFalse();
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
    }

    @Test
    void fromConfiguration_parsesEnabledList() {
        Map<String, Object> section = Map.of(
                "rules", Map.of("enabled", List.of("qa-valid-yaml"))
        );
        LspConfig cfg = LspConfig.fromConfiguration(section);
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.isRuleEnabled("qa-spaces-not-tabs")).isFalse();
    }

    @Test
    void fromConfiguration_emptyEnabledUsesAllRules() {
        Map<String, Object> section = Map.of(
                "rules", Map.of("enabled", List.of())
        );
        LspConfig cfg = LspConfig.fromConfiguration(section);
        assertThat(cfg.isRuleEnabled("qa-valid-yaml")).isTrue();
        assertThat(cfg.getEnabledRuleKeys()).isNull();
    }
}
