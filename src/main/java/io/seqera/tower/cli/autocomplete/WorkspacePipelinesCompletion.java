package io.seqera.tower.cli.autocomplete;

import io.seqera.tower.ApiClient;
import io.seqera.tower.ApiException;
import io.seqera.tower.api.TowerApi;
import io.seqera.tower.cli.AppConfig;
import io.seqera.tower.model.PipelineDbDto;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorkspacePipelinesCompletion implements Iterable<String> {

    private static List<String> EMPTY = new ArrayList<>();

    private AppConfig config;
    private TowerApi api;

    public WorkspacePipelinesCompletion() {
        //TODO use a singleton pattern to manage the config and the API

        // Initialize configuration
        config = new AppConfig();

        // Initialize API client
        ApiClient client = new ApiClient();
        client.setBasePath(config.getUrl());
        client.setBearerToken(config.getToken());
        api = new TowerApi(client);
    }


    @NotNull
    @Override
    public Iterator<String> iterator() {

        try {
            return api.pipelineWorkspaceList(config.getWorkspaceId(), 20, 0, null)
                    .getPipelines().stream()
                    .map(PipelineDbDto::getName)
                    .iterator();
        } catch (ApiException | NullPointerException e) {
            return EMPTY.iterator();
        }

    }
}
