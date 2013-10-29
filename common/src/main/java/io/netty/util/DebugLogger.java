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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class DebugLogger {

    private SimpleDateFormat format = new SimpleDateFormat("MMdd-HH");

    boolean inited;

    public String logFileName;
    public String fileRealPath;

    public static String VMID;

    static {
        long curTim = System.currentTimeMillis();
        long val = curTim / 100;
        long val2 = val / 100000;
        long val3 = val2 * 100000;
        long v = val % val3;
        VMID = String.valueOf(v);
    }

    private static final Map<String, DebugLogger> loggers = new HashMap<String, DebugLogger>();

    public static DebugLogger getLogger(String fname) {
        DebugLogger logger = null;
        synchronized (loggers) {
            logger = loggers.get(fname);
            if (logger == null) {
                logger = new DebugLogger(fname + "-" + VMID);
                loggers.put(fname, logger);
            }
        }
        return logger;
    }

    private DebugLogger(String fname) {
        logFileName = fname + "-" + VMID;
        init();
    }

    private void init() {
        if (inited) {
            return;
        }
        String udir = System.getProperty("user.home");
        File baseFile = new File(udir);

        Date d = new Date();
        String today = format.format(d);
        String baseDir = "debug-log-" + today;

        File dirFile = new File(baseFile, baseDir);

        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        File logFile = new File(dirFile, logFileName);
        fileRealPath = logFile.getAbsolutePath();
        inited = true;
    }

    public void log(String rec) {
        log(rec, false, null);
    }

    public void log(String rec, boolean stack) {
        log(rec, stack, null);
    }

    public void log(String rec, boolean printStack,
            Throwable t) {
        ControlledPrintWriter writer = WriterUtil.getWriter(fileRealPath);
        if (writer != null) {
            writer.writeRecord(rec, printStack, t);
        } else {
            throw new IllegalStateException("Couldn't obtain a writer " + fileRealPath);
        }
    }

}
