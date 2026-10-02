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


package io.seqera.tower.cli.commands.managedidentities.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.managedidentities.AbstractManagedIdentitiesCmd;
import io.seqera.tower.cli.commands.managedidentities.CredentialsCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsDeleted;
import io.seqera.tower.model.ListManagedCredentialsRespDto;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "delete",
        description = "Delete the SSH credentials of an organization member from a managed identity"
)
public class DeleteCmd extends AbstractManagedIdentitiesCmd {

    @ParentCommand
    public CredentialsCmd parent;

    @Option(names = {"-m", "--member"}, description = "Platform username of the organization member the credentials belong to. Only organization owners can delete credentials of other members [default: you].")
    public String member;

    @Option(names = {"--force"}, description = "Delete the credentials even if running jobs use them. Those jobs are stopped. By default, credentials in use are not deleted.")
    public boolean force = false;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = parent.orgId();
        ManagedIdentityDbDtoAbstractGridConfig identity = parent.managedIdentity(orgId);
        ListManagedCredentialsRespDto credentials = parent.memberCredentials(orgId, identity, member);
        Long id = credentials.getManagedCredentialsId();

        deleteUnlessInUse(id.toString(), () -> identitiesApi().deleteManagedCredentials(identity.getId(), id, orgId, !force));

        return new ManagedCredentialsDeleted(identity.getName(), id);
    }
}
