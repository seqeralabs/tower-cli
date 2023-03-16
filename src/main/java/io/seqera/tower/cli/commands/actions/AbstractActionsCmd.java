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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.model.ActionQueryAttribute;
import io.seqera.tower.model.DescribeActionResponse;
import io.seqera.tower.model.ListActionsResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public abstract class AbstractActionsCmd extends AbstractApiCmd {

    protected ListActionsResponseActionInfo actionByName(Long workspaceId, String actionName) throws ApiException {
        ListActionsResponse listActionResponse = api().listActions(workspaceId, NO_ACTION_ATTRIBUTES);

        if (listActionResponse == null || listActionResponse.getActions() == null) {
            throw new ActionNotFoundException(workspaceRef(workspaceId));
        }

        List<ListActionsResponseActionInfo> listActionsResponseActionInfos = listActionResponse.getActions()
                .stream()
                .filter(item -> Objects.equals(item.getName(), actionName))
                .collect(Collectors.toList());

        if (listActionsResponseActionInfos.isEmpty()) {
            throw new ActionNotFoundException(actionName, workspaceRef(workspaceId));
        }

        return listActionsResponseActionInfos.stream().findFirst().orElse(null);
    }

    protected DescribeActionResponse fetchDescribeActionResponse(ActionRefOptions actionRefOptions, Long wspId, List<ActionQueryAttribute> actionQueryAttributes) throws ApiException {
        DescribeActionResponse response;

        if (actionRefOptions.action.actionId != null) {
            response = api().describeAction(actionRefOptions.action.actionId, wspId, actionQueryAttributes);
        } else {
            ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(wspId, actionRefOptions.action.actionName);
            response = api().describeAction(listActionsResponseActionInfo.getId(), wspId, actionQueryAttributes);
        }

        return response;
    }

}
