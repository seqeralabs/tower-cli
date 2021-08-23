package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.TowerApi;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.User;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class AbstractCmd implements Callable<Integer> {

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

    public AbstractCmd() {
    }

    public abstract Tower app();

    protected TowerApi api() {

        if (api == null) {
            ApiClient client = new ApiClient(buildOkHttpClient());
            client.setBasePath(app().url);
            client.setBearerToken(app().token);
            api = new TowerApi(client);
        }

        return api;
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (app().xRay) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(s -> app().printerr(s));
            logging.redactHeader("Authorization");
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        return builder.build();
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
                orgName = app().orgAndWorkspaceNames.orgName;
            }
        }
        return orgName;
    }

    protected String workspaceName() throws ApiException {
        if (workspaceName == null) {
            if (app().orgAndWorkspaceNames == null) {
                loadOrgAndWorkspaceFromIds();
            } else {
                workspaceName = app().orgAndWorkspaceNames.workspaceName;
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
        if (workspaceId() == null) {
            return "personal";
        }
        return String.format("[%s / %s]", orgName(), workspaceName());
    }

    protected void println(String line) {
        app().println(line);
    }

    protected void printerr(String line) {
        app().printerr(line);
    }

    @Override
    public Integer call() {
        try {
            return exec();
        } catch (NullPointerException e) {
            e.printStackTrace(app().spec.commandLine().getErr());
        } catch (NoSuchFileException e) {
            printerr(String.format("File not found. %s", e.getMessage()));
        } catch (IOException e) {
            printerr(String.format("IO error. %s", e.getMessage()));
        } catch (ApiException e) {
            printerr(String.format("[%d] %s", e.getCode(), e.getMessage()));
        }
        return -1;
    }

    protected Integer exec() throws ApiException, IOException {
        // if the command was invoked without subcommand, show the usage help
        app().spec.commandLine().usage(System.err);
        return -1;
    }

    ;
}
