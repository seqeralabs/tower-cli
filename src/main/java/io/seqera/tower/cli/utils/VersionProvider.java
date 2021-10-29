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

package io.seqera.tower.cli.utils;

import picocli.CommandLine;

import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/META-INF/build-info.properties"));
        return new String[]{String.format("@|yellow Tower CLI version %s (build %s)|@", properties.get("version"), properties.get("commitId"))};
    }
}
