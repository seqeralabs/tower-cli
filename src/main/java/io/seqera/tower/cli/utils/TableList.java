package io.seqera.tower.cli.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

public class TableList {

    private static final String[] BLINE = { "-", "\u2501" };
    private static final String[] CROSSING = { "+", "\u2548" };
    private static final String[] VERTICAL_TSEP = { "|", "\u2502" };
    private static final String[] VERTICAL_BSEP = { "|", "\u2503" };
    private static final String TLINE = "\u2500";
    private static final String CORNER_TL = "\u250c";
    private static final String CORNER_TR = "\u2510";
    private static final String CORNER_BL = "\u2517";
    private static final String CORNER_BR = "\u251b";
    private static final String CROSSING_L = "\u2522";
    private static final String CROSSING_R = "\u252a";
    private static final String CROSSING_T = "\u252c";
    private static final String CROSSING_B = "\u253b";

    private String[] descriptions;
    private ArrayList<String[]> table;
    private int[] tableSizes;
    private int rows;
    private int findex;
    private String filter;
    private boolean ucode;
    private Comparator<String[]> comparator;
    private int spacing;
    private EnumAlignment aligns[];
    private String prefix = "";

    private PrintWriter out;

    public TableList(PrintWriter out, String... descriptions) {
        this(out, descriptions.length, descriptions);
    }

    public TableList(PrintWriter out, int columns, String... descriptions) {
        if (descriptions.length != columns && descriptions.length != 0) {
            throw new IllegalArgumentException();
        }
        this.out = out;
        this.filter = null;
        this.rows = columns;
        this.descriptions = descriptions;
        this.table = new ArrayList<>();
        this.tableSizes = new int[columns];

        if (descriptions.length != 0) {
            this.updateSizes(descriptions);
        }
        this.ucode = false;
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

    public TableList align(int column, EnumAlignment align) {
        aligns[column] = align;
        return this;
    }

    public TableList withSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    /**
     * Adds a row to the table with the specified elements.
     */

    public TableList addRow(String... elements) {
        if (elements.length != rows) {
            throw new IllegalArgumentException();
        }
        table.add(elements);
        updateSizes(elements);
        return this;
    }

    public TableList filterBy(int par0, String pattern) {
        this.findex = par0;
        this.filter = pattern;
        return this;
    }

    public TableList withUnicode(boolean ucodeEnabled) {
        this.ucode = ucodeEnabled;
        return this;
    }

    public void print() {
        StringBuilder line = null;

        if (descriptions.length > 0) {
            if (ucode) {
                for (int i = 0; i < rows; i++) {
                    if (line != null) {
                        line.append(CROSSING_T);
                    } else {
                        line = new StringBuilder();
                        line.append(CORNER_TL);
                    }
                    for (int j = 0; j < tableSizes[i] + 2 * spacing; j++) {
                        line.append(TLINE);
                    }
                }
                line.append(CORNER_TR);
                out.println(prefix + line.toString());

                line = null;
            }

            // print header
            for (int i = 0; i < rows; i++) {
                if (line != null) {
                    line.append(gc(VERTICAL_TSEP));
                } else {
                    line = new StringBuilder();
                    if (ucode) {
                        line.append(gc(VERTICAL_TSEP));
                    }
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
            if (ucode) {
                line.append(gc(VERTICAL_TSEP));
            }
            out.println(prefix + line.toString());
        }

        // print vertical seperator
        line = null;
        for (int i = 0; i < rows; i++) {
            if (line != null) {
                line.append(gc(CROSSING));
            } else {
                line = new StringBuilder();
                if (ucode) {
                    line.append(CROSSING_L);
                }
            }
            for (int j = 0; j < tableSizes[i] + 2 * spacing; j++) {
                line.append(gc(BLINE));
            }
        }
        if (ucode) {
            line.append(CROSSING_R);
        }
        out.println(prefix + line.toString());

        line = null;
        ArrayList<String[]> localTable = table;

        if (filter != null) {
            Pattern p = Pattern.compile(filter);
            localTable.removeIf(arr -> {
                String s = arr[findex];
                return !p.matcher(s).matches();
            });
        }

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
                    line.append(gc(VERTICAL_BSEP));
                } else {
                    line = new StringBuilder();
                    if (ucode) {
                        line.append(gc(VERTICAL_BSEP));
                    }
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
            if (ucode) {
                line.append(gc(VERTICAL_BSEP));
            }
            out.println(prefix + line.toString());

            line = null;
        }

        if (ucode) {
            for (int i = 0; i < rows; i++) {
                if (line != null) {
                    line.append(CROSSING_B);
                } else {
                    line = new StringBuilder();
                    line.append(CORNER_BL);
                }
                for (int j = 0; j < tableSizes[i] + 2 * spacing; j++) {
                    line.append(gc(BLINE));
                }
            }
            line.append(CORNER_BR);
            out.println(prefix + line.toString());
        }

    }

    private String gc(String[] src) {
        return src[ucode ? 1 : 0];
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public static enum EnumAlignment {
        LEFT, CENTER, RIGHT
    }

}
