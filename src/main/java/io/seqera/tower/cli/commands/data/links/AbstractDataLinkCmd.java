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

package io.seqera.tower.cli.commands.data.links;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.utils.ResponseHelper;

public class AbstractDataLinkCmd extends AbstractApiCmd {

    public boolean checkIfResultIncomplete(Long wspId, String credId, boolean wait) {
        DataLinksFetchStatus status = checkDataLinksFetchStatus(wspId, credId);
        if (wait && status == DataLinksFetchStatus.FETCHING) {
            waitForDoneStatus(wspId, credId);
        }

        return !wait && status == DataLinksFetchStatus.FETCHING;
    }

    void waitForDoneStatus(Long wspId, String credId) {
        try {
            ResponseHelper.waitStatus(
                    app().getOut(),
                    app().output != OutputType.json,
                    DataLinksFetchStatus.DONE,
                    DataLinksFetchStatus.values(),
                    () -> checkDataLinksFetchStatus(wspId, credId),
                    DataLinksFetchStatus.DONE, DataLinksFetchStatus.ERROR
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    DataLinksFetchStatus checkDataLinksFetchStatus(Long wspId, String credentialsId) {
        int status;
        try {
            status = api().listDataLinksWithHttpInfo(wspId, credentialsId, null, 1, 0, null).getStatusCode();
        } catch (ApiException e) {
            return DataLinksFetchStatus.ERROR;
        }
        switch (status) {
            case 200:
                return DataLinksFetchStatus.DONE;
            case 202:
                return DataLinksFetchStatus.FETCHING;
            default:
                return DataLinksFetchStatus.ERROR;
        }
    }

    public enum DataLinksFetchStatus {
        FETCHING, DONE, ERROR
    }

}
