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

public class DataStudioStopSubmitted extends Response {

    public final String sessionId;
    public final String userSuppliedStudioIdentifier;

    public final Long workspaceId;
    public final String workspaceRef;
    public final Boolean jobSubmitted;

    public DataStudioStopSubmitted(String sessionId, String userSuppliedStudioIdentifier, Long workspaceId, String workspaceRef, Boolean jobSubmitted) {
        this.sessionId = sessionId;
        this.userSuppliedStudioIdentifier = userSuppliedStudioIdentifier;
        this.workspaceId = workspaceId;
        this.workspaceRef = workspaceRef;
        this.jobSubmitted = jobSubmitted;
    }

    @Override
    public String toString() {
        String isSuccess = jobSubmitted ? "successfully submitted" : "failed to submit";
        return ansi(String.format("%n  @|yellow Studio %s STOP %s at %s workspace.|@%n%n    ", userSuppliedStudioIdentifier, isSuccess, workspaceRef));
    }

}
