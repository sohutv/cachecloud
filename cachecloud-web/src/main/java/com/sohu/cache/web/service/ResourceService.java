package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.web.enums.SuccessEnum;

import java.util.List;
import java.util.Map;

/**
 * Created by chenshi on 2020/7/6.
 */
public interface ResourceService {

    SuccessEnum saveResource(SystemResource systemResouce);

    SuccessEnum updateResource(SystemResource systemResouce);

    List<SystemResource> getResourceList(int resourceType);

    List<SystemResource> getResourceList(int resourceType, String searchName);

    SuccessEnum pushScript(Integer repositoryId, Integer resourceId, String content, AppUser userInfo);

    SuccessEnum pushDir(Integer repositoryId, Integer resourceId, AppUser userInfo);

    SystemResource getResourceById(int resourceId);

    SystemResource getResourceByName(String resourceName);

    String getRespositoryUrl(int resourceId, int respoitoryId);

    String getRemoteFileContent(int resourceId, int respoitoryId);

    //获取远程仓库信息
    SystemResource getRepository();

    Map<Integer, Integer> getAppUseRedis();

}

