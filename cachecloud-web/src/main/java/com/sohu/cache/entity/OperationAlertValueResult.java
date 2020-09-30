package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by rucao
 */
@Data
public class OperationAlertValueResult {
    String ip;
    MachineInfo machineInfo;
    String type;
    String status;
    String message;

    public OperationAlertValueResult(String ip, MachineInfo machineInfo, String type, String status, String message) {
        this.ip = ip;
        this.machineInfo = machineInfo;
        this.type = type;
        this.status = status;
        this.message = message;
    }

}
