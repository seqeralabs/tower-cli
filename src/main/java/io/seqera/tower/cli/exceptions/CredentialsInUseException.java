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

package io.seqera.tower.cli.exceptions;

import io.seqera.tower.model.DeleteCredentialsConflictResponseConflict;

import java.util.List;

/**
 * Credentials were not deleted because running pipelines or Studio sessions use them.
 */
public class CredentialsInUseException extends TowerException {

    private final List<DeleteCredentialsConflictResponseConflict> conflicts;

    public CredentialsInUseException(String credentialsRef, List<DeleteCredentialsConflictResponseConflict> conflicts) {
        super(buildMessage(credentialsRef, conflicts));
        this.conflicts = conflicts;
    }

    public List<DeleteCredentialsConflictResponseConflict> getConflicts() {
        return conflicts;
    }

    private static String buildMessage(String credentialsRef, List<DeleteCredentialsConflictResponseConflict> conflicts) {
        StringBuilder message = new StringBuilder(String.format("Credentials '%s' are used by running jobs and were not deleted:", credentialsRef));
        for (DeleteCredentialsConflictResponseConflict conflict : conflicts) {
            message.append(String.format("%n  - %s '%s' (%s)", conflict.getType(), conflict.getName(), conflict.getId()));
            if (conflict.getUrl() != null) {
                message.append(" ").append(conflict.getUrl());
            }
        }
        return message.append(String.format("%nUse --force to delete them anyway. The jobs listed above will be stopped.")).toString();
    }
}
