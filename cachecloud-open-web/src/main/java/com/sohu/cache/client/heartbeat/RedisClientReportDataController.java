package com.sohu.cache.client.heartbeat;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;
import com.sohu.cache.client.service.ClientReportDataService;
import com.sohu.cache.constant.ClientStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.JsonUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.IpUtil;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * cachecloud客户端上报数据接口
 * 
 * @author leifu
 * @Date 2015年1月16日
 * @Time 下午2:10:25
 */
@Controller
@RequestMapping(value = "/cachecloud/client")
public class RedisClientReportDataController {
    private final Logger logger = LoggerFactory.getLogger(RedisClientReportDataController.class);

    @Resource(name = "appService")
    private AppService appService;

    @Resource(name = "clientReportDataService")
    private ClientReportDataService clientReportDataService;

    /**
     * 上报客户端上传数据
     * 
     * @param appId
     * @param model
     */
    @RequestMapping(value = "/reportData.json" , method = RequestMethod.POST)
    public void reportData(HttpServletRequest request, HttpServletResponse response, Model model) {
        
        // 1. 验证版本的正确性
        String clientVersion = request.getParameter(ClientReportConstant.CLIENT_VERSION);
        if (!checkClientVersion(clientVersion)) {
            return;
        }

        // 2. 验证json的正确性
        String json = request.getParameter(ClientReportConstant.JSON_PARAM);
        ClientReportBean clientReportBean = checkReportJson(json);
        if (clientReportBean == null) {
            logger.error("reportWrong json: {}", json);
            return;
        }
                
        // 3.使用访问web的ip作为客户端最终的ip
        String clientIp = IpUtil.getIpAddr(request);
        if(StringUtils.isNotBlank(clientIp)){
            clientReportBean.setClientIp(clientIp);
        }
        
    	// 4. 根据类型处理数据
        boolean result = clientReportDataService.deal(clientReportBean);
        if (!result) {
            logger.error("ClientReportDataService deal fail, clientReportBean is {}", clientReportBean);
        }

    }
    
    /**
     * 检验json正确性，返回JavaBean
     * 
     * @param json
     * @return
     */
    private ClientReportBean checkReportJson(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return JsonUtil.fromJson(json, ClientReportBean.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 检查客户端的版本
     * 
     * @param clientVersion
     * @return
     */
    private boolean checkClientVersion(String clientVersion) {
        if (StringUtils.isBlank(clientVersion)) {
            return false;
        }
        // 获取合格的客户端版本
        List<String> goodVersions = Lists.newArrayList(ConstUtils.GOOD_CLIENT_VERSIONS.split(ConstUtils.COMMA));
        List<String> warnVersions = Lists.newArrayList(ConstUtils.WARN_CLIENT_VERSIONS.split(ConstUtils.COMMA));

        // 错误版本
        if (goodVersions.contains(clientVersion) && warnVersions.contains(clientVersion)) {
            logger.error("status: {}, message: {}", ClientStatusEnum.ERROR.getStatus(),
                    "ERROR: client is TOO old or NOT recognized, please update NOW!");
            return false;
        }
        return true;
    }

}
