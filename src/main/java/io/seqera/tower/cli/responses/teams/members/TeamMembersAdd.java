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
import io.seqera.tower.model.MemberDbDto;

public class TeamMembersAdd extends Response {

    public final String teamName;
    public final MemberDbDto member;

    public TeamMembersAdd(String teamName, MemberDbDto member) {
        this.teamName = teamName;
        this.member = member;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' added to team '%s' with id '%d'|@%n", member.getUserName(), teamName, member.getMemberId()));
    }

}
