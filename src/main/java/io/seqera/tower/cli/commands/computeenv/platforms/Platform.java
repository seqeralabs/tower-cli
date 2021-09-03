package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.Credentials.ProviderEnum;

import java.io.IOException;

public interface Platform {

    PlatformEnum type();

    ProviderEnum credentialsType();

    ComputeConfig computeConfig() throws ApiException, IOException;
}
