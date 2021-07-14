package io.seqera.tower.cli;

import io.seqera.tower.model.Launch;
import io.seqera.tower.model.WorkflowLaunchRequest;

public class ModelHelper {

    public static WorkflowLaunchRequest createLaunchRequest(Launch launch) {
        return new WorkflowLaunchRequest()
                .computeEnvId(launch.getComputeEnv().getId())
                .pipeline(launch.getPipeline())
                .workDir(launch.getWorkDir())
                .revision(launch.getRevision())
                .sessionId(launch.getSessionId())
                .configProfiles(launch.getConfigProfiles())
                .configText(launch.getConfigText())
                .paramsText(launch.getParamsText())
                .preRunScript(launch.getPreRunScript())
                .postRunScript(launch.getPostRunScript())
                .mainScript(launch.getMainScript())
                .entryName(launch.getEntryName())
                .schemaName(launch.getSchemaName())
                .resume(launch.getResume())
                .pullLatest(launch.getPullLatest())
                .stubRun(launch.getStubRun())
                .dateCreated(launch.getDateCreated());
    }
}
