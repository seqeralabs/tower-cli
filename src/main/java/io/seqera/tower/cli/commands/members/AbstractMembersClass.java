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

package io.seqera.tower.cli.commands.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MembersMultiplicityException;
import io.seqera.tower.cli.exceptions.MembersNotFoundException;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.MemberDbDto;
import picocli.CommandLine;

@CommandLine.Command
abstract public class AbstractMembersClass extends AbstractApiCmd {

    public AbstractMembersClass() {
    }

    protected MemberDbDto findMemberByUser(Long orgId, String user) throws ApiException {
        ListMembersResponse response = api().listOrganizationMembers(orgId, null, null, user);

        if(response.getMembers() == null || response.getMembers().size() == 0){
            throw new MembersNotFoundException(user, orgId);
        }

        if(response.getMembers().size() > 1){
            throw new MembersMultiplicityException(user, orgId);
        }

        return response.getMembers().stream().findFirst().orElse(null);
    }
}
