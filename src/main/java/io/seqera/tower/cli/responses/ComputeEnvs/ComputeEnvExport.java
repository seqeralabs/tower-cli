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

package io.seqera.tower.cli.responses.ComputeEnvs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.CreateComputeEnvRequest;

public class ComputeEnvExport extends Response {

    public final CreateComputeEnvRequest request;
    public final String fileName;

    public ComputeEnvExport(CreateComputeEnvRequest request, String fileName) {
        this.request = request;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        String configOutput = "";

        try {
            configOutput = new JSON().getContext(CreateComputeEnvRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null) {
            FilesHelper.saveString(fileName, configOutput);
        }

        return configOutput;
    }
}
