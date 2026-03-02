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

package io.seqera.tower.cli.utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import static io.seqera.tower.cli.utils.FormatHelper.ansi;

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
                j = Math.max(j, stringLength(elements[i]));
                tableSizes[i] = j;
            }
        }
    }

    public TableList compareWith(Comparator<String[]> c) {
        this.comparator = c;
        return this;
    }

    public TableList sortBy(int column) {
        return this.compareWith(Comparator.comparing(o -> o[column]));
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
                String part = ansi(String.format("@|bold %s|@", descriptions[i]));
                while (stringLength(part) < tableSizes[i] + spacing) {
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
                            for (int j = 0; j < tableSizes[i] - stringLength(strings[i]); j++) {
                                part += " ";
                            }
                            part += strings[i];
                            break;
                        case CENTER:
                            for (int j = 0; j < (tableSizes[i] - stringLength(strings[i])) / 2; j++) {
                                part += " ";
                            }
                            part += strings[i];
                            break;
                    }
                }
                while (stringLength(part) < tableSizes[i] + spacing) {
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

    private static final Pattern ANSI_ESCAPE_LINK = Pattern.compile("\u001b\\]8;;[^\u001b]*\u001b\\\\"); // Remove links
    private static final Pattern ANSI_ESCAPE_COLS = Pattern.compile("\u001B\\[[;\\d]*m"); // Remove colors
    private int stringLength(String value) {
        return ANSI_ESCAPE_COLS.matcher(ANSI_ESCAPE_LINK.matcher(value).replaceAll("")).replaceAll("").length();
    }

}
