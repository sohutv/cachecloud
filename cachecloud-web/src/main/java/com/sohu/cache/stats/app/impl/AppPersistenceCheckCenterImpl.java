package com.sohu.cache.stats.app.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.constant.AppDescEnum;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.stats.app.AppPersistenceCheckCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: 应用持久化配置检查修正
 * @author zengyizhao
 * @version 1.0
 * @date 2022/11/3
 */
@Slf4j
@Service("appPersistenceCheckCenter")
public class AppPersistenceCheckCenterImpl implements AppPersistenceCheckCenter {

    @Autowired
    private AppService appService;
    @Autowired
    private EmailComponent emailComponent;

    @Override
    public void checkAndFixAppPersistence() {
        List<AppDesc> appDescList = appService.getAllAppDesc();
        List<AppDesc> onlineAppDescs = appDescList.stream()
                .filter(appDesc -> AppStatusEnum.STATUS_PUBLISHED.getStatus() == appDesc.getStatus()
                        && appDesc.getPersistenceType() != AppDescEnum.AppPersistenceType.GENERAL.getValue())
                .collect(Collectors.toList());
        onlineAppDescs.forEach(appDesc -> {
            String failMessage = appService.checkAppPersistenceConfigAndFix(appDesc.getAppId(), getCurAppPersistenceMaps().get(0).getPersistenceMap().get(Integer.valueOf(appDesc.getPersistenceType())), getCurAppPersistenceMaps().get(1).getPersistenceMap().get(Integer.valueOf(appDesc.getPersistenceType())));
            if(StringUtils.isNotEmpty(failMessage)){
                log.error("checkAndFixAppPersistence job fail: {}", failMessage);
                emailComponent.sendMailToAdmin("【CacheCloud】持久化报警", this.generateAlertInfo(appDesc, failMessage));
            }
        });
    }

    private String generateAlertInfo(AppDesc appDesc, String failMessage){
        return String.format("应用id：%s 应用名：%s 应用环境：%s 应用类型：%s 持久化类型：%s <br> 持久化检测报警：%s<br>请及时关注。",
                appDesc.getAppId(), appDesc.getName(),
                (appDesc.getIsTest() == 0 ? "测试" : "正式"),
                appDesc.getTypeDesc(),
                AppDescEnum.AppPersistenceType.getByType(appDesc.getPersistenceType()).getInfo(),
                failMessage);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class AppPersistenceTypeMap {

        private Boolean isMaster;

        private Map<Integer, Map<String, String>> persistenceMap;

    }

    public List<AppPersistenceTypeMap> getCurAppPersistenceMaps(){
        List<AppPersistenceTypeMap> appPersistenceMaps = new ArrayList<>();
        String configStr = ConstUtils.APP_PERSISTENCE_CONFIG_MAP;
        JSONArray objects = JSON.parseArray(configStr);
        JSONObject jsonObject = objects.getJSONObject(0);
        AppPersistenceTypeMap curPersistenceMap = getCurPersistenceMap(jsonObject);
        appPersistenceMaps.add(curPersistenceMap);
        jsonObject = objects.getJSONObject(1);
        curPersistenceMap = getCurPersistenceMap(jsonObject);
        if(curPersistenceMap.isMaster){
            appPersistenceMaps.add(0, curPersistenceMap);
        }else{
            appPersistenceMaps.add(curPersistenceMap);
        }
        return appPersistenceMaps;
    }

    private AppPersistenceTypeMap getCurPersistenceMap(JSONObject jsonObject){
        Map<Integer, Map<String, String>> persistenceMap = new HashMap<>();
        Set<String> keySet = jsonObject.keySet();
        boolean isMaster = jsonObject.getBoolean("isMaster");
        JSONObject persistenceJson = jsonObject.getJSONObject("persistenceMap");
        Set<String> persisTypeSet = persistenceJson.keySet();
        persisTypeSet.forEach(persisType -> {
                    Map<String, String> configMap = persistenceJson.getObject(persisType, Map.class);
                    persistenceMap.put(Integer.valueOf(persisType), configMap);
                }
        );
        return new AppPersistenceTypeMap(isMaster, persistenceMap);
    }

}
