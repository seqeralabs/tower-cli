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

package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.BitbucketProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "bitbucket",
        description = "Update Bitbucket workspace credentials"
)
public class UpdateBitbucketCmd extends AbstractUpdateCmd {

    @Mixin
    protected BitbucketProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
