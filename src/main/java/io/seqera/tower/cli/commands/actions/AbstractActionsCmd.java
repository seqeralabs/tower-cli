/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.model.ActionQueryAttribute;
import io.seqera.tower.model.DescribeActionResponse;
import io.seqera.tower.model.ListActionsResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public abstract class AbstractActionsCmd extends AbstractApiCmd {

    protected ListActionsResponseActionInfo actionByName(Long workspaceId, String actionName) throws ApiException {
        ListActionsResponse listActionResponse = actionsApi().listActions(workspaceId, NO_ACTION_ATTRIBUTES);

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

    protected DescribeActionResponse fetchDescribeActionResponse(ActionRefOptions actionRefOptions, Long wspId, ActionQueryAttribute... attributes) throws ApiException {
        DescribeActionResponse response;

        if (actionRefOptions.action.actionId != null) {
            response = actionsApi().describeAction(actionRefOptions.action.actionId, wspId, List.of(attributes));
        } else {
            ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(wspId, actionRefOptions.action.actionName);
            response = actionsApi().describeAction(listActionsResponseActionInfo.getId(), wspId, List.of(attributes));
        }

        return response;
    }

    protected void deleteActionByName(String actionName, Long wspId) throws ActionNotFoundException, ApiException {
        ListActionsResponseActionInfo info = actionByName(wspId, actionName);
        deleteActionById(info.getId(), wspId);
    }

    protected void deleteActionById(String actionId, Long wspId) throws ActionNotFoundException, ApiException {
        actionsApi().deleteAction(actionId, wspId);
    }

}
