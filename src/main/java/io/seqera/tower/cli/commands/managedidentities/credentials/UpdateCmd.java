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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.managedidentities.CredentialsCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsUpdated;
import io.seqera.tower.model.ListManagedCredentialsRespDto;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command(
        name = "update",
        description = "Replace the SSH credentials of an organization member in a managed identity"
)
public class UpdateCmd extends AbstractApiCmd {

    @ParentCommand
    public CredentialsCmd parent;

    @Option(names = {"-m", "--member"}, description = "Platform username of the organization member the credentials belong to. Only organization owners can update credentials of other members [default: you].")
    public String member;

    @Mixin
    public SshCredentialsOptions ssh;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long orgId = parent.orgId();
        ManagedIdentityDbDtoAbstractGridConfig identity = parent.managedIdentity(orgId);
        ListManagedCredentialsRespDto credentials = parent.memberCredentials(orgId, identity, member);

        identitiesApi().updateManagedCredentials(
                identity.getId(), credentials.getManagedCredentialsId(), CredentialsCmd.updateRequest(ssh.securityKeys(), ssh.linuxUserName), orgId);

        return new ManagedCredentialsUpdated(identity.getName(), credentials.getManagedCredentialsId());
    }
}
