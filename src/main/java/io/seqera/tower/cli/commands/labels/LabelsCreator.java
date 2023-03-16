package io.seqera.tower.cli.commands.labels;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.labels.ManageLabels;

public abstract class LabelsCreator<Request,EntityId> {

    protected final DefaultApi api;
    private final LabelsFinder finder;
    private final String type;

    public LabelsCreator(DefaultApi api, String type) {
        this.api = api;
        this.finder = new LabelsFinder(api);
        this.type = type;
    }

    protected abstract Request getRequest(List<Long> labelsIds, EntityId entityId);


    protected abstract void apply(Request request, Long wspId) throws ApiException;

    protected abstract void remove(Request request, Long wspId) throws ApiException;

    protected abstract void append(Request request, Long wspId) throws ApiException;


    public void execute(Long wspId, EntityId entityId, List<Label> labels) throws ApiException{
        if (labels == null || labels.isEmpty()) {
            return;
        }
        execute(wspId, entityId,labels, LabelsSubcmdOptions.Operation.set,false);
    }

    public ManageLabels execute(Long wspId, EntityId entityId, LabelsSubcmdOptions options) throws ApiException {
        return execute(wspId,entityId,options.getLabels(), options.getOperation(), options.getNoCreate());
    }
    public ManageLabels execute(Long wspId, EntityId entityId, List<Label> labels, LabelsSubcmdOptions.Operation operation, boolean noCreate)  throws ApiException {
        final List<Long> labelsIds = finder.findLabelsIds(wspId, labels,noCreate);
        final Request request = getRequest(labelsIds,entityId);
        switch (operation) {
            case set:
                apply(request, wspId);
                break;
            case append:
                append(request,wspId);
                break;
            case delete:
                remove(request, wspId);
                break;
        }

        return new ManageLabels(operation.prettyName,this.type,entityId.toString(),wspId);
    }

}
