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

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AnsibleWorkspaceServiceTest {

    @Test
    void didChangeConfiguration_triggersFetchConfigAndReanalyze() {
        AtomicInteger callCount = new AtomicInteger();
        AnsibleLanguageServer server = new AnsibleLanguageServer() {
            @Override
            public void fetchConfigAndReanalyze() {
                callCount.incrementAndGet();
            }
        };
        AnsibleWorkspaceService service = new AnsibleWorkspaceService(server);

        service.didChangeConfiguration(new DidChangeConfigurationParams());

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void didChangeWatchedFiles_doesNotThrow() {
        AnsibleLanguageServer server = new AnsibleLanguageServer();
        AnsibleWorkspaceService service = new AnsibleWorkspaceService(server);

        assertThatCode(() -> service.didChangeWatchedFiles(new DidChangeWatchedFilesParams(List.of())))
                .doesNotThrowAnyException();
    }
}
