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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.exceptions.NoComputeEnvironmentException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListComputeEnvsResponse;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.ErrorReporting.errorMessage;
import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;

public abstract class AbstractApiCmd extends AbstractCmd {

    public static final String USER_WORKSPACE_NAME = "user";

    private DefaultApi api;

    private Long userId;
    private String userName;
    private Long workspaceId;
    private Long orgId;
    private String orgName;
    private String workspaceName;
    private String serverUrl;

    private Map<String, String> availableComputeEnvsNameToId;
    private String primaryComputeEnvId;

    protected AbstractApiCmd() {
    }

    public static String buildWorkspaceRef(String orgName, String workspaceName) {
        return String.format("[%s / %s]", orgName, workspaceName);
    }

    private Tower app() {
        return (Tower) getSpec().root().userObject();
    }

    protected DefaultApi api() {

        if (api == null) {
            ApiClient client = buildApiClient();
            client.setServerIndex(null);
            client.setBasePath(app().url);
            client.setBearerToken(app().token);
            api = new DefaultApi(client);
        }

        return api;
    }

    private ApiClient buildApiClient() {
        return new ApiClient() {
            @Override
            protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
                if (app().verbose) {
                    clientConfig.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), java.util.logging.Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 1024 * 50 /* Log payloads up to 50K */));
                    clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY);
                }
            }
        };
    }

    protected Long orgId(Long workspaceId) throws ApiException {
        if (orgId == null) {
            if (workspaceId != null) {
                loadOrgAndWorkspaceFromIds(workspaceId);
            } else {
                loadOrgAndWorkspaceFromNames(workspaceId);
            }
        }
        return orgId;
    }

    protected Long userId() throws ApiException {
        if (userId == null) {
            loadUser();
        }
        return userId;
    }

    protected String userName() throws ApiException {
        if (userName == null) {
            loadUser();
        }
        return userName;
    }

    protected String orgName(Long workspaceId) throws ApiException {
        if (orgName == null) {
            loadOrgAndWorkspaceFromIds(workspaceId);
        }
        return orgName;
    }

    protected String workspaceName(Long workspaceId) throws ApiException {
        if (workspaceName == null) {
            loadOrgAndWorkspaceFromIds(workspaceId);
        }
        return workspaceName;
    }

    protected ComputeEnv computeEnvByName(Long workspaceId, String name) throws ApiException {
        if (availableComputeEnvsNameToId == null) {
            loadAvailableComputeEnvs(workspaceId);
        }

        if (availableComputeEnvsNameToId.containsKey(name)) {
            return api().describeComputeEnv(availableComputeEnvsNameToId.get(name), workspaceId).getComputeEnv();
        }

        throw new TowerException(String.format("Compute environment '%s' is not available", name));
    }

    protected ComputeEnv primaryComputeEnv(Long workspaceId) throws ApiException {
        if (primaryComputeEnvId == null) {
            loadAvailableComputeEnvs(workspaceId);
        }

        if (primaryComputeEnvId == null) {
            throw new NoComputeEnvironmentException(workspaceRef(workspaceId));
        }

        return api().describeComputeEnv(primaryComputeEnvId, workspaceId).getComputeEnv();
    }

    protected String serverUrl() {
        if (serverUrl == null) {
            serverUrl = app().url.replaceFirst("api\\.", "").replaceFirst("/api", "");
        }
        return serverUrl;
    }

    protected String apiUrl() {
        return app().url;
    }

    protected OrgAndWorkspaceDbDto findOrganizationByName(String organizationName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new OrganizationNotFoundException(organizationName);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), null) && Objects.equals(item.getOrgName(), organizationName)
                )
                .collect(Collectors.toList());

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElseThrow(() -> new OrganizationNotFoundException(organizationName));
    }

    protected ComputeEnv findComputeEnvironmentByName( Long workspaceId, String name) throws ApiException {
        ListComputeEnvsResponse listComputeEnvsResponse = api().listComputeEnvs(null, workspaceId);

        ListComputeEnvsResponseEntry listComputeEnvsResponseEntry =  listComputeEnvsResponse
                .getComputeEnvs()
                .stream()
                .filter(it -> Objects.equals(it.getName(), name))
                .findFirst()
                .orElseThrow(() -> new ComputeEnvNotFoundException(name, workspaceId));

        return api().describeComputeEnv(listComputeEnvsResponseEntry.getId(), workspaceId).getComputeEnv();
    }


    private void loadUser() throws ApiException {
        User user = api().user().getUser();
        userName = user.getUserName();
        userId = user.getId();
    }

    private void loadOrgAndWorkspaceFromIds(Long workspaceId) throws ApiException {
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if ((workspaceId == null && ow.getWorkspaceId() == null) || (workspaceId != null && workspaceId.equals(ow.getWorkspaceId()))) {
                workspaceName = ow.getWorkspaceName();
                orgId = ow.getOrgId();
                orgName = ow.getOrgName();
                return;
            }
        }

        throw new WorkspaceNotFoundException(workspaceId);
    }

    private void loadOrgAndWorkspaceFromNames(Long workspaceId) throws ApiException {
        String wName = workspaceName(workspaceId);
        String oName = orgName(workspaceId);
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if (wName.equalsIgnoreCase(ow.getWorkspaceName()) && oName.equalsIgnoreCase(ow.getOrgName())) {
                workspaceName = ow.getWorkspaceName();
                orgName = ow.getOrgName();
                workspaceId = ow.getWorkspaceId();
                orgId = ow.getOrgId();
                return;
            }
        }

        throw new WorkspaceNotFoundException(workspaceName, orgName);
    }

    private void loadAvailableComputeEnvs(Long workspaceId) throws ApiException {
        availableComputeEnvsNameToId = new HashMap<>();
        for (ListComputeEnvsResponseEntry ce : api().listComputeEnvs("AVAILABLE", workspaceId).getComputeEnvs()) {

            if (ce.getPrimary() != null && ce.getPrimary()) {
                primaryComputeEnvId = ce.getId();
            }
            availableComputeEnvsNameToId.put(ce.getName(), ce.getId());
        }
    }

    protected String workspaceRef(Long workspaceId) throws ApiException {
        //TODO Use a WorkspaceRef class instead of this method?
        if (workspaceId == null) {
            return USER_WORKSPACE_NAME;
        }
        return buildWorkspaceRef(orgName(workspaceId), workspaceName(workspaceId));
    }

    @Override
    public Integer call() {
        try {
            Response response = exec();
            if (app().output == OutputType.json) {
                app().getOut().println(prettyJson(response.getJSON()));
            } else {
                response.toString(app().getOut());
            }
            return response.getExitCode();
        } catch (Exception e) {
            errorMessage(app(), e);
        }

        return -1;
    }

    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec());
    }

}
