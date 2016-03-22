package com.sohu.test.dao;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.dao.AppAuditDao;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

/**
 * Created by yijunzhang on 14-10-20.
 */
public class AppAuditDaoTest extends BaseTest{
    @Resource
    private AppAuditDao appAuditDao;

    @Resource
    private AppUserDao appUserDao;

    @Resource
    private AppDao appDao;

    @Test
    public void testInsert(){
        int memSize = 4;
        AppDesc appDesc = appDao.getAppDescById(10132L);
        AppUser appUser = appUserDao.get(10016L);
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setParam1(String.valueOf(memSize));
        appAudit.setParam2(appDesc.getTypeDesc());
        appAudit.setInfo("应用申请:类型:" + appDesc.getTypeDesc() + ";初始申请空间:" + memSize + ";");
        appAudit.setStatus(AppStatusEnum.STATUS_INITIALIZE.getStatus());
        appAudit.setType(AppAuditType.APP_AUDIT.getValue());
        appAuditDao.insertAppAudit(appAudit);

    }
    
    @Test
    public void testSelect(){
        List<AppAudit> audits = appAuditDao.selectWaitAppAudits(1, 1);
        logger.info("list={}", audits);
    }
    
    @Test
    public void testUpdateRefuseReason(){
        appAuditDao.updateRefuseReason(13, "呵呵!");
    }

    
    @Test
    public void testGetAppAuditByAppId(){
        Long appId = 10170L;
        List<AppAudit> appAudits = appAuditDao.getAppAuditByAppId(appId);
        for(AppAudit appAudit : appAudits){
            logger.info("{}", appAudit);
        }
    }
    
}
