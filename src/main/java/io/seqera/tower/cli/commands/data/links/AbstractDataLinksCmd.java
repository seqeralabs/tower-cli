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
import io.seqera.tower.StringUtil;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.model.DataLinkCreateRequest;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataLinkProvider;
import io.seqera.tower.model.DataLinkType;

import java.util.Objects;

public class AbstractDataLinksCmd extends AbstractApiCmd {

    public static String buildSearch(String name, String providers, String region, String uri) {
        StringBuilder builder = new StringBuilder();
        if (name != null && !name.isBlank()) {
            builder.append(name);
        }
        if (providers != null && !providers.isBlank()) {
            appendParameter(builder, providers, "provider");
        }
        if (region != null && !region.isBlank()) {
            appendParameter(builder, region, "region");
        }
        if (uri != null && !uri.isBlank()) {
            appendParameter(builder, uri, "resourceRef");
        }
        if (builder.length() > 0)
            return builder.toString();
        return null;
    }

    private static void appendParameter(StringBuilder input, String param, String paramName) {
        if (param != null && !param.isBlank()) {
            if (input.length() > 0) {
                input.append(" ");
            }
            input.append(paramName).append(":").append(param);
        }
    }

    DataLinksFetchStatus checkDataLinksFetchStatus(Long wspId, String credentialsId) {
        int status = 0;
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

    DataLinkDto addDataLink(Long wspId, String name, String description, String url, String provider, String credsId) throws ApiException {
        DataLinkCreateRequest req = new DataLinkCreateRequest();
        req.name(name);
        req.description(description);
        req.resourceRef(url);
        req.type(DataLinkType.BUCKET);
        req.provider(DataLinkProvider.fromValue(provider.toLowerCase()));

        if (Objects.isNull(credsId)) {
            req.publicAccessible(true);
        } else {
            req.publicAccessible(false);
            req.credentialsId(credsId);
        }

        return api().createCustomDataLink(req, wspId);
    }

    public enum DataLinksFetchStatus {
        FETCHING, DONE, ERROR
    }
}
