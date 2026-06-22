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

import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * LSP server for Ansible: provides diagnostics for playbook files.
 */
public class AnsibleLanguageServer implements LanguageServer {

    private LanguageClient client;
    private final AnsibleTextDocumentService textDocumentService;
    private final AnsibleWorkspaceService workspaceService;

    public AnsibleLanguageServer() {
        this.textDocumentService = new AnsibleTextDocumentService();
        this.workspaceService = new AnsibleWorkspaceService(this);
    }

    public void connect(LanguageClient client) {
        this.client = client;
        textDocumentService.setClient(client);
        fetchConfigAndReanalyze();
    }

    /**
     * Fetches workspace configuration (e.g. ansibleAnalyzer.rules.disabled/enabled) and re-analyzes open documents.
     */
    public void fetchConfigAndReanalyze() {
        LanguageClient c = client;
        if (c == null) return;
        ConfigurationItem item = new ConfigurationItem();
        item.setSection("ansibleAnalyzer");
        ConfigurationParams params = new ConfigurationParams();
        params.setItems(Collections.singletonList(item));
        c.configuration(params).thenAccept(list -> {
            Object section = list != null && !list.isEmpty() ? list.get(0) : null;
            textDocumentService.setConfig(LspConfig.fromConfiguration(section));
            textDocumentService.reAnalyzeAll();
        }).exceptionally(t -> {
            textDocumentService.setConfig(LspConfig.defaults());
            textDocumentService.reAnalyzeAll();
            return null;
        });
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        ServerInfo serverInfo = new ServerInfo("Ansible Analyzer", "1.1.0");
        return CompletableFuture.completedFuture(new InitializeResult(capabilities, serverInfo));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    public LanguageClient getClient() {
        return client;
    }
}
