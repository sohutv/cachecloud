package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.Inspector;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.vo.AppDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chenshi on 2017/9/11.
 */
public class AppHitPrecentInspector extends BaseAlertService implements Inspector {

    /**
     * app统计相关
     */
    private AppStatsCenter appStatsCenter;

    /**
     * 应用相关dao
     */
    private AppDao appDao;

    @Override
    public boolean inspect(Map<InspectParamEnum, Object> paramMap) {

        Long appId = MapUtils.getLong(paramMap, InspectParamEnum.SPLIT_KEY);
        List<AppDesc> appDescList = new ArrayList<AppDesc>();
        AppDesc app = appDao.getAppDescById(appId);
        if (app != null) {
            appDescList.add(app);
        }
        if (CollectionUtils.isEmpty(appDescList)) {
            logger.error("appList is empty, appId={}", appId);
            return true;
        }
        // 执行检查逻辑
        for (AppDesc appDesc : appDescList) {
            // 测试不检查
            if (appDesc.getIsTest() == 1) {
                continue;
            }
            long checkAppid = appDesc.getAppId();
            // 监控命中率阀值(阀值为0不监控)
            int hitprecent_alertValue = appDesc.getHitPrecentAlertValue();
            if (hitprecent_alertValue == 0) {
                //logger.error("ignore hitprcent monitor, appId={}", appId);
                return true;
            }

            AppDetailVO appDetailVO = appStatsCenter.getAppDetail(checkAppid);
            if (appDetailVO == null) {
                continue;
            }
            // 全局命中率
            double hitPercent = appDetailVO.getHitPercent();
            if (hitPercent < hitprecent_alertValue) {
                // 报警
                alertAppHitPrecnt(appDetailVO);
            }
        }
        return false;
    }

    /**
     * <p>
     * Description:命中率低于监控阀值
     * </p>
     *
     * @param appDetailVO 应用信息
     * @return void
     * @author chenshi
     * @version 1.0
     * @date 2017/9/11
     */
    private void alertAppHitPrecnt(final AppDetailVO appDetailVO) {
        AppDesc appDesc = appDetailVO.getAppDesc();
        String content = String.format("应用(%s)-应用平均命中率报警-当前命中率百分之%s-现已低于预设百分之%s-请及时关注",
                appDesc.getAppId(), appDetailVO.getHitPercent(), appDesc.getHitPrecentAlertValue());
        String title = "CacheCloud系统-应用平均命中率报警";
        emailComponent.sendMail(title, content, appDetailVO.getEmailList(),
                Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
        weChatComponent.sendWeChatToAll(title,content,appDetailVO.getWeChatList());
    }

    public void setAppStatsCenter(AppStatsCenter appStatsCenter) {
        this.appStatsCenter = appStatsCenter;
    }

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }
}
