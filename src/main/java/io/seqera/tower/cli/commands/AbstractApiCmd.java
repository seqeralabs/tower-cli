package io.seqera.tower.cli.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.api.TowerApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.exceptions.ApiExceptionMessage;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractApiCmd extends AbstractCmd {

    public static final String USER_WORKSPACE_NAME = "user";

    public TowerApi api;

    private transient Long userId;
    private transient String userName;
    private transient Long workspaceId;
    private transient Long orgId;
    private transient String orgName;
    private transient String workspaceName;
    private transient String serverUrl;

    private transient Map<String, String> availableComputeEnvsNameToId;
    private transient String primaryComputeEnvId;

    protected AbstractApiCmd() {
    }

    public abstract Tower app();

    protected TowerApi api() {

        if (api == null) {
            ApiClient client = buildApiClient();
            client.setServerIndex(null);
            client.setBasePath(app().url);
            client.setBearerToken(app().token);
            api = new TowerApi(client);
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

    protected Long orgId() throws ApiException {
        if (orgId == null) {
            loadOrgAndWorkspaceFromIds();
        }
        return orgId;
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

        throw new ApiException(String.format("Compute environment '%s' is not available", name));
    }

    protected ComputeEnv primaryComputeEnv() throws ApiException {
        if (primaryComputeEnvId == null) {
            loadAvailableComputeEnvs();
        }

        return api().describeComputeEnv(primaryComputeEnvId, workspaceId()).getComputeEnv();
    }

    protected String serverUrl() {
        if (serverUrl == null) {
            serverUrl = app().url.replaceFirst("api\\.", "").replaceFirst("/api", "");
        }
        return serverUrl;
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

        throw new ApiException(String.format("Workspace %d not found", workspaceId));
    }

    private void loadOrgAndWorkspaceFromNames() throws ApiException {
        String workspaceName = workspaceName();
        String orgName = orgName();
        for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
            if (workspaceName.equals(ow.getWorkspaceName()) && orgName.equals(ow.getOrgName())) {
                workspaceId = ow.getWorkspaceId();
                orgId = ow.getOrgId();
                return;
            }
        }

        throw new ApiException(String.format("Workspace '%s' at organization '%s' not found", workspaceName, orgName));
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
        return String.format("[%s / %s]", orgName(), workspaceName());
    }

    private void printerr(String line) {
        app().printerr(ansi(String.format("%n @|bold,red ERROR:|@ @|red %s|@%n", line)));
    }

    @Override
    public Integer call() {
        try {
            Response response = exec();
            if (app().json) {
                app().println(prettyJson(response.getBody()));
            } else {
                response.toString(app().getOut());
            }
            return 0;
        } catch (ShowUsageException e) {
            app().spec.commandLine().usage(System.err);
        } catch (TowerException e) {
            printerr(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace(app().spec.commandLine().getErr());
        } catch (NoSuchFileException e) {
            printerr(String.format("File not found. %s", e.getMessage()));
        } catch (IOException e) {
            printerr(String.format("IO error. %s", e.getMessage()));
        } catch (ApiException e) {
            switch (e.getCode()) {
                case 401:
                    printerr("Unauthorized. Check your access token, workspace id and tower server url.");
                    break;

                case 403:
                    printerr("Unknown entity. Check that the provided identifier is correct.");
                    break;

                default:
                    printerr(String.format("[%d] %s", e.getCode(), decodeMessage(e.getResponseBody())));
            }
        }

        return -1;
    }

    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException();
    }

    protected static String prettyJson(Object obj) throws JsonProcessingException {
        return new JSON().getContext(obj.getClass()).writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    protected static <T> T parseJson(String json, Class<T> clazz) throws JsonProcessingException {
        return new JSON().getContext(clazz).readValue(json, clazz);
    }

    protected String ansi(String value) {
        return CommandLine.Help.Ansi.AUTO.string(value);
    }

    private String decodeMessage(String body) {
        if (body == null) {
            return "";
        }

        try {
            ApiExceptionMessage message = parseJson(body, ApiExceptionMessage.class);
            return message.getMessage();
        } catch (JsonProcessingException e) {
            // On exception return as it is
        }

        return body;

    }

}
