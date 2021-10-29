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

package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;

public class TeamMemberDeleted extends Response {

    public final String teamName;
    public final String username;

    public TeamMemberDeleted(String teamName, String username) {
        this.teamName = teamName;
        this.username = username;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Team member '%s' deleted at '%s' team|@%n", username, teamName));
    }

}
