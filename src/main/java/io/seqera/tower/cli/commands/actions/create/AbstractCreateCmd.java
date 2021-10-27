package io.seqera.tower.cli.commands.actions.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionCreate;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateActionRequest;
import io.seqera.tower.model.CreateActionResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;

public abstract class AbstractCreateCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Option(names = {"--pipeline"}, description = "Pipeline to launch", required = true)
    public String pipeline;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(opts.computeEnv) : primaryComputeEnv();

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : ce.getConfig() != null ? ce.getConfig().getWorkDir() : null;
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : ce.getConfig() != null ? ce.getConfig().getPreRunScript() : null;
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : ce.getConfig() != null ? ce.getConfig().getPostRunScript() : null;


        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.computeEnvId(ce.getId())
                .pipeline(pipeline)
                .revision(opts.revision)
                .workDir(workDirValue)
                .configProfiles(opts.profiles)
                .paramsText(FilesHelper.readString(opts.params))

                // Advanced options
                .configText(FilesHelper.readString(opts.config))
                .preRunScript(preRunScriptValue)
                .postRunScript(postRunScriptValue)
                .pullLatest(opts.pullLatest)
                .stubRun(opts.stubRun)
                .mainScript(opts.mainScript)
                .entryName(opts.entryName);

        CreateActionRequest request = new CreateActionRequest();
        request.setName(actionName);
        request.setSource(getSource());
        request.setLaunch(workflowLaunchRequest);

        CreateActionResponse response;
        try {
            response = api().createAction(request, workspaceId());
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to create action for workspace '%s'", workspaceRef()));
        }

        return new ActionCreate(actionName, workspaceRef(), response.getActionId());
    }

    protected abstract ActionSource getSource();
}
