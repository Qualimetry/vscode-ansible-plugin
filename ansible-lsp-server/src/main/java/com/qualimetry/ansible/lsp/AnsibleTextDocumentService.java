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
import com.qualimetry.sonar.ansible.analyzer.parser.AnsibleParser;
import com.qualimetry.sonar.ansible.analyzer.parser.model.PlaybookFile;
import com.qualimetry.sonar.ansible.analyzer.visitor.AnsibleContext;
import com.qualimetry.sonar.ansible.analyzer.visitor.AnsibleWalker;
import com.qualimetry.sonar.ansible.analyzer.visitor.BaseCheck;
import com.qualimetry.sonar.ansible.analyzer.parser.model.TextPosition;
import com.qualimetry.sonar.ansible.analyzer.visitor.Issue;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Text document service: on open/change, run analyzer and publish diagnostics.
 */
public class AnsibleTextDocumentService implements org.eclipse.lsp4j.services.TextDocumentService {

    private final ConcurrentHashMap<String, String> documents = new ConcurrentHashMap<>();
    private volatile LspConfig config = LspConfig.defaults();
    private LanguageClient client;
    private final AnsibleParser parser = new AnsibleParser();
    private final List<BaseCheck> checks = new ArrayList<>();

    public AnsibleTextDocumentService() {
        try {
            for (Class<? extends BaseCheck> clazz : CheckList.getAllChecks()) {
                checks.add(clazz.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate checks", e);
        }
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    public void setConfig(LspConfig config) {
        this.config = config != null ? config : LspConfig.defaults();
    }

    /**
     * Re-analyzes all open documents and publishes diagnostics. Used after config change.
     */
    public void reAnalyzeAll() {
        for (var e : documents.entrySet()) {
            analyzeAndPublish(e.getKey(), e.getValue());
        }
    }

    @Override
    public void didOpen(org.eclipse.lsp4j.DidOpenTextDocumentParams params) {
        documents.put(params.getTextDocument().getUri(), params.getTextDocument().getText());
        analyzeAndPublish(params.getTextDocument().getUri(), params.getTextDocument().getText());
    }

    @Override
    public void didChange(org.eclipse.lsp4j.DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String fullText = params.getContentChanges().isEmpty()
                ? documents.get(uri)
                : params.getContentChanges().get(0).getText();
        if (fullText != null) {
            documents.put(uri, fullText);
            analyzeAndPublish(uri, fullText);
        }
    }

    @Override
    public void didSave(org.eclipse.lsp4j.DidSaveTextDocumentParams params) {
        // Re-analyze on save if needed; didChange already covers content
    }

    @Override
    public void didClose(org.eclipse.lsp4j.DidCloseTextDocumentParams params) {
        documents.remove(params.getTextDocument().getUri());
        if (client != null) {
            client.publishDiagnostics(new PublishDiagnosticsParams(params.getTextDocument().getUri(), List.of()));
        }
    }

    private void analyzeAndPublish(String uri, String content) {
        if (client == null) return;

        try {
            PlaybookFile playbook = parser.parse(uri, content != null ? content : "");
            AnsibleContext context = new AnsibleContext(playbook, null, content != null ? content : "");
            for (BaseCheck check : checks) {
                check.setContext(context);
                AnsibleWalker.walk(playbook, check);
            }

            LspConfig cfg = config;
            List<Diagnostic> diagnostics = new ArrayList<>();
            for (Issue issue : context.getIssues()) {
                if (!cfg.isRuleEnabled(issue.ruleKey())) continue;
                Diagnostic d = toDiagnostic(issue, cfg);
                if (d != null) diagnostics.add(d);
            }

            client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
        } catch (Exception e) {
            // Publish empty so editor clears stale diagnostics; avoid crashing the server
            client.publishDiagnostics(new PublishDiagnosticsParams(uri, List.of()));
        }
    }

    /**
     * Maps an analyzer Issue to an LSP Diagnostic with range and severity.
     * Uses config for severity override when available.
     */
    static Diagnostic toDiagnostic(Issue issue, LspConfig config) {
        Diagnostic d = new Diagnostic();
        d.setMessage(issue.message());
        d.setCode(issue.ruleKey());
        d.setSource("qualimetry-ansible");
        d.setSeverity(config != null ? config.getSeverity(issue.ruleKey()) : DiagnosticSeverity.Warning);
        d.setRange(issueToRange(issue));
        return d;
    }

    private static Range issueToRange(Issue issue) {
        int line0;
        int startChar;
        int endChar;
        TextPosition pos = issue.position();
        Integer line = issue.line();
        Integer endCol = issue.endColumn();
        if (pos != null) {
            line0 = Math.max(0, pos.line() - 1);
            startChar = Math.max(0, pos.column() - 1);
            endChar = endCol != null && endCol > startChar ? endCol : startChar + 1;
        } else if (line != null && line > 0) {
            line0 = line - 1;
            startChar = 0;
            endChar = endCol != null && endCol > 0 ? endCol : 256;
        } else {
            line0 = 0;
            startChar = 0;
            endChar = 256;
        }
        return new Range(new Position(line0, startChar), new Position(line0, endChar));
    }
}
