/*
 * Copyright 2021-2023, Seqera.
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

package io.seqera.tower.cli.responses.datastudios;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DataStudioDto;

public class DataStudiosCreated extends Response {

    public final String sessionId;
    public final String workspaceRef;
    public final Long workspaceId;
    public final boolean autoStart;
    public final String studioUrl;


    public DataStudiosCreated(String sessionId, Long workspaceId, String workspaceRef, String baseWorkspaceUrl, boolean autoStart) {
        this.sessionId = sessionId;
        this.workspaceRef = workspaceRef;
        this.workspaceId = workspaceId;
        this.autoStart = autoStart;
        this.studioUrl = String.format("%s/studios/%s/connect", baseWorkspaceUrl, sessionId);
    }

    @Override
    public String toString() {
        if (autoStart){
            return ansi(String.format("%n  @|yellow Data Studio %s CREATED at %s workspace and auto started.|@%n%n  To connect to this data studio session, copy and paste the following URL in your browser:%n%n    @|bold %s|@%n", sessionId, workspaceRef, studioUrl));
        } else {
            return ansi(String.format("%n  @|yellow Data Studio %s CREATED at %s workspace.|@%n", sessionId, workspaceRef));
        }
    }
}
