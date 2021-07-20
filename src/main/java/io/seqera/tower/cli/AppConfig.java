package io.seqera.tower.cli;

import java.util.Map;

public class AppConfig {

    private String url;
    private String token;
    private Long workspaceId;

    public AppConfig() {
        Map<String, String> env = System.getenv();
        url = env.getOrDefault("TOWER_URL", "https://api.tower.nf");
        token = env.getOrDefault("TOWER_ACCESS_TOKEN", null);

        if (env.containsKey("TOWER_WORKSPACE_ID")) {
            workspaceId = Long.valueOf(env.get("TOWER_WORKSPACE_ID"));
        } else {
            workspaceId = null;
        }
    }

    public AppConfig(String url, String token, Long workspaceId) {
        this.url = url;
        this.token = token;
        this.workspaceId = workspaceId;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }
}