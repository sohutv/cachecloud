package com.sohu.tv.cc.client.spectator.util;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Utils {

    public static String getStackTrace(Throwable ex) {
        StringWriter buf = new StringWriter();
        ex.printStackTrace(new PrintWriter(buf));
        return buf.toString();
    }
}
