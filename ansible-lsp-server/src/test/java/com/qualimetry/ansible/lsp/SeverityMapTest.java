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

import static org.assertj.core.api.Assertions.assertThat;

class SeverityMapTest {

    @Test
    void getSeverity_returnsWarningForAnyRule() {
        assertThat(SeverityMap.getSeverity("qa-valid-yaml")).isEqualTo(DiagnosticSeverity.Warning);
        assertThat(SeverityMap.getSeverity("qa-spaces-not-tabs")).isEqualTo(DiagnosticSeverity.Warning);
    }

    @Test
    void getSeverity_returnsWarningForUnknownRule() {
        assertThat(SeverityMap.getSeverity("unknown-rule")).isEqualTo(DiagnosticSeverity.Warning);
    }

    @Test
    void sonarToDiagnosticSeverity_blockerMapsToError() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("blocker")).isEqualTo(DiagnosticSeverity.Error);
    }

    @Test
    void sonarToDiagnosticSeverity_criticalMapsToError() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("critical")).isEqualTo(DiagnosticSeverity.Error);
    }

    @Test
    void sonarToDiagnosticSeverity_majorMapsToWarning() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("major")).isEqualTo(DiagnosticSeverity.Warning);
    }

    @Test
    void sonarToDiagnosticSeverity_minorMapsToInformation() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("minor")).isEqualTo(DiagnosticSeverity.Information);
    }

    @Test
    void sonarToDiagnosticSeverity_infoMapsToHint() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("info")).isEqualTo(DiagnosticSeverity.Hint);
    }

    @Test
    void sonarToDiagnosticSeverity_caseInsensitive() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("BLOCKER")).isEqualTo(DiagnosticSeverity.Error);
        assertThat(SeverityMap.sonarToDiagnosticSeverity("Major")).isEqualTo(DiagnosticSeverity.Warning);
        assertThat(SeverityMap.sonarToDiagnosticSeverity("MINOR")).isEqualTo(DiagnosticSeverity.Information);
    }

    @Test
    void sonarToDiagnosticSeverity_trimsWhitespace() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("  critical  ")).isEqualTo(DiagnosticSeverity.Error);
    }

    @Test
    void sonarToDiagnosticSeverity_nullReturnsNull() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity(null)).isNull();
    }

    @Test
    void sonarToDiagnosticSeverity_blankReturnsNull() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("  ")).isNull();
    }

    @Test
    void sonarToDiagnosticSeverity_unknownValueReturnsNull() {
        assertThat(SeverityMap.sonarToDiagnosticSeverity("unknown")).isNull();
    }
}
