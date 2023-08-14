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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AgentSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class TwAgentProvider extends AbstractProvider<AgentSecurityKeys> {

    @Option(names = {"--connection-id"}, description = "Connection identifier.", required = true)
    public String connectionId;

    @Option(names = {"--work-dir"}, description = "Default work directory", defaultValue = "$TW_AGENT_WORK")
    public String workDir;

    public TwAgentProvider() {
        super(ProviderEnum.TW_AGENT);
    }

    @Override
    public AgentSecurityKeys securityKeys() throws IOException {
        return new AgentSecurityKeys()
                .connectionId(connectionId)
                .workDir(workDir);
    }
}
