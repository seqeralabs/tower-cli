package io.seqera.tower.cli.commands.pipelines.versions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.AbstractPipelinesCmd;
import io.seqera.tower.cli.commands.pipelines.PipelineRefOptions;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.versions.ListPipelineVersionsCmdResponse;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListPipelineVersionsResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command(
        name = "list",
        description = "List pipeline versions"
)
public class ListCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    WorkspaceOptionalOptions workspaceOptions;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only pipeline versions with name that contain the given word")
    public String filter;

    @CommandLine.Option(names = {"--is-published"}, description = "Show only published pipeline versions if true, draft versions only if false, all versions by default", required = false)
    Boolean isPublishedOption = null;

    @CommandLine.Option(names = {"--full-hash"}, description = "Show full-length hash values without truncation")
    public boolean showFullHash;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {

        Long wspId = workspaceId(workspaceOptions.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);

        if (pipeline == null) throwPipelineNotFoundException(pipelineRefOptions, wspId);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        // you can only filter by name versions with a name attached (published versions)
        if (filter != null) {
            isPublishedOption = true;
        }

        ListPipelineVersionsResponse response = pipelineVersionsApi().listPipelineVersions(
                pipeline.getPipelineId(),
                wspId,
                max, offset,
                filter,
                isPublishedOption
        );

        if (response.getVersions() == null) {
            throw new TowerException("No versions available for the pipeline, check if Pipeline versioning feature is enabled for the workspace");
        }

        List<PipelineVersionFullInfoDto> versions = response.getVersions().stream()
                .map(PipelineDbDto::getVersion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new ListPipelineVersionsCmdResponse(workspaceOptions.workspace, pipeline.getPipelineId(), pipeline.getName(), versions, PaginationInfo.from(offset, max), showFullHash);
    }

    private void throwPipelineNotFoundException(PipelineRefOptions pipelineRefOptions, Long wspId) throws ApiException, PipelineNotFoundException {
        if (pipelineRefOptions.pipeline.pipelineId != null) {
            throw new PipelineNotFoundException(pipelineRefOptions.pipeline.pipelineId, workspaceRef(wspId));
        }
        throw new PipelineNotFoundException(pipelineRefOptions.pipeline.pipelineName, workspaceRef(wspId));
    }
}
