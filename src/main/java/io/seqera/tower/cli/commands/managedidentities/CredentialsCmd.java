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


package io.seqera.tower.cli.commands.managedidentities;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.managedidentities.credentials.AddCmd;
import io.seqera.tower.cli.commands.managedidentities.credentials.DeleteCmd;
import io.seqera.tower.cli.commands.managedidentities.credentials.UpdateCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsList;
import io.seqera.tower.model.CreateManagedCredentialsRequest;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.ListManagedCredentialsRespDto;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import io.seqera.tower.model.SSHSecurityKeys;
import io.seqera.tower.model.UpdateManagedCredentialsRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.List;

@Command(
        name = "credentials",
        description = "List the organization members' SSH credentials for a managed identity",
        subcommands = {
                AddCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
        }
)
public class CredentialsCmd extends AbstractManagedIdentitiesCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Mixin
    public ManagedIdentityRefOptions ref;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = orgId();
        ManagedIdentityDbDtoAbstractGridConfig identity = findManagedIdentity(orgId, ref);
        return new ManagedCredentialsList(organizationRef, identity.getName(), listManagedCredentials(orgId, identity.getId(), null));
    }

    public Long orgId() throws ApiException {
        return findOrganizationByRef(organizationRef).getOrgId();
    }

    public ManagedIdentityDbDtoAbstractGridConfig managedIdentity(Long orgId) throws ApiException {
        return findManagedIdentity(orgId, ref);
    }

    /**
     * Organization owners get a row for every member, with an empty managed credentials id when the member has
     * not added credentials yet. Other members only get their own row.
     */
    public ListManagedCredentialsRespDto memberRow(Long orgId, ManagedIdentityDbDtoAbstractGridConfig identity, String member) throws ApiException {
        return listManagedCredentials(orgId, identity.getId(), member).stream()
                .filter(it -> member.equals(it.getUserName()))
                .findFirst()
                .orElseThrow(() -> new TowerException(String.format("Member '%s' not found in the organization", member)));
    }

    public ListManagedCredentialsRespDto memberCredentials(Long orgId, ManagedIdentityDbDtoAbstractGridConfig identity, String member) throws ApiException {
        String userName = member != null ? member : userName();
        return listManagedCredentials(orgId, identity.getId(), userName).stream()
                .filter(it -> userName.equals(it.getUserName()) && it.getManagedCredentialsId() != null)
                .findFirst()
                .orElseThrow(() -> new TowerException(String.format("Member '%s' has no credentials for managed identity '%s'", userName, identity.getName())));
    }

    private List<ListManagedCredentialsRespDto> listManagedCredentials(Long orgId, Long identityId, String search) throws ApiException {
        return identitiesApi().listManagedCredentials(identityId, orgId, null, search, null, null).getManagedCredentials();
    }

    public static CreateManagedCredentialsRequest createRequest(SSHSecurityKeys keys, String linuxUserName) {
        return new CreateManagedCredentialsRequest()
                .provider(CreateManagedCredentialsRequest.ProviderEnum.SSH)
                .credentials(new Credentials().provider(Credentials.ProviderEnum.SSH).keys(keys))
                .metadata(new SshManagedCredentialsMetadata(linuxUserName));
    }

    public static UpdateManagedCredentialsRequest updateRequest(SSHSecurityKeys keys, String linuxUserName) {
        return new UpdateManagedCredentialsRequest()
                .provider(UpdateManagedCredentialsRequest.ProviderEnum.SSH)
                .credentials(new Credentials().provider(Credentials.ProviderEnum.SSH).keys(keys))
                .metadata(new SshManagedCredentialsMetadata(linuxUserName));
    }
}
