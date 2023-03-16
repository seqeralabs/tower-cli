package io.seqera.tower.cli.commands.runs;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.LabelsCreator;
import io.seqera.tower.model.AssociateWorkflowLabelsRequest;

public class RunsLabelsCreator extends LabelsCreator<AssociateWorkflowLabelsRequest,String> {

    public RunsLabelsCreator(DefaultApi api) {
        super(api, "run");
    }

    @Override
    protected AssociateWorkflowLabelsRequest getRequest(List<Long> labelsIds, String entityId) {
        return new AssociateWorkflowLabelsRequest().labelIds(labelsIds).workflowIds(List.of(entityId));
    }

    @Override
    protected void apply(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.applyLabelsToWorkflows(associateWorkflowLabelsRequest, wspId);
    }

    @Override
    protected void remove(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.removeLabelsFromWorkflows(associateWorkflowLabelsRequest, wspId);
    }

    @Override
    protected void append(AssociateWorkflowLabelsRequest associateWorkflowLabelsRequest, Long wspId) throws ApiException {
        api.addLabelsToWorkflows(associateWorkflowLabelsRequest, wspId);
    }
}
