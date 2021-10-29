/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
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
