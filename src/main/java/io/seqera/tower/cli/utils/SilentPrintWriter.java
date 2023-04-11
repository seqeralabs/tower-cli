package io.seqera.tower.cli.utils;

import java.io.OutputStream;
import java.io.PrintWriter;

public class SilentPrintWriter extends PrintWriter {

    public SilentPrintWriter() {
        super(new NullOutputStream());
    }

    private static class NullOutputStream extends OutputStream {
        public void write(int b) {}
    }

    @Override
    public void write(int c) {}

    @Override
    public void write(char[] buf, int off, int len) {}

    @Override
    public void write(String s, int off, int len) {}
}
