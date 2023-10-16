/*
 * Copyright 2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

public class ManageLabels extends Response {

    public String operation;
    public String type;

    public final String id;

    public final Long workspaceID;


    public ManageLabels(String operation, String type, String id, Long workspaceID) {
        this.operation = operation;
        this.type = type;
        this.id = id;
        this.workspaceID = workspaceID;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n @|yellow '%s' labels on '%s' with id '%s' at %s workspace|@%n",
                operation, type, id, workspaceID == null ? "user" : workspaceID));
    }
}
