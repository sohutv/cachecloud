package com.sohu.cache.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.sohu.cache.constant.AppDescEnum;
import com.sohu.cache.constant.AppDescEnum.AppImportantLevel;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.util.ConstUtils;

/**
 * 应用的信息，包括分片、类型以及各分片的ip
 * <p/>
 * User: lingguo
 * Date: 14-5-29
 * Time: 下午9:42
 */
public class AppDesc implements Serializable {
    private static final long serialVersionUID = -3507970915810652761L;

    /**
     * 应用id
     */
    private long appId;
    
    /**
     * 应用秘钥
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 应用状态, 0未分配，1是申请了未审批，2是审批并发布, 3应用下线
     */
    private int status;

    /**
     * 应用描述
     */
    private String intro;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 申请通过的时间
     */
    private Date passedTime;

    /**
     * 类型
     */
    private int type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 负责人
     */
    private String officer;

    /**
     * 版本号
     */
    private int verId;

    /**
     * 是否测试：1是0否
     */
    private int isTest;

    /**
     * 是否有后端数据源: 1是0否
     */
    private int hasBackStore;

    /**
     * 是否需要持久化: 1是0否
     */
    private int needPersistence;

    /**
     * 预估qps
     */
    private int forecaseQps;

    /**
     * 是否需要热备: 1是0否
     */
    private int needHotBackUp;

    /**
     * 预估条目数
     */
    private int forecastObjNum;

    /**
     * 内存报警阀值
     */
    private int memAlertValue;
    
    /**
     * 客户端连接数报警阀值
     */
    private int clientConnAlertValue;
    
    /**
     * 客户端机器机房
     */
    private String clientMachineRoom;
    
    /**
     * 重要度，默认重要
     */
    private int importantLevel = AppImportantLevel.IMPORTANT.getValue();

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPassedTime() {
        return passedTime;
    }

    public void setPassedTime(Date passedTime) {
        this.passedTime = passedTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOfficer() {
        return officer;
    }

    public void setOfficer(String officer) {
        this.officer = officer;
    }

    public int getVerId() {
        return verId;
    }

    public void setVerId(int verId) {
        this.verId = verId;
    }

    public int getIsTest() {
        return isTest;
    }

    public void setIsTest(int isTest) {
        this.isTest = isTest;
    }

    public int getHasBackStore() {
        return hasBackStore;
    }

    public void setHasBackStore(int hasBackStore) {
        this.hasBackStore = hasBackStore;
    }

    public int getNeedPersistence() {
        return needPersistence;
    }

    public void setNeedPersistence(int needPersistence) {
        this.needPersistence = needPersistence;
    }


    public int getForecaseQps() {
        return forecaseQps;
    }

    public void setForecaseQps(int forecaseQps) {
        this.forecaseQps = forecaseQps;
    }

    public int getNeedHotBackUp() {
        return needHotBackUp;
    }

    public void setNeedHotBackUp(int needHotBackUp) {
        this.needHotBackUp = needHotBackUp;
    }

    public int getForecastObjNum() {
        return forecastObjNum;
    }

    public void setForecastObjNum(int forecastObjNum) {
        this.forecastObjNum = forecastObjNum;
    }

    public int getMemAlertValue() {
        return memAlertValue;
    }

    public void setMemAlertValue(int memAlertValue) {
        this.memAlertValue = memAlertValue;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public String getClientMachineRoom() {
        return clientMachineRoom;
    }

    public void setClientMachineRoom(String clientMachineRoom) {
        this.clientMachineRoom = clientMachineRoom;
    }
    
    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public int getClientConnAlertValue() {
        return clientConnAlertValue;
    }

    public void setClientConnAlertValue(int clientConnAlertValue) {
        this.clientConnAlertValue = clientConnAlertValue;
    }
    
    public int getImportantLevel() {
        return importantLevel;
    }

    public void setImportantLevel(int importantLevel) {
        this.importantLevel = importantLevel;
    }

    /**
     * 应用运行天数
     */
    public int getAppRunDays() {
        if (createTime == null) {
            return -1;
        }
        Date now = new Date();
        long diff = now.getTime() - createTime.getTime();
        return (int) (diff / TimeUnit.DAYS.toMillis(1));
    }

    public String getTypeDesc() {
        if (type <= 0) {
            return "";
        } else if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            return "redis-cluster";
        } else if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            return "redis-sentinel";
        } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            return "redis-standalone";
        }
        return "";
    }
    
    public String getCreateTimeFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(createTime != null){
            return sdf.format(createTime);
        }
        return "";
    }


    @Override
    public String toString() {
        return "AppDesc [appId=" + appId + ", appKey=" + appKey + ", name=" + name + ", userId=" + userId + ", status="
                + status + ", intro=" + intro + ", createTime=" + createTime + ", passedTime=" + passedTime + ", type="
                + type + ", typeDesc=" + typeDesc + ", officer=" + officer + ", verId=" + verId + ", isTest=" + isTest
                + ", hasBackStore=" + hasBackStore + ", needPersistence=" + needPersistence + ", forecaseQps="
                + forecaseQps + ", needHotBackUp=" + needHotBackUp + ", forecastObjNum=" + forecastObjNum
                + ", memAlertValue=" + memAlertValue + ", clientConnAlertValue=" + clientConnAlertValue
                + ", clientMachineRoom=" + clientMachineRoom + ", importantLevel=" + importantLevel + "]";
    }

    public String getStatusDesc() {
        AppStatusEnum appStatusEnum = AppStatusEnum.getByStatus(status);
        if (appStatusEnum != null) {
            return appStatusEnum.getInfo();
        }
        return "";
    }


    /**
     * 是否下线
     * @return
     */
    public boolean isOffline() {
        return status == AppStatusEnum.STATUS_OFFLINE.getStatus();
    }

    /**
     * 是否是测试
     * @return
     */
    public boolean isTest() {
        return isTest == AppDescEnum.AppTest.IS_TEST.getValue();
    }

    /**
     * 非常重要
     * @return
     */
    public boolean isVeryImportant() {
        return importantLevel == AppDescEnum.AppImportantLevel.VERY_IMPORTANT.getValue();
    }
    
    /**
     * 超级重要
     * @return
     */
    public boolean isSuperImportant() {
        return importantLevel == AppDescEnum.AppImportantLevel.SUPER_IMPORTANT.getValue();
    }

}
