package com.sohu.tv.cc.client.spectator.json;

public class JSONUtils {
    public static String toJSONString(Object o) {
        JSONWriter writer = new JSONWriter();
        writer.writeObject(o);
        return writer.toString();
    }

    public static Object parse(String text) {
        JSONParser parser = new JSONParser(text);
        return parser.parse();
    }
}
