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
        ListMembersResponse response = orgsApi().listOrganizationMembers(orgId, null, null, user);

        if(response.getMembers() == null || response.getMembers().size() == 0){
            throw new MembersNotFoundException(user, orgId);
        }

        if(response.getMembers().size() > 1){
            throw new MembersMultiplicityException(user, orgId);
        }

        return response.getMembers().stream().findFirst().orElse(null);
    }
}
