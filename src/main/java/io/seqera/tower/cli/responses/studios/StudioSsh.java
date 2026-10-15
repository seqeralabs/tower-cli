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

package io.seqera.tower.cli.responses.studios;

import io.seqera.tower.model.DataStudioDto;

/**
 * Helpers for the SSH connection command that Platform returns for studios with SSH enabled.
 */
public final class StudioSsh {

    private StudioSsh() {}

    /**
     * The ready-to-use SSH command from the studio's SSH details, or {@code null} when SSH is not enabled.
     * Platform builds it (including the session-prefixed user), so the CLI never assembles it itself.
     */
    public static String commandOf(DataStudioDto studio) {
        if (studio == null || studio.getSshDetails() == null) {
            return null;
        }
        String command = studio.getSshDetails().getCommand();
        return command == null || command.isBlank() ? null : command;
    }

    static String commandLine(String sshCommand) {
        return sshCommand == null ? "" : String.format("%n  SSH: @|bold %s|@%n", sshCommand);
    }
}
