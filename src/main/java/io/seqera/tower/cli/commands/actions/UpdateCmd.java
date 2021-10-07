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
import java.util.Objects;

@CommandLine.Command(
        name = "update",
        description = "Update a Pipeline Action"
)
public class UpdateCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Option(names = {"-s", "--status"}, description = "Action status (pause or active)")
    public String status;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo actionInfo = actionByName(actionName);

        Action action = api().describeAction(actionInfo.getId(), workspaceId()).getAction();

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(opts.computeEnv) : action.getLaunch().getComputeEnv();

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : action.getLaunch().getWorkDir();
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : action.getLaunch().getPreRunScript();
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : action.getLaunch().getPostRunScript();


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

        if (status != null) {
            if (Objects.equals(action.getStatus().toString().toLowerCase(), status)) {
                throw new TowerException(String.format("The action is already set to '%s'", status.toUpperCase()));
            }

            try {
                api().pauseAction(action.getId(), workspaceId(), null);
            } catch (Exception e) {
                throw new TowerException(String.format("An error has occur while setting the action '%s' to '%s'", actionName, status.toUpperCase()));
            }
        }

        return new ActionUpdate(actionName, workspaceRef(), action.getId());
    }
}
