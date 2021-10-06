package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionUpdate;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Action;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import io.seqera.tower.model.UpdateActionRequest;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update a Pipeline Action"
)
public class UpdateCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo actionInfo = actionByName(actionName);

        Action action = api().describeAction(actionInfo.getId(), workspaceId()).getAction();

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(opts.computeEnv) : primaryComputeEnv();

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : ce.getConfig() != null ? ce.getConfig().getWorkDir() : null;
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : ce.getConfig() != null ? ce.getConfig().getPreRunScript() : null;
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : ce.getConfig() != null ? ce.getConfig().getPostRunScript() : null;


        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.computeEnvId(ce.getId())
                .id(action.getLaunch().getId())
                .pipeline(actionInfo.getPipeline())
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

        UpdateActionRequest request = new UpdateActionRequest();
        request.setLaunch(workflowLaunchRequest);

        try {
            api().updateAction(action.getId(), request, workspaceId());
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to update action '%s' for workspace '%s'", actionName, workspaceRef()));
        }

        return new ActionUpdate(actionName, workspaceRef(), action.getId());
    }
}
