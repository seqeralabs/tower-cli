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

package io.seqera.tower.cli.shared;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.LabelDbDto;

import java.util.List;

/**
 * This is the class used by ExportCmd to structure CE data as JSON.
 * The class {@link ComputeConfig} does not include labels so this envelope
 * class is needed to export them along the rest of the data.
 */
public final class ComputeEnvExportFormat<T extends ComputeConfig> {

    @JsonUnwrapped
    private T config;

    private List<LabelDbDto> labels;

    public ComputeEnvExportFormat(final T config, final List<LabelDbDto> labels) {
        this.config = config;
        this.labels = labels;
    }

    public T getConfig() {
        return config;
    }

    public List<LabelDbDto> getLabels() {
        return labels;
    }
}
