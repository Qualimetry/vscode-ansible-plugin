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

import com.qualimetry.sonar.ansible.analyzer.parser.model.TextPosition;
import com.qualimetry.sonar.ansible.analyzer.visitor.Issue;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnsibleTextDocumentServiceDiagnosticsTest {

    @Test
    void toDiagnostic_setsMessageCodeSourceAndSeverity() {
        Issue issue = new Issue("qa-spaces-not-tabs", "Do not use tabs", null, 2, null, null);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        assertThat(d.getMessage()).isEqualTo("Do not use tabs");
        assertThat(d.getCode()).isNotNull();
        assertThat(d.getCode().getLeft()).isEqualTo("qa-spaces-not-tabs");
        assertThat(d.getSource()).isEqualTo("qualimetry-ansible");
        assertThat(d.getSeverity()).isEqualTo(DiagnosticSeverity.Warning);
    }

    @Test
    void toDiagnostic_withLineOnly_setsRangeToLine() {
        Issue issue = new Issue("qa-valid-yaml", "Parse error", null, 3, null, null);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        Range range = d.getRange();
        assertThat(range.getStart()).isEqualTo(new Position(2, 0));
        assertThat(range.getEnd()).isEqualTo(new Position(2, 256));
    }

    @Test
    void toDiagnostic_withPosition_setsRangeFromPosition() {
        TextPosition pos = new TextPosition(5, 10);
        Issue issue = new Issue("qa-task-has-name", "Task should have a name", pos, 5, null, null);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        Range range = d.getRange();
        assertThat(range.getStart()).isEqualTo(new Position(4, 9));
        assertThat(range.getEnd()).isEqualTo(new Position(4, 10));
    }

    @Test
    void toDiagnostic_withPositionAndEndColumn_setsRangeToEndColumn() {
        TextPosition pos = new TextPosition(1, 2);
        Issue issue = new Issue("qa-task-name-first", "Invalid order", pos, 1, null, 20);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        Range range = d.getRange();
        assertThat(range.getStart()).isEqualTo(new Position(0, 1));
        assertThat(range.getEnd()).isEqualTo(new Position(0, 20));
    }

    @Test
    void toDiagnostic_withLineAndEndColumn_setsRange() {
        Issue issue = new Issue("qa-max-line-length", "Line too long", null, 10, null, 120);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        Range range = d.getRange();
        assertThat(range.getStart()).isEqualTo(new Position(9, 0));
        assertThat(range.getEnd()).isEqualTo(new Position(9, 120));
    }

    @Test
    void toDiagnostic_zeroLine_usesZeroBasedRange() {
        Issue issue = new Issue("qa-valid-yaml", "Parse error", null, null, null, null);
        Diagnostic d = AnsibleTextDocumentService.toDiagnostic(issue, LspConfig.defaults());
        Range range = d.getRange();
        assertThat(range.getStart()).isEqualTo(new Position(0, 0));
        assertThat(range.getEnd()).isEqualTo(new Position(0, 256));
    }
}
