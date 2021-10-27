package io.seqera.tower.cli.commands.global;

import io.seqera.tower.cli.exceptions.TowerException;
import picocli.CommandLine;

public class PaginationOptions {

    public static final int MAX = 100;

    // TODO: The use of the validate option is a work around to an existing Picocli issue. Please refer to https://github.com/seqeralabs/tower-cli/pull/72#issuecomment-952588876
    @CommandLine.ArgGroup(validate = false)
    public Pageable pageable;

    // TODO: The use of the validate option is a work around to an existing Picocli issue. Please refer to https://github.com/seqeralabs/tower-cli/pull/72#issuecomment-952588876
    @CommandLine.ArgGroup(validate = false)
    public Sizeable sizeable;

    public static class Pageable {
        @CommandLine.Option(names = {"--page"}, description = "Page to display to display (default is 1)")
        public Integer page;

        @CommandLine.Option(names = {"--offset"}, description = "Rows record's offset (default is 0)")
        public Integer offset;
    }

    public static class Sizeable {
        @CommandLine.Option(names = {"--max"}, description = "Maximum number of records to display (default is " + MAX + ")")
        public Integer max;

        @CommandLine.Option(names = {"--no-max"}, description = "Show all records")
        public Boolean noMax;
    }

    public static Integer getMax(PaginationOptions paginationOptions) throws TowerException {
        Integer max = PaginationOptions.MAX;

        if (paginationOptions.sizeable != null) {
            if (paginationOptions.sizeable.noMax != null && paginationOptions.sizeable.max != null) {
                throw new TowerException("Please use either --no-max or --max as pagination size parameter");
            }

            max = paginationOptions.sizeable.noMax != null ? null : paginationOptions.sizeable.max != null ? paginationOptions.sizeable.max : max;
        }

        return max;
    }

    public static Integer getOffset(PaginationOptions paginationOptions, Integer max) throws TowerException {
        int offset = 0;

        if (paginationOptions.pageable != null) {

            if (paginationOptions.pageable.page != null && paginationOptions.pageable.offset != null) {
                throw new TowerException("Please use either --page or --offset as pagination parameter");
            }

            offset = max == null ? offset : paginationOptions.pageable.offset != null ? paginationOptions.pageable.offset : offset;

            if (offset < 0) {
                throw new TowerException("Record offset number must be a positive value.");
            }

            if (max != null && paginationOptions.pageable.page != null) {

                if (paginationOptions.pageable.page < 1) {
                    throw new TowerException("Page number must be greater than zero.");
                }

                offset = (paginationOptions.pageable.page - 1) * max;
            }
        }

        return offset;
    }

}
