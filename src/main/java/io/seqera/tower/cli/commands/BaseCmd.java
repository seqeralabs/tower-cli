package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.App;
import io.seqera.tower.api.TowerApi;

import java.util.concurrent.Callable;

public abstract class BaseCmd implements Callable<Integer> {

    private App app;

    public BaseCmd(App app) {
        this.app = app;
    }

    protected TowerApi api() {
        return app.getApi();
    }

    protected Long workspaceId() {
        return app.getConfig().getWorkspaceId();
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
