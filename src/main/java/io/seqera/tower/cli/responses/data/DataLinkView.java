package io.seqera.tower.cli.responses.data;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataLinkDto;

import java.io.PrintWriter;
import java.util.Objects;

public class DataLinkView extends Response {

    public final DataLinkDto dataLink;

    public DataLinkView(DataLinkDto dataLink) {
        this.dataLink = dataLink;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Data link created:|@%n")));

        TableList table = new TableList(out, 5, "ID", "Provider", "Name", "Resource ref", "Region").sortBy(0);

        table.addRow(
                dataLink.getId(),
                Objects.requireNonNull(dataLink.getProvider()).toString(),
                dataLink.getName(),
                dataLink.getResourceRef(),
                dataLink.getRegion()
        );

        table.print();

        out.println("");
    }

}
