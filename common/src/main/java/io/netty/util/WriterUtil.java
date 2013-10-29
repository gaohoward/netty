/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public final class WriterUtil {
    public static HashMap<String, ControlledPrintWriter> allWriters = new HashMap<String, ControlledPrintWriter>();

    private WriterUtil() {
    }

    public static synchronized ControlledPrintWriter getWriter(String path) {
        ControlledPrintWriter writer = allWriters.get(path);
        try {
            if (writer == null) {
                writer = new ControlledPrintWriter(path, true);
                allWriters.put(path, writer);
            }
        } catch (FileNotFoundException e) {
            writer = null;
        }
        return writer;
    }
}

class ControlledPrintWriter {
    private PrintWriter internalWriter;
    private String filePath;
    private boolean autoFlush;
    private int ln_counter;
    private byte f_counter;

    private static final int FILE_LINES = 800000;

    // private static final int FILE_LINES = 10;

    public ControlledPrintWriter(String path, boolean flush)
            throws FileNotFoundException {
        ln_counter = 0;
        f_counter = 0;
        filePath = path;
        autoFlush = flush;
        internalWriter = new PrintWriter(new FileOutputStream(filePath, true),
            autoFlush);
    }

    // I'm not thread safe, whoever uses me takes care of it.
    private void println(String message) {
        internalWriter.println(message);
        ln_counter++;
        if (ln_counter >= FILE_LINES) {
            internalWriter.close();
            // back up
            File oldFile = new File(filePath);
            File bkFile = new File(filePath + f_counter % 100);
            f_counter++;

            if (bkFile.exists()) {
                bkFile.delete();
            }
            oldFile.renameTo(bkFile);

            // startNew
            try {
                internalWriter = new PrintWriter(new FileOutputStream(filePath,
                    false), autoFlush);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ln_counter = 0;
        }
    }

    public void close() {
        internalWriter.close();
    }

    public synchronized void writeRecord(String rec, boolean printStack, Throwable t) {
        long tid = Thread.currentThread().getId();
        println(LoggingUtil.getCurrentTime() + "-[t" + tid + "t] " + rec);

        if (printStack) {
            println("----------------Thread Stack Trace------------------");
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            for (StackTraceElement e : traces) {
                println(e.toString());
            }
            println("----------------End Thread Stack Trace------------------");
        }

        if (t != null) {
            println("----------------Exception " + t + "------------------");
            StackTraceElement[] traces = t.getStackTrace();
            for (StackTraceElement e : traces) {
                println(e.toString());
            }
            println("----------------End Exception------------------");
        }
    }
}
