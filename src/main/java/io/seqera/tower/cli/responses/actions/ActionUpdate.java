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

package io.seqera.tower.cli.responses.actions;

import io.seqera.tower.cli.responses.Response;

public class ActionUpdate extends Response {

    public final String actionName;
    public final String workspaceRef;
    public final String actionId;

    public ActionUpdate(String actionName, String workspaceRef, String actionId) {
        this.actionName = actionName;
        this.workspaceRef = workspaceRef;
        this.actionId = actionId;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline action '%s' updated at %s workspace with id '%s'|@%n", actionName, workspaceRef, actionId));
    }
}
