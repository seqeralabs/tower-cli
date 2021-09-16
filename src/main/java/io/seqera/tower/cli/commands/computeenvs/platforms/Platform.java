package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;

import java.io.IOException;

public interface Platform {

    PlatformEnum type();

    ComputeConfig computeConfig() throws ApiException, IOException;
}
