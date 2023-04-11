/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

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
