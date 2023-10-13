/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.add.AbstractAddCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.shared.ComputeEnvExportFormat;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ComputeEnvResponseDto;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "import",
        description = "Add a compute environment from file content."
)
public class ImportCmd extends AbstractAddCmd {
    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the compute env if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import.", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {

        ComputeEnvExportFormat ceData = ComputeEnvExportFormat.deserialize(FilesHelper.readString(fileName));

        ComputeEnv.PlatformEnum platform = ComputeEnv.PlatformEnum.fromValue(ceData.getConfig().getDiscriminator());

        Long wspId = workspaceId(workspace.workspace);

        if (overwrite) tryDeleteCE(name, wspId);

        // prefer specified user labels before imported ones
        if (labels != null && !labels.isEmpty()) {
            // handles 'labels' parameter, creates labels if they do not exist
            return addComputeEnv(platform, ceData.getConfig());
        }
        // use imported labels
        if (ceData.getLabels() != null) {
            List<Label> importLabels = ceData.getLabels().stream()
                    .map(dto -> new Label(dto.getName(), dto.getValue()))
                    .collect(Collectors.toList());
            // creates labels if they do not exist
            return addComputeEnvWithLabels(platform, ceData.getConfig(), importLabels);
        }
        // no labels
        return addComputeEnvWithLabels(platform, ceData.getConfig(), null);
    }

    @Override
    protected Platform getPlatform() {
        throw new UnsupportedOperationException("Unknown platform");
    }

    private void tryDeleteCE(String name, Long wspId) throws ApiException {
        try {
            ComputeEnvResponseDto ce = computeEnvByRef(wspId, name);
            api().deleteComputeEnv(ce.getId(), wspId);
        } catch (ComputeEnvNotFoundException ignored) {}
    }

}
