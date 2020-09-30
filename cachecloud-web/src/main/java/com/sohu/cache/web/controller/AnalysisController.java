package com.sohu.cache.web.controller;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.dao.AppAuditDao;
import com.sohu.cache.dao.InstanceBigKeyDao;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.ParamCount;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.task.constant.IdleTimeDistriEnum;
import com.sohu.cache.task.constant.TtlTimeDistriEnum;
import com.sohu.cache.task.constant.ValueSizeDistriEnum;
import com.sohu.cache.task.entity.InstanceBigKey;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.AppEmailUtil;
import net.sf.json.JSONArray;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Tuple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yijunzhang
 */
@Controller
public class AnalysisController extends BaseController {

    @Autowired
    private AssistRedisService assistRedisService;

    @Autowired
    private InstanceBigKeyDao instanceBigKeyDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AppAuditDao appAuditDao;

    @Autowired
    private AppEmailUtil appEmailUtil;

    /**
     * key分析
     */
    @RequestMapping("/admin/app/key")
    public ModelAndView appKey(Model model, Long appId) {
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        List<AppAudit> appAuditList = appService.getAppAudits(appId, AppAuditType.KEY_ANALYSIS.getValue());
        model.addAttribute("appAuditList", appAuditList);
        return new ModelAndView("analysis/appKey");
    }

    /**
     * 提交前置键值分析申请
     *
     * @param appId 应用id
     */
    @RequestMapping(value = "/admin/app/keyAnalysis")
    public ModelAndView submitKeyAnalysis(HttpServletRequest request,
                                          HttpServletResponse response, Long appId, String nodeInfo, String appAnalysisReason) {
        try {
            AppUser appUser = getUserInfo(request);
            AppDesc appDesc = appService.getByAppId(appId);
            AppAudit appAudit = appService.saveAppKeyAnalysis(appDesc, appUser, appAnalysisReason, nodeInfo);
            appEmailUtil.noticeAppResult(appDesc, appAudit);
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
        }
        return null;
    }

    /**
     * key分析结果
     */
    @RequestMapping("/admin/app/keyAnalysisResult")
    public ModelAndView keyAnalysisResult(Model model, Long appId, long auditId) {
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        //idle
        String idleKeyResultKey = ConstUtils.getRedisServerIdleKey(appId, auditId);
        Set<Tuple> idleKeyTuples = assistRedisService.zrangeWithScores(idleKeyResultKey, 0, -1);
        List<ParamCount> idleKeyParamCountList = new ArrayList<ParamCount>();
        for (Tuple tuple : idleKeyTuples) {
            String element = tuple.getElement();
            IdleTimeDistriEnum idleTimeDistriEnum = IdleTimeDistriEnum.getByValue(element);
            ParamCount paramCount = new ParamCount(idleTimeDistriEnum.getInfo(), tuple.getScore(), "");
            idleKeyParamCountList.add(paramCount);
        }
        model.addAttribute("idleKeyParamCountList", idleKeyParamCountList);
        model.addAttribute("idleKeyDistri", JSONArray.fromObject(idleKeyParamCountList));

        //type
        String keyTypeResultKey = ConstUtils.getRedisServerTypeKey(appId, auditId);
        Set<Tuple> keyTypeTuples = assistRedisService.zrangeWithScores(keyTypeResultKey, 0, -1);
        List<ParamCount> keyTypeParamCountList = new ArrayList<ParamCount>();
        for (Tuple tuple : keyTypeTuples) {
            ParamCount paramCount = new ParamCount(tuple.getElement(), tuple.getScore(), "");
            keyTypeParamCountList.add(paramCount);
        }
        model.addAttribute("keyTypeParamCountList", keyTypeParamCountList);
        model.addAttribute("keyTypeDistri", JSONArray.fromObject(keyTypeParamCountList));

        //ttl
        String keyTtlResultKey = ConstUtils.getRedisServerTtlKey(appId, auditId);
        Set<Tuple> keyTtlTuples = assistRedisService.zrangeWithScores(keyTtlResultKey, 0, -1);
        List<ParamCount> keyTtlParamCountList = new ArrayList<ParamCount>();
        for (Tuple tuple : keyTtlTuples) {
            String element = tuple.getElement();
            TtlTimeDistriEnum ttlTimeDistriEnum = TtlTimeDistriEnum.getByValue(element);
            ParamCount paramCount = new ParamCount(ttlTimeDistriEnum.getInfo(), tuple.getScore(), "");
            keyTtlParamCountList.add(paramCount);
        }
        model.addAttribute("keyTtlParamCountList", keyTtlParamCountList);
        model.addAttribute("keyTtlDistri", JSONArray.fromObject(keyTtlParamCountList));

        //key value size
        String keyValueSizeResultKey = ConstUtils.getRedisServerValueSizeKey(appId, auditId);
        Set<Tuple> keyValueSizeTuples = assistRedisService.zrangeWithScores(keyValueSizeResultKey, 0, -1);
        List<ParamCount> keyValueSizeParamCountList = new ArrayList<ParamCount>();
        for (Tuple tuple : keyValueSizeTuples) {
            String element = tuple.getElement();
            ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getByValue(element);
            ParamCount paramCount = new ParamCount(valueSizeDistriEnum.getInfo(), tuple.getScore(), "");
            keyValueSizeParamCountList.add(paramCount);
        }
        model.addAttribute("keyValueSizeParamCountList", keyValueSizeParamCountList);
        model.addAttribute("keyValueSizeDistri", JSONArray.fromObject(keyValueSizeParamCountList));

        //big
        List<InstanceBigKey> instanceBigKeyList = instanceBigKeyDao.getAppBigKeyList(appId, auditId, null);
        model.addAttribute("instanceBigKeyCount", instanceBigKeyList.size());
        instanceBigKeyList = instanceBigKeyList.subList(0, Math.min(instanceBigKeyList.size(), 100));
        model.addAttribute("instanceBigKeyList", instanceBigKeyList);

        return new ModelAndView("analysis/keyAnalysis");
    }

    /**
     * 审批执行键值分析
     */
    @RequestMapping(value = "/manage/app/startKeyAnalysis")
    public ModelAndView startKeyAnalysis(HttpServletRequest request) {
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        long auditId = NumberUtils.toLong(request.getParameter("appAuditId"));
        appAuditDao.updateAppAuditOperateUser(auditId, getUserInfo(request).getId());
        long taskId = taskService.addAppKeyAnalysisTask(appId, auditId, 0);
        return new ModelAndView("redirect:/manage/task/flow?taskId=" + taskId);
    }

}
