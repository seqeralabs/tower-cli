package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.App;
import io.seqera.tower.api.TowerApi;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class BaseCmd implements Callable<Integer> {

    private App app;

    public BaseCmd(App app) {
        this.app = app;
    }

    protected App getApp() {
        return app;
    }

    protected TowerApi api() {
        return app.getApi();
    }

    protected Long workspaceId() {
        return app.getConfig().getWorkspaceId();
    }

    protected void println(String line) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string(line));
    }

    @Override
    public Integer call() {
        try {
            return exec();
        } catch (ApiException e) {
            println(e.getResponseBody());
            e.printStackTrace();
            return -1;
        }
    }

    protected abstract Integer exec() throws ApiException;
}
