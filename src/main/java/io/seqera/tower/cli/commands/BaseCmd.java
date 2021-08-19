package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.App;
import io.seqera.tower.api.TowerApi;
import io.seqera.tower.cli.AppConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class BaseCmd implements Callable<Integer> {

    private App app;

    private transient Long userId;
    private transient String userName;
    private transient Long orgId;
    private transient String orgName;
    private transient String workspaceName;
    private transient String serverUrl;

    private transient Map<String, String> availableComputeEnvsNameToId;
    private transient String primaryComputeEnvId;

    public BaseCmd(App app) {
        this.app = app;
    }

    protected TowerApi api() {
        return app.getApi();
    }

    protected Long workspaceId() {
        return app.getConfig().getWorkspaceId();
    }

    protected AppConfig config() {
        return app.getConfig();
    }

    protected Long userId() {
        if (userId == null) {
            loadUser();
        }
        return userId;
    }

    protected String userName() {
        if (userName == null) {
            loadUser();
        }
        return userName;
    }

    protected Long orgId() {
        if (orgId == null) {
            loadOrgAndWorkspace();
        }
        return orgId;
    }

    protected String orgName() {
        if (orgName == null) {
            loadOrgAndWorkspace();
        }
        return orgName;
    }

    protected String workspaceName() {
        if (workspaceName == null) {
            loadOrgAndWorkspace();
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
            serverUrl = config().getUrl().replaceFirst("api\\.", "" ).replaceFirst("/api", "");
        }
        return serverUrl;
    }

    private void loadUser() {
        try {
            User user = api().user().getUser();
            userName = user.getUserName();
            userId = user.getId();
        } catch (ApiException | NullPointerException e) {
            //TODO add logging
        }
    }

    private void loadOrgAndWorkspace() {
        try {
            for (OrgAndWorkspaceDbDto ow : api().listWorkspacesUser(userId()).getOrgsAndWorkspaces()) {
                if ((workspaceId() == null && ow.getWorkspaceId() == null) || (workspaceId() != null && workspaceId().equals(ow.getWorkspaceId()))) {
                    workspaceName = ow.getWorkspaceName();
                    orgId = ow.getOrgId();
                    orgName = ow.getOrgName();
                    return;
                }

            }
        } catch (ApiException | NullPointerException e) {
            //TODO add logging
        }
    }

    private void loadAvailableComputeEnvs() {
        try {
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
        } catch (NullPointerException | ApiException e) {
            //TODO add logging
        }
    }

    protected void println(String line) {
        app.println(line);
    }

    @Override
    public Integer call() {
        try {
            return exec();
        } catch (ApiException | IOException e) {
            println(e.getMessage());
            return -1;
        }
    }

    protected abstract Integer exec() throws ApiException, IOException;
}
