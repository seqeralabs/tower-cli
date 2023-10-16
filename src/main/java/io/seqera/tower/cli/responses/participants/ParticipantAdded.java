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

package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;

public class ParticipantAdded extends Response {

    final public ParticipantDbDto participant;
    final public String workspaceName;

    public ParticipantAdded(ParticipantDbDto participant, String workspaceName) {
        this.participant = participant;
        this.workspaceName = workspaceName;
    }

    @Override
    public String toString() {

        if (participant.getType() == ParticipantType.TEAM) {
            return ansi(String.format("%n  @|yellow Team '%s' was added as participant to '%s' workspace with role '%s'|@%n", participant.getTeamName(), workspaceName, participant.getWspRole()));
        }

        return ansi(String.format("%n  @|yellow User '%s' was added as participant to '%s' workspace with role '%s'|@%n", participant.getUserName(), workspaceName, participant.getWspRole()));
    }

}
