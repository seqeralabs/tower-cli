package io.seqera.tower.cli.utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

public class TableList {

    private static final String BLINE = "-";
    private static final String CROSSING = "+";
    private static final String VERTICAL_TSEP = "|";
    private static final String VERTICAL_BSEP = "|";

    private String[] descriptions;
    private ArrayList<String[]> table;
    private int[] tableSizes;
    private int rows;
    private Comparator<String[]> comparator;
    private int spacing;
    private EnumAlignment aligns[];
    private String prefix = "";

    private PrintWriter out;

    public TableList(PrintWriter out, int columns, String... descriptions) {
        this.out = out;
        this.rows = columns;
        this.descriptions = descriptions;
        this.table = new ArrayList<>();
        this.tableSizes = new int[columns];

        if (descriptions.length != 0) {
            this.updateSizes(descriptions);
        }
        this.spacing = 1;
        this.aligns = new EnumAlignment[columns];
        this.comparator = null;
        for (int i = 0; i < aligns.length; i++) {
            aligns[i] = EnumAlignment.LEFT;
        }
    }

    private void updateSizes(String[] elements) {
        for (int i = 0; i < tableSizes.length; i++) {
            if (elements[i] != null) {
                int j = tableSizes[i];
                j = Math.max(j, elements[i].length());
                tableSizes[i] = j;
            }
        }
    }

    public TableList compareWith(Comparator<String[]> c) {
        this.comparator = c;
        return this;
    }

    public TableList sortBy(int column) {
        return this.compareWith((o1, o2) -> o1[column].compareTo(o2[column]));
    }

    /**
     * Adds a row to the table with the specified elements.
     */

    public TableList addRow(String... elements) {
        table.add(elements);
        updateSizes(elements);
        return this;
    }

    public void print() {
        StringBuilder line = null;

        if (descriptions.length > 0) {
            // print header
            for (int i = 0; i < rows; i++) {
                if (line != null) {
                    line.append(VERTICAL_TSEP);
                } else {
                    line = new StringBuilder();
                }
                String part = descriptions[i];
                while (part.length() < tableSizes[i] + spacing) {
                    part += " ";
                }
                for (int j = 0; j < spacing; j++) {
                    line.append(" ");
                }
                line.append(part);
            }
            out.println(prefix + line.toString());
        }

        // print vertical seperator
        line = null;
        for (int i = 0; i < rows; i++) {
            if (line != null) {
                line.append(CROSSING);
            } else {
                line = new StringBuilder();
            }
            for (int j = 0; j < tableSizes[i] + 2 * spacing; j++) {
                line.append(BLINE);
            }
        }
        out.println(prefix + line.toString());

        line = null;
        ArrayList<String[]> localTable = table;

        if (localTable.isEmpty()) {
            String[] sa = new String[rows];
            localTable.add(sa);
        }

        localTable.forEach(arr -> {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = "";
                }
            }
        });

        if (comparator != null) {
            localTable.sort(comparator);
        }

        for (String[] strings : localTable) {
            for (int i = 0; i < rows; i++) {
                if (line != null) {
                    line.append(VERTICAL_BSEP);
                } else {
                    line = new StringBuilder();
                }
                String part = "";
                for (int j = 0; j < spacing; j++) {
                    part += " ";
                }
                if (strings[i] != null) {
                    switch (aligns[i]) {
                        case LEFT:
                            part += strings[i];
                            break;
                        case RIGHT:
                            for (int j = 0; j < tableSizes[i] - strings[i].length(); j++) {
                                part += " ";
                            }
                            part += strings[i];
                            break;
                        case CENTER:
                            for (int j = 0; j < (tableSizes[i] - strings[i].length()) / 2; j++) {
                                part += " ";
                            }
                            part += strings[i];
                            break;
                    }
                }
                while (part.length() < tableSizes[i] + spacing) {
                    part += " ";
                }
                for (int j = 0; j < spacing; j++) {
                    part += " ";
                }
                line.append(part);
            }
            out.println(prefix + line.toString());

            line = null;
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public enum EnumAlignment {
        LEFT, CENTER, RIGHT
    }

}
