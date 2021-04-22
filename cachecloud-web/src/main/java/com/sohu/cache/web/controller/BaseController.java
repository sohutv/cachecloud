package com.sohu.cache.web.controller;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.dao.AppAuditDao;
import com.sohu.cache.dao.AppAuditLogDao;
import com.sohu.cache.dao.AppImportDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.service.UserLoginStatusService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.AppEmailUtil;
import com.sohu.cache.web.util.DateUtil;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基类controller
 *
 * @author leifu
 * @Time 2014年10月16日
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected UserService userService;

    @Autowired
    protected AppService appService;

    @Autowired
    protected MachineCenter machineCenter;

    @Autowired
    protected UserLoginStatusService userLoginStatusService;

    @Autowired
    protected RedisCenter redisCenter;

    @Resource
    protected AppDeployCenter appDeployCenter;

    @Resource
    protected AppAuditDao appAuditDao;

    @Resource
    protected AppImportDao appImportDao;

    @Resource
    protected AppAuditLogDao appAuditLogDao;

    @Resource
    protected InstanceDao instanceDao;

    @Resource
    protected RedisDeployCenter redisDeployCenter;

    @Resource
    protected AppEmailUtil appEmailUtil;

    @Resource
    protected AsyncService asyncService;

    @Autowired
    protected AppStatsCenter appStatsCenter;

    @Autowired
    protected InstanceDeployCenter instanceDeployCenter;

    @Autowired
    AssistRedisService assistRedisService;

    @Resource
    protected RedisConfigTemplateService redisConfigTemplateService;

    @Resource
    protected ResourceService resourceService;

    public Boolean saveTempResource(String resourceId, String content) {
        try {
            assistRedisService.set(getResourceKey(resourceId), content);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String getResourceKey(String resourceId) {
        return String.format("resource_%s", resourceId);
    }

    public boolean clearTempResource(String resourceId) {
        try {
            assistRedisService.del(getResourceKey(resourceId));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getTempResource(String resourceId) {
        try {
            return assistRedisService.get(getResourceKey(resourceId));
        } catch (Exception e) {
            return null;
        }
    }

    protected TimeBetween getJsonTimeBetween(HttpServletRequest request) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate;
        if (StringUtils.isBlank(endDateParam)) {
            endDate = DateUtils.addDays(startDate, 1);
        } else {
            endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        }
        long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
        long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
        return new TimeBetween(beginTime, endTime, startDate, endDate);
    }

    protected TimeBetween getTimeBetween(HttpServletRequest request, Model model, String startDateAtr,
                                         String endDateAtr) throws ParseException {
        String startDateParam = request.getParameter(startDateAtr);
        String endDateParam = request.getParameter(endDateAtr);
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            startDate = new Date();
            endDate = DateUtils.addDays(startDate, 1);
        } else {
            endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
            startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        }
        Date yesterDay = DateUtils.addDays(startDate, -1);

        long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
        long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
        model.addAttribute(startDateAtr, startDateParam);
        model.addAttribute(endDateAtr, endDateParam);
        model.addAttribute("yesterDay", DateUtil.formatDate(yesterDay, "yyyy-MM-dd"));
        return new TimeBetween(beginTime, endTime, startDate, endDate);
    }

    /**
     * 返回用户基本信息
     *
     * @param request
     * @return
     */
    public AppUser getUserInfo(HttpServletRequest request) {
        String userName = userLoginStatusService.getUserNameFromLoginStatus(request);
        return userService.getByName(userName);
    }


    /**
     * 发送json消息
     *
     * @param response
     * @param message
     */
    public void sendMessage(HttpServletResponse response, String message) {
        response.reset();
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(message);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }

    }

    /**
     * @param response
     * @param result
     */
    protected void write(HttpServletResponse response, String result) {
        try {
            response.setContentType("text/javascript");
            response.getWriter().print(result);
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 查看用户对于app操作的权限
     *
     * @param request
     * @param appId
     * @return
     */
    protected boolean checkAppUserProvilege(HttpServletRequest request, long appId) {
        // 当前用户
        AppUser currentUser = getUserInfo(request);
        if (currentUser == null) {
            logger.error("currentUser is empty");
            return false;
        }

        if (AppUserTypeEnum.ADMIN_USER.value().equals(currentUser.getType())) {
            return true;
        }

        // 应用用户列表
        List<AppToUser> appToUsers = appService.getAppToUserList(appId);
        if (CollectionUtils.isEmpty(appToUsers)) {
            logger.error("appId {} userList is empty", appId);
            return false;
        }

        // 应用下用户id集合
        Set<Long> appUserIdSet = new HashSet<Long>();
        for (AppToUser appToUser : appToUsers) {
            appUserIdSet.add(appToUser.getUserId());
        }

        //最终判断
        if (!appUserIdSet.contains(currentUser.getId())) {
            logger.error("currentUser {} hasn't previlege in appId {}", currentUser.getId(), appId);
            return false;
        }
        return true;
    }

    /**
     * 实例统计信息
     *
     * @param appId
     * @param model
     */
    protected void fillAppInstanceStats(Long appId, Model model) {
        // 实例列表
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        model.addAttribute("instanceList", instanceList);
        Map<Integer, List<InstanceInfo>> instanceListMap = instanceGroupByMaster(instanceList);
        model.addAttribute("instanceListMap", instanceListMap);


        // 实例Map
        Map<Integer, InstanceInfo> instanceInfoMap = new HashMap<Integer, InstanceInfo>();
        for (InstanceInfo instanceInfo : instanceList) {
            instanceInfoMap.put(instanceInfo.getId(), instanceInfo);
        }
        model.addAttribute("instanceInfoMap", instanceInfoMap);

        // 实例统计
        List<InstanceStats> appInstanceStats = appService.getAppInstanceStats(appId);
        Map<String, InstanceStats> instanceStatsMap = new HashMap<String, InstanceStats>();
        for (InstanceStats instanceStats : appInstanceStats) {
            instanceStatsMap.put(instanceStats.getIp() + ":" + instanceStats.getPort(), instanceStats);
        }
        model.addAttribute("instanceStatsMap", instanceStatsMap);

        //slot分布
        Map<String, InstanceSlotModel> clusterSlotsMap = redisCenter.getClusterSlotsMap(appId);
        model.addAttribute("clusterSlotsMap", clusterSlotsMap);

        //机器列表
        long startTime = System.currentTimeMillis();
        List<MachineStats> machineList = machineCenter.getMachineStats(null, null, null, null, null, null, null);
        Map<String, MachineStats> machineMap = machineList.stream().collect(Collectors.toMap(MachineStats::getIp, machineStats -> machineStats));
        model.addAttribute("machineMap", machineMap);
        logger.info("getMachineStats cost: {}, appId: {}", System.currentTimeMillis() - startTime, appId);

        Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
        model.addAttribute("machineInstanceCountMap", machineInstanceCountMap);
    }

    private Map<Integer, List<InstanceInfo>> instanceGroupByMaster(List<InstanceInfo> instanceList) {
        Map<Integer, List<InstanceInfo>> resultMap = new HashMap<Integer, List<InstanceInfo>>();
        for (InstanceInfo info : instanceList) {
            String roleDesc = info.getRoleDesc();
            if (roleDesc != null && roleDesc.equals("master")) {
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, info.getId(), new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(info.getId(), list);
            } else if (roleDesc != null && roleDesc.equals("slave")) {
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, info.getMasterInstanceId(), new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(info.getMasterInstanceId(), list);
            } else if (roleDesc != null && roleDesc.equals("sentinel")) {
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, -2, new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(-2, list);
            } else {//offline
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, -1, new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(-1, list);
            }
        }
        return resultMap;
    }

    /**
     * 应用机器实例分布图
     *
     * @param appId
     * @param model
     */
    protected void fillAppMachineInstanceTopology(Long appId, Model model) {
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        int groupId = 1;
        // 1.分组，同一个主从在一组
        for (int i = 0; i < instanceList.size(); i++) {
            InstanceInfo instance = instanceList.get(i);
            // 有了groupId，不再设置
            if (instance.getGroupId() > 0) {
                continue;
            }
            if (instance.isOffline()) {
                continue;
            }
            for (int j = i + 1; j < instanceList.size(); j++) {
                InstanceInfo instanceCompare = instanceList.get(j);
                if (instanceCompare.isOffline()) {
                    continue;
                }
                // 寻找主从对应关系
                if (instanceCompare.getMasterInstanceId() == instance.getId()
                        || instance.getMasterInstanceId() == instanceCompare.getId()) {
                    instanceCompare.setGroupId(groupId);
                }
            }
            instance.setGroupId(groupId++);
        }

        // 2.机器下的实例列表
        Map<String, List<InstanceInfo>> machineInstanceMap = new HashMap<String, List<InstanceInfo>>();
        for (InstanceInfo instance : instanceList) {
            String ip = instance.getIp();
            if (machineInstanceMap.containsKey(ip)) {
                machineInstanceMap.get(ip).add(instance);
            } else {
                List<InstanceInfo> tempInstanceList = new ArrayList<InstanceInfo>();
                tempInstanceList.add(instance);
                machineInstanceMap.put(ip, tempInstanceList);
            }
        }

        model.addAttribute("machineInstanceMap", machineInstanceMap);
        model.addAttribute("instancePairCount", groupId - 1);
    }

    /**
     * markdown to html
     */
    public String markdown2html(String filename, String suffix) throws Exception {
        String templatePath = "static/" + filename + suffix;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (inputStream == null) {
            return null;
        }
        String markdown = new String(read(inputStream), Charset.forName("UTF-8"));
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Arrays.asList(new Extension[]{TablesExtension.create()}));
        Document document = Parser.builder(options).build().parse(markdown);
        String html = HtmlRenderer.builder(options).build().render(document);
        return html;
    }

    public byte[] read(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return bos.toByteArray();
    }
}
