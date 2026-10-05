/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.commands.info;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.responses.ComponentVersions;
import io.seqera.tower.cli.responses.ComponentVersions.ComponentVersion;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.NextflowVersion;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;

@Command(
        name = "versions",
        description = "List the Nextflow versions available to launch with"
)
public class VersionsCmd extends AbstractApiCmd {

    private static final String NEXTFLOW = "nextflow";

    @Override
    protected Response exec() throws ApiException {
        List<ComponentVersion> versions = Objects.requireNonNullElse(nextflowApi().nextflowVersions().getNextflowVersions(), List.<NextflowVersion>of()).stream()
                .map(v -> new ComponentVersion(v.getVersion(), Boolean.TRUE.equals(v.getDefault())))
                .toList();
        return new ComponentVersions(NEXTFLOW, versions);
    }
}
