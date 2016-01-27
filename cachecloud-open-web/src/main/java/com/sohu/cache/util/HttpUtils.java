package com.sohu.cache.util;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http get and post request
 * User: lingguo
 * Date: 14-6-4
 * Time: 下午6:35
 */
public class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * get request
     * @param url
     * @return
     */
    public static String doGet(String url) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String result = "";
        try {
            RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(ConstUtils.HTTP_CONNECTION_TIMEOUT)
                    .setSocketTimeout(ConstUtils.HTTP_SOCKET_TIMEOUT).build();
            httpGet.setConfig(config);
            result = httpClient.execute(httpGet, responseHandler);
        } catch (ClientProtocolException ex) {
            logger.info("error", ex);
        } catch (IOException ex) {
            logger.info("error", ex);
        }
        return result;
    }

    /**
     * http post request
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static String doPost(String url, Map<String, Object> params, String encoding) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        if (params != null) {
            try {
                List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
                for (String key: params.keySet()) {
                    nameValuePairList.add(new BasicNameValuePair(key, (String)params.get(key)));
                }
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, encoding);
                httpPost.setEntity(formEntity);
                HttpResponse response = httpClient.execute(httpPost);
                return response.toString();
            } catch (UnsupportedEncodingException ex) {
                logger.info("error", ex);
            } catch (ClientProtocolException ex) {
                logger.info("error", ex);
            } catch (IOException ex) {
                logger.info("error", ex);
            }
        }
        return null;
    }
}
