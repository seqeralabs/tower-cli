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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.ComponentVersions;
import io.seqera.tower.cli.responses.ComponentVersions.ComponentVersion;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CatalogVersion;
import io.seqera.tower.model.CatalogVersionsResponse;
import io.seqera.tower.model.NextflowVersion;
import io.seqera.tower.model.NextflowVersionsResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Objects;

@Command(
        name = "versions",
        description = "List the Nextflow or Fusion versions available to launch with"
)
public class VersionsCmd extends AbstractApiCmd {

    private static final String NEXTFLOW = "nextflow";

    @Option(names = {"-c", "--component"}, description = "Component to list versions of: 'nextflow' or 'fusion'. Default: nextflow.", defaultValue = NEXTFLOW)
    public String component;

    @Option(names = {"--nextflow"}, description = "Only list versions compatible with these Nextflow versions. Comma-separated list.")
    public String nextflow;

    @Option(names = {"--fusion"}, description = "Only list versions compatible with these Fusion versions. Comma-separated list.")
    public String fusion;

    @Override
    protected Response exec() throws ApiException {
        NextflowVersionsResponse platformVersions = nextflowApi().nextflowVersions();

        if (!Boolean.TRUE.equals(platformVersions.getCatalogEnabled())) {
            // Without the compatibility catalog, Platform only knows its configured Nextflow versions.
            if (!NEXTFLOW.equals(component) || nextflow != null || fusion != null) {
                throw new TowerException("The component compatibility catalog is not enabled on this Platform: only Nextflow versions can be listed, without compatibility filters");
            }
            List<ComponentVersion> versions = Objects.requireNonNullElse(platformVersions.getNextflowVersions(), List.<NextflowVersion>of()).stream()
                    .map(v -> new ComponentVersion(v.getVersion(), Boolean.TRUE.equals(v.getDefault())))
                    .toList();
            return new ComponentVersions(component, versions);
        }

        // Constrain the catalog to this Platform release, as the launch form does.
        String platform = serviceInfoApi().info().getServiceInfo().getVersion();
        CatalogVersionsResponse catalog = compatibilityApi().catalogComponentVersions(component, platform, nextflow, fusion, null, null);
        List<ComponentVersion> versions = Objects.requireNonNullElse(catalog.getVersions(), List.<CatalogVersion>of()).stream()
                .map(v -> new ComponentVersion(v.getVersion(), v.getVersion().equals(catalog.getDefaultVersion())))
                .toList();
        return new ComponentVersions(component, versions);
    }
}
