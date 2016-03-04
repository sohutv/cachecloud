package com.sohu.cache.web.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http原生工具类
 * 
 * @author leifu
 * @Date 2015年1月15日
 * @Time 上午9:42:47
 */
public final class HttpRequestUtil {
    
    // http 超时设置
    private static final int HTTP_CONNECTION_TIMEOUT = 3000;
    private static final int HTTP_SOCKET_TIMEOUT = 3000;
    
    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    public static String doPost(String reqUrl, Map<String, String> parameters) {
        return doPost(reqUrl, parameters, "UTF-8", HTTP_CONNECTION_TIMEOUT, HTTP_SOCKET_TIMEOUT);
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, String encoding) {
        return doPost(reqUrl, parameters, encoding, HTTP_CONNECTION_TIMEOUT, HTTP_SOCKET_TIMEOUT);
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, String encoding, int connectTimeout,
            int readTimeout) {
        HttpURLConnection urlConn = null;
        try {
            urlConn = sendPost(reqUrl, parameters, encoding, connectTimeout, readTimeout);
            String responseContent = getContent(urlConn, encoding);
            return responseContent.trim();
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
                urlConn = null;
            }
        }
    }

    private static HttpURLConnection sendPost(String reqUrl,
            Map<String, String> parameters, String encoding, int connectTimeout, int readTimeout) {
        HttpURLConnection urlConn = null;
        try {
            String params = generatorParamString(parameters, encoding);
            URL url = new URL(reqUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            // urlConn
            // .setRequestProperty(
            // "User-Agent",
            // "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3");
            urlConn.setConnectTimeout(connectTimeout);// （单位：毫秒）jdk
            urlConn.setReadTimeout(readTimeout);// （单位：毫秒）jdk 1.5换成这个,读操作超时
            urlConn.setDoOutput(true);
            // String按照字节处理是一个好方法
            byte[] b = params.getBytes(encoding);
            urlConn.getOutputStream().write(b, 0, b.length);
            urlConn.getOutputStream().flush();
            urlConn.getOutputStream().close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return urlConn;
    }

    private static String getContent(HttpURLConnection urlConn, String encoding) {
        try {
            String responseContent = null;
            InputStream in = urlConn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, encoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            String crlf = System.getProperty("line.separator");
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempStr.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
            return responseContent;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param link
     * @param encoding
     * @return
     */
    public static String doGet(String link, String encoding, int connectTimeout, int readTimeout) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            // conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedInputStream in = new BufferedInputStream(
                    conn.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int i = 0; (i = in.read(buf)) > 0;) {
                out.write(buf, 0, i);
            }
            out.flush();
            String s = new String(out.toByteArray(), encoding);
            return s;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
    }

    /**
     * UTF-8编码
     * 
     * @param link
     * @return
     */
    public static String doGet(String link) {
        return doGet(link, "UTF-8", HTTP_CONNECTION_TIMEOUT, HTTP_SOCKET_TIMEOUT);
    }

    /**
     * 将parameters中数据转换成用"&"链接的http请求参数形式
     * 
     * @param parameters
     * @return
     */
    private static String generatorParamString(Map<String, String> parameters, String encoding) {
        StringBuffer params = new StringBuffer();
        if (parameters != null) {
            for (Iterator<String> iter = parameters.keySet().iterator(); iter
                    .hasNext();) {
                String name = iter.next();
                String value = parameters.get(name);
                params.append(name + "=");
                try {
                    params.append(URLEncoder.encode(value, encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (Exception e) {
                    String message = String.format("'%s'='%s'", name, value);
                    throw new RuntimeException(message, e);
                }
                if (iter.hasNext())
                    params.append("&");
            }
        }
        return params.toString();
    }

}
