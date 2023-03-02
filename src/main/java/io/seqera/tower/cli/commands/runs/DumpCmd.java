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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunDump;
import io.seqera.tower.model.ServiceInfo;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import picocli.CommandLine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "dump",
        description = "Dump all logs and details of a run into a compressed tarball file for troubleshooting."
)
public class DumpCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "-id"}, description = "Pipeline run identifier.", required = true)
    public String id;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file to store the dump.", required = true)
    Path outputFile;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    private static JSON JSON = new JSON();

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (!outputFile.getFileName().toString().endsWith(".tar.xz")) {
            throw new TowerException("Only 'tar.xz' format is supported. Output file name should end with '.tar.xz'");
        }

        try {

            FileOutputStream fileOut = new FileOutputStream(outputFile.toFile());
            BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
            XZCompressorOutputStream xzOut = new XZCompressorOutputStream(buffOut);
            TarArchiveOutputStream out = new TarArchiveOutputStream(xzOut);

            addServiceInfo(out);

            out.close();

            return new RunDump(id, workspaceRef(wspId), outputFile);
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new RunNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }

    private void addServiceInfo(TarArchiveOutputStream out) throws ApiException, IOException {
        byte[] data = JSON.getContext(ServiceInfo.class).writerWithDefaultPrettyPrinter().writeValueAsBytes(api().info().getServiceInfo());
        TarArchiveEntry entry = new TarArchiveEntry("service-info.json");
        entry.setSize(data.length);
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
    }
}
