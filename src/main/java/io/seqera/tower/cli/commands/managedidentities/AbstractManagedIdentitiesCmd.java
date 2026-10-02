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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.CredentialsInUseException;
import io.seqera.tower.cli.exceptions.ManagedIdentityNotFoundException;
import io.seqera.tower.model.DeleteCredentialsConflictResponse;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import picocli.CommandLine.Command;

import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;

@Command
public abstract class AbstractManagedIdentitiesCmd extends AbstractApiCmd {

    protected static final String ORGANIZATION_DESCRIPTION = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.";

    protected List<ManagedIdentityDbDtoAbstractGridConfig> listManagedIdentities(Long orgId) throws ApiException {
        return identitiesApi().listManagedIdentities(orgId, null, null, null).getManagedIdentities();
    }

    // The describe endpoint response is not modelled correctly in the SDK (it wraps the identity in a
    // 'managedIdentity' field), so look the identity up in the organization list instead.
    protected ManagedIdentityDbDtoAbstractGridConfig findManagedIdentity(Long orgId, ManagedIdentityRefOptions ref) throws ApiException {
        return listManagedIdentities(orgId).stream()
                .filter(it -> ref.managedIdentity.id != null ? ref.managedIdentity.id.equals(it.getId()) : ref.managedIdentity.name.equals(it.getName()))
                .findFirst()
                .orElseThrow(() -> new ManagedIdentityNotFoundException(ref.ref(), orgId));
    }

    /**
     * Runs a managed identity or managed credentials deletion, turning the 409 the Platform returns when running
     * jobs use the credentials into a {@link CredentialsInUseException}.
     */
    protected static void deleteUnlessInUse(String ref, DeleteCall call) throws ApiException {
        try {
            call.run();
        } catch (ApiException e) {
            DeleteCredentialsConflictResponse conflict = decodeConflict(e);
            if (conflict != null) {
                throw new CredentialsInUseException(ref, conflict.getConflicts());
            }
            throw e;
        }
    }

    // The managed credentials 409 body has the same shape as the credentials one, whose conflict type
    // CredentialsInUseException reports.
    private static DeleteCredentialsConflictResponse decodeConflict(ApiException e) {
        if (e.getCode() != 409 || e.getResponseBody() == null) {
            return null;
        }
        try {
            DeleteCredentialsConflictResponse conflict = parseJson(e.getResponseBody(), DeleteCredentialsConflictResponse.class);
            return conflict.getConflicts() == null || conflict.getConflicts().isEmpty() ? null : conflict;
        } catch (JsonProcessingException ignored) {
            // Not a conflict body: let the original error through
            return null;
        }
    }

    @FunctionalInterface
    protected interface DeleteCall {
        void run() throws ApiException;
    }
}
