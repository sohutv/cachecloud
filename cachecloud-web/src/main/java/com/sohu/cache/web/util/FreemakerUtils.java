package com.sohu.cache.web.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * @Author: rucao
 * @Date: 2020/10/14 10:11
 */
public class FreemakerUtils {
    protected static final Logger logger = LoggerFactory.getLogger(FreemakerUtils.class);

    public synchronized static String createText(String templateName, Configuration configuration, Map<String, Object> model) {
        if (configuration != null) {
            try {
                Template template = configuration.getTemplate(templateName);
                model.put("ccDomain", IpUtil.domain);
                model.put("decimalFormat", new DecimalFormat("###,###"));
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                return html;
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return "";
    }
}
