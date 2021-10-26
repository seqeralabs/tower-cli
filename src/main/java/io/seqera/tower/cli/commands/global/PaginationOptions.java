package io.seqera.tower.cli.commands.global;

import picocli.CommandLine;

public class PaginationOptions {

    public static final int MAX = 100;

    @CommandLine.ArgGroup
    public Pageable pageable;

    @CommandLine.ArgGroup
    public Sizeable sizeable;

    public static class Pageable {
        @CommandLine.Option(names = {"--page"}, description = "Page to display to display")
        public Integer page;

        @CommandLine.Option(names = {"--offset"}, description = "Rows record's offset (default is 0)")
        public Integer offset;
    }

    public static class Sizeable {
        @CommandLine.Option(names = {"--max"}, description = "Maximum number of records to display (default is 100)")
        public Integer max;

        @CommandLine.Option(names = {"--no-max"}, description = "Show all records")
        public Boolean noMax = false;
    }

    public static Integer getMax(PaginationOptions paginationOptions){
        Integer max = PaginationOptions.MAX;

        if (paginationOptions.sizeable != null) {
            max = paginationOptions.sizeable.noMax ? null : paginationOptions.sizeable.max != null ? paginationOptions.sizeable.max : max;
        }

        return max;
    }

    public static Integer getOffset(PaginationOptions paginationOptions, Integer max){
        Integer offset = 0;

        if (paginationOptions.pageable != null) {
            offset = max != null ? paginationOptions.pageable.offset : offset;

            if (max != null && paginationOptions.pageable.page != null) {
                offset = (paginationOptions.pageable.page - 1) * max;
            }
        }

        return offset;
    }

}
