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

package io.seqera.tower.cli.responses.datastudios;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataStudioTemplate;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DataStudiosTemplatesList extends Response {

    public List<DataStudioTemplate> templates;

    public DataStudiosTemplatesList(List<DataStudioTemplate> templates) {
        this.templates = templates;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Available templates for Data Studios:|@%n")));

        if (templates.isEmpty()) {
            out.println(ansi("    @|yellow No data studios templates found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("Templates"));
        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()])).sortBy(0);
        table.setPrefix("    ");

        templates.forEach(template -> {
            List<String> rows = new ArrayList<>(List.of(
                    template.getRepository() == null ? "" : template.getRepository()
            ));
            table.addRow(rows.toArray(new String[rows.size()]));

        });
        table.print();
        out.println("");
    }
}
