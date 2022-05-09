package com.sohu.cache.client.heartbeat;

import com.google.common.collect.Lists;
import com.sohu.cache.client.AppClientReportModel;
import com.sohu.cache.client.service.DealClientReportService;
import com.sohu.cache.constant.ClientStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.JsonUtil;
import com.sohu.cache.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Pattern;

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

    @Autowired
    private DealClientReportService dealClientReportService;

    /**
     * 上报客户端上传数据
     *
     * @param
     * @param model
     */
    @RequestMapping(value = "/reportData.json", method = RequestMethod.POST)
    public void reportData(HttpServletRequest request, HttpServletResponse response, Model model) {
        return;
    }

    @RequestMapping(value = "/v1/reportData/exception", method = RequestMethod.POST)
    public ResponseEntity<String> reportExceptionData(@RequestParam("clientVersion") String clientVersion,
                                                      @RequestParam("stats") String json) {
        return dealAppClientReportData(clientVersion, json);
    }

    @RequestMapping(value = "/v1/reportData/command", method = RequestMethod.POST)
    public ResponseEntity<String> reportCommandData(@RequestParam("clientVersion") String clientVersion,
                                                    @RequestParam("stats") String json) {
        return dealAppClientReportData(clientVersion, json);
    }


    /**
     * 统一统一上报数据
     *
     * @param clientVersion
     * @param json
     * @return
     */
    private ResponseEntity<String> dealAppClientReportData(String clientVersion, String json) {
        HttpStatus status = HttpStatus.CREATED;
        // 验证json的正确性
        AppClientReportModel appClientReportModel = checkAppClientReportJson(json);
        if (appClientReportModel == null) {
            logger.error("reportWrong message: {}", json);
            status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body("reportWrong message");
        } else if (clientIpFilter(appClientReportModel.getClientIp())) {
            logger.debug("discard report data, clientIp:{}", appClientReportModel.getClientIp());
            return ResponseEntity.status(status).body("success");
        }
        // 处理数据
        boolean result = dealClientReportService.deal(appClientReportModel);
        if (!result) {
            logger.error("appClientReportCommandService deal fail, appClientReportModel is {}", appClientReportModel);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body("message deal fail");
        }
        return ResponseEntity.status(status).body("success");
    }

    private boolean clientIpFilter(String clientIp) {
        if (StringUtil.isBlank(clientIp)) {
            return true;
        }
        //todo 可自行实现客户端ip过滤逻辑
        return false;
    }


    /**
     * 检验json正确性，返回AppClientReportModel
     *
     * @param json
     * @return
     */
    private AppClientReportModel checkAppClientReportJson(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return JsonUtil.fromJson(json, AppClientReportModel.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

}
