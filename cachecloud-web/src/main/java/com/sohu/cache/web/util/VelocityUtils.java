/**
 * Copyright (c) 2013 Sohu. All Rights Reserved
 */
package com.sohu.cache.web.util;

import com.sohu.cache.entity.*;
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

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class VelocityUtils {

    public static String charset = "UTF-8";
    protected static final Logger logger = LoggerFactory.getLogger(VelocityUtils.class);

    /**
     * 邮件模板
     *
     * @param appDesc       应用信息
     * @param appAudit      处理信息
     * @param templatePath  模板路径
     * @param customCharset 编码
     */
    public synchronized static String createText(VelocityEngine engine,
                                                 AppDesc appDesc, AppAudit appAudit,
                                                 AppDailyData appDailyData,
                                                 List<InstanceAlertValueResult> instanceAlertValueResultList,
                                                 List<OperationAlertValueResult> operationAlertValueResultList,
                                                 List<Map> topologyExamResult,
                                                 String templatePath, String customCharset) {
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
        context.put("operationAlertValueResultList", operationAlertValueResultList);
        context.put("examResult", topologyExamResult);
        context.put("numberTool", new NumberTool());
        context.put("ccDomain", IpUtil.domain);
        context.put("decimalFormat", new DecimalFormat("###,###"));
        context.put("StringUtils", StringUtils.class);
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
        }
        logger.info("velocity: create text done.");
        if (writer != null) {
            return writer.toString();
        }
        return null;
    }

    public synchronized static String createExpAppsText(VelocityEngine engine,
                                                        String searchDate,
                                                        Map<Long, AppDesc> appDescMap,
                                                        Map<String, List<Map<String, Object>>> appClientGatherStatGroup,
                                                        String templatePath, String customCharset) {
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

        context.put("appDescMap", appDescMap);
        context.put("appClientGatherStatGroup", appClientGatherStatGroup);
        context.put("searchDate", searchDate);
        context.put("numberTool", new NumberTool());
        context.put("ccDomain", IpUtil.domain);
        context.put("decimalFormat", new DecimalFormat("###,###"));
        context.put("StringUtils", StringUtils.class);
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
        }
        logger.info("velocity: create text done.");
        if (writer != null) {
            return writer.toString();
        }
        return null;
    }

}
