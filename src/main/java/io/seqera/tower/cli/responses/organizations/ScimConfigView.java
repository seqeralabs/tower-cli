/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DescribeScimConfigResponse;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class ScimConfigView extends Response {

    public final String organizationName;
    public final DescribeScimConfigResponse scim;

    public ScimConfigView(String organizationName, DescribeScimConfigResponse scim) {
        this.organizationName = organizationName;
        this.scim = scim;
    }

    @Override
    public Object getJSON() {
        return scim;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold SCIM configuration for %s organization:|@%n", organizationName)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Endpoint URL", scim.getEndpointUrl());
        table.addRow("SSO active", String.valueOf(scim.getSsoActive()));
        table.addRow("Active token", String.valueOf(scim.getHasActiveToken()));
        table.addRow("Token", scim.getMaskedToken());
        table.addRow("Token created", formatDate(scim.getTokenCreatedAt()));
        table.addRow("Token last used", formatDate(scim.getTokenLastUsed()));
        table.addRow("Groups", scim.getGroupCount() == null ? null : scim.getGroupCount().toString());
        table.print();
        out.println("");
    }
}
