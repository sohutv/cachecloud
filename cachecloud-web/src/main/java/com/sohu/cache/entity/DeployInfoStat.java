package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by rucao on 2018/10/10
 */
@Data
public class DeployInfoStat {
    private Integer masterNum;
    private Integer slaveNum;
    private Integer sentinelNum;
    private Integer twemproxyNum;

    public DeployInfoStat(Integer masterNum, Integer slaveNum, Integer sentinelNum) {
        this.masterNum = masterNum;
        this.slaveNum = slaveNum;
        this.sentinelNum = sentinelNum;
    }

    public DeployInfoStat(Integer masterNum, Integer slaveNum, Integer sentinelNum, Integer twemproxyNum) {
        this.masterNum = masterNum;
        this.slaveNum = slaveNum;
        this.sentinelNum = sentinelNum;
        this.twemproxyNum = twemproxyNum;
    }

    public DeployInfoStat() {
        this.masterNum = 0;
        this.slaveNum = 0;
        this.sentinelNum = 0;
        this.twemproxyNum = 0;
    }

}
