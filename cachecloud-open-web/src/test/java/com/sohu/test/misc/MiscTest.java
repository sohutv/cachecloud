package com.sohu.test.misc;

import com.sohu.test.SimpleBaseTest;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections4.MapUtils;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * User: lingguo
 * Date: 14-6-30
 */
public class MiscTest extends SimpleBaseTest {

    @Test
    public void testSplit() {
        String key = "diff.cmd_get";
        String[] array = key.split("\\.");
        for (String s: array) {
            logger.info("{}", s);
        }
    }


    @Test
    public void testConvert() {
        String value = "2.39%";
        double result = 0;
        try {
            result = Double.parseDouble(value.substring(0, value.length()));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("convert: {}", result);
    }

    @Test
    public void testCharacter() {
        String ch = Character.toString((char)2);
        logger.info("ch: {}",ch);
    }

    @Test
    public void testMaps() {
        Map<String, Long> map = new HashMap<String, Long>();
        map.put("first", 10L);
        map.put("second", 20L);
        map.put("third", null);
        logger.info("third from map: {}", map.get("third"));

        try {
            ImmutableMap<String, Long> readMap = ImmutableMap.copyOf(map);
            logger.info("third from readMap: {}", readMap.get("third"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("third from MapUtils: {}", MapUtils.getLong(map, "third", 1000L));
    }

    @Test
    public void testPwd() {
        String dir1 = System.getProperty("user.dir");
        String dir2 = Paths.get("").toAbsolutePath().toString();
        logger.info("dir1: {}, dir2: {}", dir1, dir2);
    }

    @Test
    public void deleteLocalFile() {
        URL url = ClassLoader.getSystemResource("script/cachecloud-init.sh");
        try {
            File file = new File(url.toURI());
            if (file.exists()) {
                logger.info("file is " + file.getAbsolutePath());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoggerError() {
        String invalidIntStr = "234t";
        try {
            Integer.parseInt(invalidIntStr);
        } catch (NumberFormatException e) {
            logger.error("str: {}", invalidIntStr, e);
        }
    }
}
