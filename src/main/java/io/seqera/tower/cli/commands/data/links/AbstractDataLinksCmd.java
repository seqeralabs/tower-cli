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
import io.seqera.tower.model.DataLinkDto;

public class AbstractDataLinksCmd extends AbstractApiCmd {

    protected String getDataLinkId(DataLinkRefOptions dataLinkRefOptions, Long wspId) throws ApiException {
        return getDataLinkId(dataLinkRefOptions, wspId, null);
    }

    protected String getDataLinkId(DataLinkRefOptions dataLinkRefOptions, Long wspId, String credId) throws ApiException {
        // if DataLink ID is supplied - use that directly
        if (dataLinkRefOptions.dataLinkRef.dataLinkId != null) {
            return dataLinkRefOptions.dataLinkRef.dataLinkId;
        }
        return getDataLink(dataLinkRefOptions, wspId, credId).getId();
    }

    protected DataLinkDto getDataLink(DataLinkRefOptions dataLinkRefOptions, Long wspId, String credId) throws ApiException  {
        DataLinkService dataLinkService = new DataLinkService(dataLinksApi(), app());
        return dataLinkService.getDataLink(dataLinkRefOptions.dataLinkRef, wspId, credId);
    }
}
