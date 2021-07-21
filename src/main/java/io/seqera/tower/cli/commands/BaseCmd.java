package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.App;
import io.seqera.tower.api.TowerApi;
import io.seqera.tower.cli.AppConfig;
import io.seqera.tower.model.DescribeUserResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.User;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class BaseCmd implements Callable<Integer> {

    private App app;

    private transient Long userId;
    private transient String userName;
    private transient Long orgId;
    private transient String orgName;
    private transient String workspaceName;
    private transient String serverUrl;

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
            for (OrgAndWorkspaceDbDto ow : api().workspaceListByUser(userId()).getOrgsAndWorkspaces()) {
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

    protected void println(String line) {
        app.println(line);
    }

    @Override
    public Integer call() {
        try {
            return exec();
        } catch (ApiException e) {
            println(e.getMessage());
            return -1;
        }
    }

    protected abstract Integer exec() throws ApiException;
}
