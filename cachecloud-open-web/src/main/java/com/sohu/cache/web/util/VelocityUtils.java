/**
 * Copyright (c) 2013 Sohu. All Rights Reserved
 */
package com.sohu.cache.web.util;

import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDailyData;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.util.ConstUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;


public class VelocityUtils {

    public static String charset = "UTF-8";
    public static int lineBreakPos = -1;
    public static boolean munge = true;
    public static boolean verbose = false;
    public static boolean preserveAllSemiColons = false;
    public static boolean disableOptimizations = true;

    protected static final Logger logger = LoggerFactory.getLogger(VelocityUtils.class);

    /**
     * 邮件模板
     *
     * @param appDesc       应用信息
     * @param appAudit      处理信息
     * @param templatePath  模板路径
     * @param customCharset 编码
     */
    public synchronized static String createText(VelocityEngine engine, AppDesc appDesc, AppAudit appAudit, AppDailyData appDailyData, 
            List<InstanceAlertValueResult> instanceAlertValueResultList, String templatePath, String customCharset) {
        if (!StringUtils.isEmpty(customCharset)) {
            charset = customCharset;
        }
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", Thread.currentThread().getContextClassLoader().getResource("").getPath());
        p.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        p.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        p.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        Velocity.init(p);

        logger.info("velocity: init done.");
        VelocityContext context = new VelocityContext();
        context.put("appDesc", appDesc);
        context.put("appAudit", appAudit);
        context.put("appDailyData", appDailyData);
        context.put("instanceAlertValueResultList", instanceAlertValueResultList);
        context.put("numberTool", new NumberTool());
        context.put("ccDomain", ConstUtils.CC_DOMAIN);
        context.put("decimalFormat", new DecimalFormat("###,###"));
        context.put("StringUtils", StringUtils.class);
        FileOutputStream fos = null;
        StringWriter writer = null;
        try {
            Template template = engine.getTemplate(templatePath);
            writer = new StringWriter();
            template.merge(context, writer);
        } catch (ResourceNotFoundException ex) {
            logger.error("error: velocity vm resource not found.", ex);
        } catch (ParseErrorException ex) {
            logger.error("error: velocity parse vm file error.", ex);
        } catch (MethodInvocationException ex) {
            logger.error("error: velocity template merge.", ex);
        } catch (Exception ex) {
            logger.error("error", ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger.error("error: close writer", e);
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                logger.error("error: close output stream.", e);
            }
        }
        logger.info("velocity: create text done.");
        if (writer != null) {
            return writer.toString();
        }
        return null;
    }

    public static void main(String[] args) throws Exception{
        ClassPathResource resource = new ClassPathResource("classpath:templates/appAudit.vm");
        logger.info("VelocityUtils: {}",resource.getFile().getPath());
    }

}
