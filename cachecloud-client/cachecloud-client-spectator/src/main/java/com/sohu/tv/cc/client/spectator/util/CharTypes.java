package com.sohu.tv.cc.client.spectator.util;


public class CharTypes {

    private static final boolean[] hexFlags = new boolean[256];
    private static final boolean[] firstIdentifierFlags;
    private static final String[] stringCache;
    private static final boolean[] identifierFlags;
    private static final boolean[] whitespaceFlags;

    public CharTypes() {
    }

    public static boolean isHex(char c) {
        return c < 256 && hexFlags[c];
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isFirstIdentifierChar(char c) {
        if (c <= firstIdentifierFlags.length) {
            return firstIdentifierFlags[c];
        } else {
            return c != 12288 && c != '，';
        }
    }

    public static boolean isIdentifierChar(char c) {
        if (c <= identifierFlags.length) {
            return identifierFlags[c];
        } else {
            return c != 12288 && c != '，';
        }
    }

    public static String valueOf(char ch) {
        return ch < stringCache.length ? stringCache[ch] : null;
    }

    public static boolean isWhitespace(char c) {
        return c <= whitespaceFlags.length && whitespaceFlags[c] || c == 12288;
    }

    static {
        char c;
        for(c = 0; c < hexFlags.length; ++c) {
            if (c >= 'A' && c <= 'F') {
                hexFlags[c] = true;
            } else if (c >= 'a' && c <= 'f') {
                hexFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                hexFlags[c] = true;
            }
        }

        firstIdentifierFlags = new boolean[256];

        for(c = 0; c < firstIdentifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                firstIdentifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                firstIdentifierFlags[c] = true;
            }
        }

        firstIdentifierFlags[96] = true;
        firstIdentifierFlags[95] = true;
        firstIdentifierFlags[36] = true;
        stringCache = new String[256];
        identifierFlags = new boolean[256];

        for(c = 0; c < identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                identifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                identifierFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                identifierFlags[c] = true;
            }
        }

        identifierFlags[95] = true;
        identifierFlags[36] = true;
        identifierFlags[35] = true;

        int i;
        for(i = 0; i < identifierFlags.length; ++i) {
            if (identifierFlags[i]) {
                char ch = (char)i;
                stringCache[i] = Character.toString(ch);
            }
        }

        whitespaceFlags = new boolean[256];

        for(i = 0; i <= 32; ++i) {
            whitespaceFlags[i] = true;
        }

        whitespaceFlags[26] = false;

        for(i = 127; i <= 160; ++i) {
            whitespaceFlags[i] = true;
        }

        whitespaceFlags[160] = true;
    }
}
