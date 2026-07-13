/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.responses;

import io.seqera.tower.model.CredentialsStatus;
import picocli.CommandLine;

public class CredentialsValidated extends Response {

    public final String id;
    public final String name;
    public final String workspaceRef;
    public final CredentialsStatus status;
    public final boolean transientError;
    public final String message;

    public CredentialsValidated(String id, String name, String workspaceRef, CredentialsStatus status, boolean transientError, String message) {
        this.id = id;
        this.name = name;
        this.workspaceRef = workspaceRef;
        this.status = status;
        this.transientError = transientError;
        this.message = message;
    }

    @Override
    public String toString() {
        String color = status == CredentialsStatus.AVAILABLE ? "green" : "red";
        StringBuilder result = new StringBuilder(ansi(String.format(
                "%n  @|bold Credentials '%s'|@ at %s workspace validated as @|%s %s|@%n",
                id, workspaceRef, color, status)));

        if (transientError) {
            result.append(ansi(String.format(
                    "  @|yellow The provider could not be reached; the reported status is the last known value.|@%n")));
        }
        if (message != null && !message.isEmpty()) {
            result.append(String.format("  Detail: %s%n", message));
        }
        return result.toString();
    }

    @Override
    public int getExitCode() {
        return status == CredentialsStatus.INVALID ? CommandLine.ExitCode.SOFTWARE : CommandLine.ExitCode.OK;
    }
}
