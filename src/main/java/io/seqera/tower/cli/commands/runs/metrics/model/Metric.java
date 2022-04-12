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

package io.seqera.tower.cli.commands.runs.metrics.model;

import io.seqera.tower.model.ResourceData;

public class Metric {

    public final String description;
    public final ResourceData resourceData;

    public Metric(String description, ResourceData resourceData) {
        this.description = description;
        this.resourceData = resourceData;
    }

    public static Metric of(String description, ResourceData resourceData) {
        return new Metric(description, resourceData);
    }
}