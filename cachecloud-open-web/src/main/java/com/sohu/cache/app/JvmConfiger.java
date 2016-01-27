package com.sohu.cache.app;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by zhangyijun on 15/11/2.
 */
public class JvmConfiger {
    public final static ResourceBundle RESOURCE = ResourceBundle.getBundle("application");


    public static String getJvmConfig() {
        StringBuilder buffer = new StringBuilder();
        append(buffer, RESOURCE, "jvm.mem");
        append(buffer, RESOURCE, "jvm.log");
        append(buffer, RESOURCE, "jvm.gc");
        append(buffer, RESOURCE, "jvm.others");
        append(buffer, RESOURCE, "jvm.args");
        return buffer.toString();
    }

    public static String getRun() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("nohup java ");
        buffer.append(String.format("`java -jar %s -jvm`",getPackageName()));
        buffer.append(" -jar ");
        buffer.append(getPackageName());
        buffer.append(" > ");
        buffer.append("./console.log");
        buffer.append(" 2>&1 &");
        return buffer.toString();
    }

    private static String getPackageName(){
        return getAppConfig("project.name") + "." + getAppConfig("project.package");
    }

    private static StringBuilder append(StringBuilder buffer, ResourceBundle resource, String key) {
        if (resource.containsKey(key)) {
            String value = resource.getString(key);
            if (value.contains("hostname=%s")) {
                value = String.format(value, getLocalAddress());
            }
            return buffer.append(" " + value);
        }
        return buffer;
    }

    public static String getAppConfig(String key) {
        if (RESOURCE.containsKey(key)) {
            return RESOURCE.getString(key);
        }
        return null;
    }

    public static String getLocalAddress() {
        try {
            // Traversal Network interface to get the first non-loopback and non-private address
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList<String>();
            ArrayList<String> ipv6Result = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }

            // prefer ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }

                    return ip;
                }

                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            //If failed to find,fall back to localhost
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }
}
