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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.ActionsCmd;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.model.ListActionsResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public abstract class AbstractActionsCmd extends AbstractApiCmd {

    protected ListActionsResponseActionInfo actionByName(String actionName) throws ApiException {
        ListActionsResponse listActionResponse = api().listActions(workspaceId());

        if (listActionResponse == null || listActionResponse.getActions() == null) {
            throw new ActionNotFoundException(workspaceRef());
        }

        List<ListActionsResponseActionInfo> listActionsResponseActionInfos = listActionResponse.getActions()
                .stream()
                .filter(item -> Objects.equals(item.getName(), actionName))
                .collect(Collectors.toList());

        if (listActionsResponseActionInfos.isEmpty()) {
            throw new ActionNotFoundException(actionName, workspaceRef());
        }

        return listActionsResponseActionInfos.stream().findFirst().orElse(null);
    }
}
