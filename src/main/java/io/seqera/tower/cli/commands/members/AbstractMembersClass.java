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

package io.seqera.tower.cli.commands.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MembersNotFoundException;
import io.seqera.tower.cli.utils.Paginator;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.MemberDbDto;
import picocli.CommandLine;

@CommandLine.Command
abstract public class AbstractMembersClass extends AbstractApiCmd {

    public AbstractMembersClass() {
    }

    // The search matches usernames, emails and names by substring, so keep only the exact username or email
    protected MemberDbDto findMemberByUser(Long orgId, String user) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> orgsApi().listOrganizationMembers(orgId, max, offset, user),
                ListMembersResponse::getMembers,
                ListMembersResponse::getTotalSize,
                it -> user.equalsIgnoreCase(it.getUserName()) || user.equalsIgnoreCase(it.getEmail())
        ).orElseThrow(() -> new MembersNotFoundException(user, orgId));
    }
}
