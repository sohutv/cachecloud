package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.ScanCleanConstants;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: zengyizhao
 * @Date: 2022/5/25
 */
@Component("InstanceScanCleanKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceScanCleanKeyTask extends BaseTask {

    private long appId;

    private long auditId;

    private long parentTaskId;

    private String host;

    private int port;

    private String operateType;

    private String pattern;

    private int perCount;

    private Long ttlLess;

    private Long ttlMore;

    private Integer ttlResetMore;

    private Integer ttlResetLess;

    private Integer index;

    private Integer compareType;

    private Long compareValue;

    private String authPwd;

    private String hostPort;

    private String condition;

    private Random random;

    private Pattern patternExp = Pattern.compile("[0-9]+");

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);

        //检查匹配分析
        if(ScanCleanConstants.OPERATE_ANALYSE.equals(paramMap.get(ScanCleanConstants.OPERATE_TYPE))){
            taskStepList.add("analyseKeyMatch");
        }

        //分析清理
        if(ScanCleanConstants.OPERATE_CLEAN.equals(paramMap.get(ScanCleanConstants.OPERATE_TYPE))){
            taskStepList.add("scanClean");
        }

        //分析重置ttl
        if(ScanCleanConstants.OPERATE_TTL_RESET.equals(paramMap.get(ScanCleanConstants.OPERATE_TYPE))){
            taskStepList.add("scanResetTtl");
        }
        return taskStepList;
    }

    /**
     * 1.初始化参数
     */
    @Override
    public TaskFlowStatusEnum init() {
        super.init();
        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskFlowStatusEnum.ABORT;
        }

        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY);
        if (auditId <= 0) {
            logger.error(marker, "task {} auditId {} is wrong", taskId, auditId);
            return TaskFlowStatusEnum.ABORT;
        }

        host = MapUtils.getString(paramMap, TaskConstants.HOST_KEY);
        if (StringUtils.isBlank(host)) {
            logger.error(marker, "task {} host is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }

        port = MapUtils.getIntValue(paramMap, TaskConstants.PORT_KEY);
        if (port <= 0) {
            logger.error(marker, "task {} port {} is wrong", taskId, port);
            return TaskFlowStatusEnum.ABORT;
        }

        operateType = MapUtils.getString(paramMap, ScanCleanConstants.OPERATE_TYPE);
        if (!(ScanCleanConstants.OPERATE_ANALYSE.equals(operateType)
                || ScanCleanConstants.OPERATE_CLEAN.equals(operateType)
                || ScanCleanConstants.OPERATE_TTL_RESET.equals(operateType))) {
            logger.error(marker, "task {} isClean {} is wrong", taskId, operateType);
            return TaskFlowStatusEnum.ABORT;
        }

        pattern = MapUtils.getString(paramMap, ScanCleanConstants.PATTERN);
        if (StringUtils.isBlank(pattern)) {
            logger.error(marker, "task {} pattern is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        this.compilePatternAndSet();
        AppDesc appDesc = appService.getByAppId(appId);
        authPwd = appDesc.getAuthPassword();

        ttlLess = MapUtils.getLong(paramMap, ScanCleanConstants.TTL_LESS);
        ttlMore = MapUtils.getLong(paramMap, ScanCleanConstants.TTL_MORE);
        if(ttlLess != null && ttlMore != null && ttlLess < ttlMore){
            logger.error(marker, "task {} ttl filter value more/less condition fault", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        ttlResetLess = MapUtils.getInteger(paramMap, ScanCleanConstants.TTL_RESET_LESS);
        ttlResetMore = MapUtils.getInteger(paramMap, ScanCleanConstants.TTL_RESET_MORE);
        if(ttlResetLess != null && ttlResetMore != null && ttlResetLess < ttlResetMore){
            logger.error(marker, "task {} ttl reset value more and less condition fault", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        if(ttlResetLess != null && ttlResetMore != null){
            random = new Random();
        }
        perCount = MapUtils.getIntValue(paramMap, ScanCleanConstants.PER_COUNT, 100);
        parentTaskId = MapUtils.getLongValue(paramMap, "parentTaskId");
        hostPort = host + ":" + port;
        getCondition();
        return TaskFlowStatusEnum.SUCCESS;
    }

    private void compilePatternAndSet(){
        Pattern lessMatchPattern = Pattern.compile("@Less\\{[0-9]+\\}Less@");
        Matcher lessMatcher = lessMatchPattern.matcher(pattern);
        boolean foundFlag = lessMatcher.find();
        if(foundFlag){
            int start = lessMatcher.start();
            int end = lessMatcher.end();
            compareType = ScanCleanConstants.COMPARE_TYPE_LESS_THAN;
            compareValue = Long.valueOf(pattern.substring(start + 6, end - 6));
            pattern = pattern.substring(0, start) + "[0-9]*[0-9]" + pattern.substring(end);
            index = start;
        }else{
            Pattern moreMatchPattern = Pattern.compile("@More\\{[0-9]+\\}More@");
            Matcher moreMatcher = moreMatchPattern.matcher(pattern);
            foundFlag = moreMatcher.find();
            if(foundFlag){
                int start = moreMatcher.start();
                int end = moreMatcher.end();
                compareType = ScanCleanConstants.COMPARE_TYPE_MORE_THAN;
                compareValue = Long.valueOf(pattern.substring(start + 6, end - 6));
                pattern = pattern.substring(0, start) + "[0-9]*[0-9]" + pattern.substring(end);
                index = start;
            }
        }
    }

    private void getCondition(){
        StringBuffer stringBuffer = new StringBuffer();
        if(ScanCleanConstants.OPERATE_ANALYSE.equals(operateType)){
            stringBuffer.append("扫描分析： ");
        }else if(ScanCleanConstants.OPERATE_CLEAN.equals(operateType)){
            stringBuffer.append("分析清理： ");
        }else if(ScanCleanConstants.OPERATE_TTL_RESET.equals(operateType)){
            stringBuffer.append("重置ttl： ");
        }
        stringBuffer.append("匹配键=").append(pattern);
        stringBuffer.append(",每次扫描数量=").append(perCount);
        if(compareValue != null){
            stringBuffer.append(",精确筛选条件=(").append("key从第").append(index).append("字符起数字匹配值");
            if(ScanCleanConstants.COMPARE_TYPE_LESS_THAN.equals(compareType)){
                stringBuffer.append(" < ");
            }else {
                stringBuffer.append(" > ");
            }
            stringBuffer.append(compareValue).append(")");
        }
        if(ttlLess != null || ttlMore != null){
            if(ttlLess != null && ttlMore != null){
                stringBuffer.append(",ttl剩余筛选条件=(").append("ttl").append(" > ").append(ttlMore).append(" and ttl < ").append(ttlLess).append(")");
            }else if(ttlLess != null){
                stringBuffer.append(",ttl剩余筛选条件=(").append("ttl").append(" < ").append(ttlLess).append(")");
            }else if(ttlMore != null){
                stringBuffer.append(",ttl剩余筛选条件=(").append("ttl").append(" > ").append(ttlMore).append(")");
            }
        }
        if(ttlResetLess != null || ttlResetMore != null){
            stringBuffer.append(",ttl重置时间配置=(").append("ttl").append(" > ").append(ttlResetMore).append(" and ttl < ").append(ttlResetLess).append(")");
        }
        condition = stringBuffer.toString();
    }

    /**
     * 2.analyseKeyMatch
     *
     * @return
     */
    public TaskFlowStatusEnum analyseKeyMatch() {
        long recordId =  saveSubTaskRecord();
        long startTime = System.currentTimeMillis();

        String instanceScanKey = ConstUtils.getInstanceScanClean(taskId, hostPort);
        Jedis slaveJedis = null;
        Jedis masterJedis = null;
        try {
            slaveJedis = redisCenter.getJedis(host, port, authPwd);
            HostAndPort master = redisCenter.getMaster(host, port, authPwd);
            if(master != null){
                masterJedis = redisCenter.getJedis(master.getHost(), master.getPort(), authPwd);
            }else{
                masterJedis = slaveJedis;
            }
            scanAnalyse(slaveJedis, masterJedis, instanceScanKey);
            //更新记录
            long cost = System.currentTimeMillis() - startTime;
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 1, cost);
            logger.info(marker, "analyse key match successfully, appId:{} hostport:{} cost time is {} ms", appId, hostPort, cost);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, "analyse key match error, appId:{} hostport:{} error:" + e.getMessage(), appId, hostPort, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (slaveJedis != null) {
                slaveJedis.close();
            }
            if (masterJedis != null) {
                masterJedis.close();
            }
        }
    }

    /**
     * 3.scanCleanMatch
     *
     * @return
     */
    public TaskFlowStatusEnum scanClean() {
        long recordId =  saveSubTaskRecord();
        long startTime = System.currentTimeMillis();
        String instanceScanKey = ConstUtils.getInstanceScanClean(taskId, hostPort);
        Jedis jedis = null;
        try {
            jedis = redisCenter.getJedis(host, port, authPwd);
            scanTtlClean(jedis, instanceScanKey);
            //更新记录
            long cost = System.currentTimeMillis() - startTime;
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 1, cost);
            logger.info(marker, "scan clean successfully, appId:{} hostport:{} cost time is {} ms", appId, cost);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, "scan clean key error, appId:{} hostport:{} error:" + e.getMessage(), appId, hostPort, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 4.scanResetTtl
     *
     * @return
     */
    public TaskFlowStatusEnum scanResetTtl() {
        long recordId =  saveSubTaskRecord();
        long startTime = System.currentTimeMillis();
        String instanceScanKey = ConstUtils.getInstanceScanClean(taskId, hostPort);
        Jedis jedis = null;
        try {
            jedis = redisCenter.getJedis(host, port, authPwd);
            scanResetTtl(jedis, instanceScanKey);
            //更新记录
            long cost = System.currentTimeMillis() - startTime;
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 1, cost);
            logger.info(marker, "scan reset ttl successfully, appId:{} hostport:{} cost time is {} ms", appId, cost);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, "scan reset ttl error, appId:{} hostport:{} error:" + e.getMessage(), appId, hostPort, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, instanceScanKey, 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private long saveSubTaskRecord(){
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        record.setNode(hostPort);
        record.setDiagnosticCondition(condition);
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.SCAN_CLEAN.getType());
        record.setStatus(0);
        diagnosticTaskRecordDao.insertDiagnosticTaskRecord(record);
        return record.getId();
    }

    private void scanAnalyse(Jedis slaveJedis, Jedis masterJedis, String instanceScanKey) throws InterruptedException {
        //结果存redis
        assistRedisService.del(instanceScanKey);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s——键分析开始", instanceScanKey));
        long startTime = System.currentTimeMillis();
        ScanParams scanParams = new ScanParams();
        scanParams.count(perCount);
        scanParams.match(pattern);
        ScanResult<String> scanResult = null;
        String cursor = "0";
        Boolean flag = true;
        List<String> keyList = new LinkedList<>();
        List<String> matchKeyList = new LinkedList<>();
        long totalCount = 0;
        long count = 0;
        long notTtlCount = 0;
        long notValueMatchCount = 0;
        long times = 0;
        while (flag) {
            keyList.clear();
            scanResult = slaveJedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            totalCount += scanResult.getResult().size();
            for (String key : scanResult.getResult()) {
                if(compareValue == null){
                    keyList.add(key);
                }else {
                    if(checkMatchByCompareValue(key)){
                        keyList.add(key);
                    }else{
                        notValueMatchCount++;
                    }
                }
            }

            int originalSize = keyList.size();
            if(ttlLess != null || ttlMore != null){
                checkTtlByPipeline(masterJedis, keyList);
            }
            int filterSize = keyList.size();
            notTtlCount += originalSize -filterSize;

            count += keyList.size();

            times++;
            //取样
            this.sampleMatchKey(matchKeyList, keyList, times);
            if(times % 10 == 0){
                logger.info("实例{}——键分析进行中,当前精确匹配键数量：{},排除键数量：{},(值去除({})/ttl去除({})),pattern匹配总数量：{},当前cursor:{}, 匹配键示例：{}",
                        instanceScanKey, count, (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount, cursor, (matchKeyList.size() > 10 ? matchKeyList.subList(0, 10) : matchKeyList));
            }
            if (cursor.equals("0")) {
                flag = false;
                logger.info("实例{}——键分析结束-----------scan over!--------", instanceScanKey);
            }
        }
        //memoryUsageByPipeline
        long avgMemoryUsage = memoryUsage(masterJedis, matchKeyList);
        assistRedisService.rpush(instanceScanKey, String.format("实例键分析结束, 精确匹配键数量：%s, 取样平均内存占用：%s(B), 总内存占用：%s(B), 排除键数量：%s(值去除(%s)/ttl去除(%s)), pattern匹配总数量：%s",
                count, avgMemoryUsage, (count * avgMemoryUsage), (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount));
        this.printMatchKey(instanceScanKey, matchKeyList);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s——键分析结束, 总耗时%sms", instanceScanKey, (System.currentTimeMillis() - startTime)));
    }

    private void scanTtlClean(Jedis masterJedis, String instanceScanKey) throws InterruptedException {
        //结果存redis
        assistRedisService.del(instanceScanKey);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s——键清理开始", instanceScanKey));
        ScanParams scanParams = new ScanParams();
        scanParams.count(perCount);
        scanParams.match(pattern);
        ScanResult<String> scanResult = null;
        String cursor = "0";
        long startTime = System.currentTimeMillis();
        Boolean flag = true;
        List<String> keyList = new LinkedList<>();
        List<String> matchKeyList = new LinkedList<>();
        long totalCount = 0;
        long count = 0;
        long notTtlCount = 0;
        long notValueMatchCount = 0;
        long times = 0;
        while (flag) {
            keyList.clear();
            scanResult = masterJedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            totalCount += scanResult.getResult().size();
            for (String key : scanResult.getResult()) {
                if(compareValue == null){
                    keyList.add(key);
                }else {
                    if(checkMatchByCompareValue(key)){
                        keyList.add(key);
                    }else{
                        notValueMatchCount++;
                    }
                }
            }

            int originalSize = keyList.size();
            if(ttlLess != null || ttlMore != null){
                checkTtlByPipeline(masterJedis, keyList);
            }
            int filterSize = keyList.size();
            notTtlCount += originalSize -filterSize;
            count += keyList.size();

            times++;
            //取样
            this.sampleMatchKey(matchKeyList, keyList, times);
            if(times % 10 == 0){
                logger.info("实例：{},键清理进行中,当前精确匹配键数量：{},排除键数量：{},(值去除({})/ttl去除({})),pattern匹配总数量：{},当前cursor:{}, 匹配键示例：{}",
                        instanceScanKey, count, (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount, cursor, (matchKeyList.size() > 10 ? matchKeyList.subList(0, 10) : matchKeyList));
            }
            if(keyList.size() > 0){
                unlinkByPipeline(masterJedis, keyList);
                if(times % 10 == 0){
                    logger.info("实例：{},键清理进行中,删除键:{}", instanceScanKey, (keyList.size() > 10 ? keyList.subList(0, 10) : keyList));
                }
            }
            if (cursor.equals("0")) {
                flag = false;
                logger.info("实例{}——键清理结束------------scan over!--------", instanceScanKey);
            }
        }
        assistRedisService.rpush(instanceScanKey, String.format("实例——键清理结束, 精确匹配清理键数量：%s, 排除键数量：%s(值去除(%s)/ttl去除(%s)), pattern匹配总数量：%s",
                count, (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount));
        this.printMatchKey(instanceScanKey, matchKeyList);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s——键清理结束, 总耗时%sms", instanceScanKey, (System.currentTimeMillis() - startTime)));
    }

    private void scanResetTtl(Jedis masterJedis, String instanceScanKey) throws InterruptedException {
        //结果存redis
        assistRedisService.del(instanceScanKey);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s—键重置ttl开始", instanceScanKey));
        ScanParams scanParams = new ScanParams();
        scanParams.count(perCount);
        scanParams.match(pattern);
        ScanResult<String> scanResult = null;
        String cursor = "0";
        long startTime = System.currentTimeMillis();
        Boolean flag = true;
        List<String> keyList = new LinkedList<>();
        List<String> matchKeyList = new LinkedList<>();
        long totalCount = 0;
        long count = 0;
        long notTtlCount = 0;
        long notValueMatchCount = 0;
        long times = 0;
        while (flag) {
            keyList.clear();
            scanResult = masterJedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            totalCount += scanResult.getResult().size();
            for (String key : scanResult.getResult()) {
                if(compareValue == null){
                    keyList.add(key);
                }else {
                    if(checkMatchByCompareValue(key)){
                        keyList.add(key);
                    }else{
                        notValueMatchCount++;
                    }
                }
            }

            int originalSize = keyList.size();
            if(ttlLess != null || ttlMore != null){
                checkTtlByPipeline(masterJedis, keyList);
            }
            int filterSize = keyList.size();
            notTtlCount += originalSize -filterSize;

            count += keyList.size();

            times++;
            //取样
            this.sampleMatchKey(matchKeyList, keyList, times);
            if(times % 10 == 0){
                logger.info("实例：{},键重置ttl进行中,当前精确匹配键数量：{},排除键数量：{},(值去除({})/ttl去除({})),pattern匹配总数量：{},当前cursor:{}, 匹配键示例：{}",
                        instanceScanKey, count, (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount, cursor, (matchKeyList.size() > 10 ? matchKeyList.subList(0, 10) : matchKeyList));
            }
            if(keyList.size() > 0){
                resetTtlByPatch(masterJedis, keyList);
                if(times % 10 == 0){
                    logger.info("实例：{},键重置ttl进行中,重置ttl键:{}", instanceScanKey, (keyList.size() > 10 ? keyList.subList(0, 10) : keyList));
                }
            }
            if (cursor.equals("0")) {
                flag = false;
                logger.info("实例{}——键重置ttl结束------------scan over!--------", instanceScanKey);
            }
        }
        assistRedisService.rpush(instanceScanKey, String.format("实例——键重置ttl结束, 精确匹配重置键数量：%s, 排除键数量：%s(值去除(%s)/ttl去除(%s)), pattern匹配总数量：%s",
                count, (notValueMatchCount + notTtlCount), notValueMatchCount, notTtlCount , totalCount));
        this.printMatchKey(instanceScanKey, matchKeyList);
        assistRedisService.rpush(instanceScanKey, String.format("实例%s——键重置ttl结束, 总耗时%sms", instanceScanKey, (System.currentTimeMillis() - startTime)));
    }

    private boolean checkMatchByCompareValue(String key){
        if(index == null || compareType == null || compareValue == null){
            return false;
        }
        String subKey = key.substring(index);
        Matcher matcher = patternExp.matcher(subKey);
        boolean foundFlag = matcher.find();
        if(foundFlag){
            int end = matcher.end();
            try{
                Long id = Long.valueOf(subKey.substring(0, end));
                if(ScanCleanConstants.COMPARE_TYPE_MORE_THAN.equals(compareType)){
                    return id > compareValue;
                }else if(ScanCleanConstants.COMPARE_TYPE_LESS_THAN.equals(compareType)){
                    return id < compareValue;
                }
            }catch (Exception e){
                logger.error("checkMatchByCompareValue key {}, error,", key, e.getMessage());
            }
        }
        return false;
    }

    private void checkTtlByPipeline(Jedis jedis, List<String> keyList){
        Pipeline pipelined = jedis.pipelined();
        for (String key : keyList){
            pipelined.ttl(key);
        }
        List<Object> ttlList = pipelined.syncAndReturnAll();
        int j = 0;
        for(int i = 0; i < ttlList.size(); i++){
            Object ttlObj = ttlList.get(i);
            if(ttlObj instanceof Number){
                if(((Long) ttlObj > 0)){
                    if(ttlLess != null && ttlMore != null && ((Long) ttlObj > ttlLess || (Long) ttlObj < ttlMore)) {
                        keyList.remove(j);
                        j--;
                    }else if(ttlLess != null && (Long) ttlObj > ttlLess){
                        keyList.remove(j);
                        j--;
                    }else if(ttlMore != null && (Long) ttlObj < ttlMore){
                        keyList.remove(j);
                        j--;
                    }
                }
            }else{
                keyList.remove(j);
                j--;
            }
            j++;
        }
    }

    private long memoryUsage(Jedis jedis, List<String> keyList){
        if(CollectionUtils.isEmpty(keyList)){
            return 0;
        }
        int totalCount = keyList.size();
        long sumMemUsage = 0;
        for (String key : keyList){
            Long memoryUsage = jedis.memoryUsage(key);
            if(memoryUsage == null || memoryUsage < 0){
                totalCount--;
            }else{
                sumMemUsage += memoryUsage;
            }
        }
        return sumMemUsage/totalCount;
    }

    private void sampleMatchKey(List<String> matchKeyList, List<String> keyList, long times){
        if(CollectionUtils.isNotEmpty(keyList)){
            if(matchKeyList.size() < 500){
                int num = perCount/keyList.size() * 2;
                num  = num > 50 ? 50 : (num > 5 ? num: 5);
                matchKeyList.addAll(keyList.size() > num ? keyList.subList(0, num) : keyList);
                return;
            }
            if(times%50 == 0){
                matchKeyList.addAll(keyList.size() > 10 ? keyList.subList(0, 10) : keyList);
                if(matchKeyList.size() > 500){
                    for(int i = 0; i < matchKeyList.size() - 500;){
                        int toRem = new Random().nextInt(500);
                        matchKeyList.remove(toRem);
                    }
                }
            }
        }
    }

    private void printMatchKey(String instanceScanKey, List<String> matchKeyList){
        if(CollectionUtils.isNotEmpty(matchKeyList)){
            if(matchKeyList.size() > 100){
                Collections.shuffle(matchKeyList);
                matchKeyList = matchKeyList.subList(0, 100);
            }
            List<String> matchResultList = new ArrayList<>();
            boolean first = true;
            for(int i = 0; i < matchKeyList.size() / 20; ){
                if(first){
                    matchResultList.add("匹配键示例：[" + matchKeyList.subList(0, 20).stream().collect(Collectors.joining(",")));
                    first = false;
                }else{
                    if(matchKeyList.size() != 20){
                        matchResultList.add(matchKeyList.subList(0, 20).stream().collect(Collectors.joining(",")));
                    }else{
                        matchResultList.add(matchKeyList.subList(0, 20).stream().collect(Collectors.joining(",")) + "]");
                    }
                }
                matchKeyList = matchKeyList.subList(20, matchKeyList.size());
            }
            if(matchKeyList.size() > 0){
                if(first){
                    matchResultList.add("匹配键示例：[" + matchKeyList.stream().collect(Collectors.joining(",")) + "]");
                }else{
                    matchResultList.add(matchKeyList.stream().collect(Collectors.joining(",")) + "]");
                }
            }
            assistRedisService.rpushList(instanceScanKey, matchResultList);
        }else{
            assistRedisService.rpush(instanceScanKey, "匹配键示例：[]");
        }
    }

    private void unlinkByPatch(Jedis jedis, List<String> keyList){
        jedis.unlink(keyList.toArray(new String[keyList.size()]));
    }

    private void unlinkByPipeline(Jedis jedis, List<String> keyList){
        Pipeline pipelined = jedis.pipelined();
        for (String key : keyList){
            pipelined.unlink(key);
        }
        pipelined.sync();
    }

    private void unlinkBySlotAndPatch(Map<Integer, Jedis> slotJedisMap, List<String> keyList){
        Map<Jedis, List<String>> keyToJedisMap = new HashMap<>();
        for (String key : keyList){
            int slot = JedisClusterCRC16.getSlot(key);
            Jedis jedis = slotJedisMap.get(slot);
            if(keyToJedisMap.containsKey(jedis)){
                keyToJedisMap.get(jedis).add(key);
            }else{
                keyToJedisMap.put(jedis, Lists.newArrayList(key));
            }
        }
        Set<Jedis> jedisSet = keyToJedisMap.keySet();
        for (Jedis jedis : jedisSet){
            jedis.unlink(keyToJedisMap.get(jedis).toArray(new String[keyToJedisMap.get(jedis).size()]));
        }
    }

    private void resetTtlByPatch(Jedis jedis, List<String> keyList){
        Pipeline pipelined = jedis.pipelined();
        for (String key : keyList){
            pipelined.expire(key, ttlResetMore + random.nextInt(ttlResetLess - ttlResetMore));
        }
        pipelined.sync();
    }

}
