package com.sohu.cache.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.web.controller.AppController;

/**
 * 简单文件读取
 * @author leifu
 * @Date 2015年3月2日
 * @Time 下午2:12:15
 */
public class SimpleFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileUtil.class);

    /**
     * 从class环境读取文件成List<String>
     * @param fileName
     * @return
     */
    public static List<String> getListFromFile(String fileName, String encoding) {
        List<String> list = new ArrayList<String>();

        InputStream is = null;
        BufferedReader br = null;
        try {
            is = AppController.class.getClassLoader().getResourceAsStream(fileName);
            br = new BufferedReader(new InputStreamReader(is, encoding));
            String line = null;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return list;
    }
}
