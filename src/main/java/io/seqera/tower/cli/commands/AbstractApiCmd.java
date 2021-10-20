package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.exceptions.NoComputeEnvironmentException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
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

    public abstract Tower app();

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

    protected Long workspaceId() throws ApiException {
        if (workspaceId == null) {
            if (app().workspaceId != null) {
                workspaceId = app().workspaceId;
            } else {
                if (app().orgAndWorkspaceNames != null) {
                    loadOrgAndWorkspaceFromNames();
                }
            }
        }
        return workspaceId;
    }

    protected Long orgId() throws ApiException {
        if (orgId == null) {
            if (app().workspaceId != null) {
                loadOrgAndWorkspaceFromIds();
            } else {
                loadOrgAndWorkspaceFromNames();
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

    protected String orgName() throws ApiException {
        if (orgName == null) {
            if (app().orgAndWorkspaceNames == null) {
                loadOrgAndWorkspaceFromIds();
            } else {
                orgName = app().orgAndWorkspaceNames.organization;
            }
        }
        return orgName;
    }

    protected String workspaceName() throws ApiException {
        if (workspaceName == null) {
            if (app().orgAndWorkspaceNames == null) {
                loadOrgAndWorkspaceFromIds();
            } else {
                workspaceName = app().orgAndWorkspaceNames.workspace;
            }
        }
        return workspaceName;
    }

    protected ComputeEnv computeEnvByName(String name) throws ApiException {
        if (availableComputeEnvsNameToId == null) {
            loadAvailableComputeEnvs();
        }

        if (availableComputeEnvsNameToId.containsKey(name)) {
            return api().describeComputeEnv(availableComputeEnvsNameToId.get(name), workspaceId()).getComputeEnv();
        }

        throw new TowerException(String.format("Compute environment '%s' is not available", name));
    }

    protected ComputeEnv primaryComputeEnv() throws ApiException {
        if (primaryComputeEnvId == null) {
            loadAvailableComputeEnvs();
        }

        if (primaryComputeEnvId == null) {
            throw new NoComputeEnvironmentException(workspaceRef());
        }

        return api().describeComputeEnv(primaryComputeEnvId, workspaceId()).getComputeEnv();
    }

    protected String serverUrl() {
        if (serverUrl == null) {
            serverUrl = app().url.replaceFirst("api\\.", "").replaceFirst("/api", "");
        }
        return serverUrl;
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

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            throw new OrganizationNotFoundException(organizationName);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElse(null);
    }

    private void loadUser() throws ApiException {
        User user = api().user().getUser();
        userName = user.getUserName();
        userId = user.getId();
    }

    private void loadOrgAndWorkspaceFromIds() throws ApiException {
        Long workspaceId = workspaceId();
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if ((workspaceId == null && ow.getWorkspaceId() == null) || (workspaceId != null && workspaceId().equals(ow.getWorkspaceId()))) {
                workspaceName = ow.getWorkspaceName();
                orgId = ow.getOrgId();
                orgName = ow.getOrgName();
                return;
            }
        }

        throw new WorkspaceNotFoundException(workspaceId);
    }

    private void loadOrgAndWorkspaceFromNames() throws ApiException {
        String wName = workspaceName();
        String oName = orgName();
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

    private void loadAvailableComputeEnvs() throws ApiException {
        availableComputeEnvsNameToId = new HashMap<>();
        for (ListComputeEnvsResponseEntry ce : api().listComputeEnvs("AVAILABLE", workspaceId()).getComputeEnvs()) {

            // Make the first compute environment the default if there is no primary set.
            if (primaryComputeEnvId == null) {
                primaryComputeEnvId = ce.getId();
            }

            if (ce.getPrimary() != null && ce.getPrimary()) {
                primaryComputeEnvId = ce.getId();
            }
            availableComputeEnvsNameToId.put(ce.getName(), ce.getId());
        }
    }

    protected String workspaceRef() throws ApiException {
        //TODO Use a WorkspaceRef class instead of this method?
        if (workspaceId() == null) {
            return USER_WORKSPACE_NAME;
        }
        return buildWorkspaceRef(orgName(), workspaceName());
    }

    @Override
    public Integer call() {
        try {
            Response response = exec();
            if (app().json) {
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
        throw new ShowUsageException();
    }

}
