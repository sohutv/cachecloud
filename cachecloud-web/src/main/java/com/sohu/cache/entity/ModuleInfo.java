package com.sohu.cache.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by chenshi on 2021/5/7.
 */
@Data
public class ModuleInfo {

    private int id;

    private String name;

    private String gitUrl;

    private String info;

    private int status;

    private List<ModuleVersion> versions;
}
