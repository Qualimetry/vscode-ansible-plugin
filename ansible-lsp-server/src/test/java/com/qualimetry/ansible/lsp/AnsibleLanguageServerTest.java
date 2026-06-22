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

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class AnsibleLanguageServerTest {

    @Test
    void initialize_returnsFullSyncCapability() throws ExecutionException, InterruptedException {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        InitializeResult result = server.initialize(new InitializeParams()).get();

        assertThat(result.getCapabilities().getTextDocumentSync().getLeft())
                .isEqualTo(TextDocumentSyncKind.Full);
    }

    @Test
    void initialize_returnsServerInfo() throws ExecutionException, InterruptedException {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        InitializeResult result = server.initialize(new InitializeParams()).get();

        assertThat(result.getServerInfo()).isNotNull();
        assertThat(result.getServerInfo().getName()).isEqualTo("Ansible Analyzer");
        assertThat(result.getServerInfo().getVersion()).isNotBlank();
    }

    @Test
    void shutdown_returnsNull() throws ExecutionException, InterruptedException {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        assertThat(server.shutdown().get()).isNull();
    }

    @Test
    void getTextDocumentService_returnsNonNull() {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        TextDocumentService tds = server.getTextDocumentService();
        assertThat(tds).isNotNull().isInstanceOf(AnsibleTextDocumentService.class);
    }

    @Test
    void getWorkspaceService_returnsNonNull() {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        WorkspaceService ws = server.getWorkspaceService();
        assertThat(ws).isNotNull().isInstanceOf(AnsibleWorkspaceService.class);
    }

    @Test
    void clientIsNullBeforeConnect() {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        assertThat(server.getClient()).isNull();
    }
}
