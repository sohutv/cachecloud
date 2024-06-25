package com.sohu.cache.web.service.impl;

import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.task.constant.PushEnum;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by chenshi on 2020/7/6.
 */
@Service("resourceService")
public class ResourceServiceImpl implements ResourceService {

    private Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    ResourceDao resourceDao;
    @Autowired
    SSHService sshService;

    @PostConstruct
    public void init() {
        List<SystemResource> resourceList = this.getResourceList(ResourceEnum.REDIS.getValue());
        ConstUtils.REDIS_RESOURCE = resourceList.stream().collect(Collectors.toMap(res -> Integer.valueOf(res.getId()), Function.identity()));
    }

    public SuccessEnum saveResource(SystemResource systemResouce) {
        try {
            resourceDao.save(systemResouce);
        } catch (Exception e) {
            logger.error("key {} value {} update faily" + e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

    public SuccessEnum updateResource(SystemResource systemResouce) {
        try {
            resourceDao.update(systemResouce);
        } catch (Exception e) {
            logger.error("key {} value {} update faily" + e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.REPEAT;
    }

    public List<SystemResource> getResourceList(int resourceType) {
        try {
            return resourceDao.getResourceList(resourceType);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<SystemResource> getResourceList(int resourceType, String searchName) {
        try {
            return resourceDao.getResourceListByName(resourceType, searchName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public SuccessEnum pushScript(Integer repositoryId, Integer resourceId, String content, AppUser userInfo) {

        try {
            SystemResource repository = resourceDao.getResourceById(repositoryId);
            SystemResource resource = resourceDao.getResourceById(resourceId);
            // a).推送脚本
            if (repository != null && resource != null && resource.getType() == ResourceEnum.SCRIPT.getValue()) {
                // 远程仓库地址ip/path
                String repos_ip = repository.getName();
                String fileDir = String.format("%s%s", repository.getDir(), resource.getDir());

                // 1. 先内容保存到本地
                String localAbsolutePath = MachineProtocol.TMP_DIR + resource.getName();
                File tmpDir = new File(MachineProtocol.TMP_DIR);
                if (!tmpDir.exists()) {
                    if (!tmpDir.mkdirs()) {
                        logger.error("cannot create /tmp/cachecloud directory.");
                    }
                }
                Path path = Paths.get(MachineProtocol.TMP_DIR + resource.getName());
                BufferedWriter bufferedWriter = null;
                try {
                    bufferedWriter = Files.newBufferedWriter(path, Charset.forName(MachineProtocol.ENCODING_UTF8));
                    bufferedWriter.write(content);
                } catch (IOException e) {
                    logger.error("write rmt file error, ip: {}, path: {}, content: {}", repos_ip, localAbsolutePath, content);
                    return SuccessEnum.FAIL;
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                // 2. scp file to remote

                try {
                    //2.1 备份老文件
                    String bakDir = fileDir + "/bak";
                    String filename = fileDir + "/" + resource.getName();
                    String bakfilename = bakDir + "/" + resource.getName() + "-" + DateUtil.formatYYYYMMddHHMMss(new Date()) + "-" + userInfo.getName();
                    String bakcmd = String.format("mkdir -p %s && cp -r %s %s", bakDir, filename, bakfilename);
                    String bak_result = SSHUtil.execute(repos_ip, bakcmd);
                    logger.info("bak_result: {}", bak_result);

                    //2.2 上传新文件
                    SSHUtil.scpFileToRemote(repos_ip, localAbsolutePath, fileDir);
                } catch (SSHException e) {
                    logger.error("message {}", e.getMessage(), e);
                    return SuccessEnum.FAIL;
                }
                // 3. delete temp file
                File file = new File(localAbsolutePath);
                if (file.exists()) {
                    boolean del = file.delete();
                    if (!del) {
                        logger.warn("file.delete:{}", del);
                    }
                }
                // 4.update push status
                resource.setIspush(PushEnum.YES.getValue());
                resource.setUsername(userInfo.getName());
                resource.setLastmodify(new Date());
                resourceDao.update(resource);
            }
        } catch (Exception e) {
            logger.error("pushScript resource repositoryId:{} resourceId:{} error:{}", repositoryId, resourceId, e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

    @Override
    public SuccessEnum pushDir(Integer repositoryId, Integer resourceId, AppUser userInfo) {

        try {
            SystemResource repository = resourceDao.getResourceById(repositoryId);
            SystemResource resource = resourceDao.getResourceById(resourceId);
            if (repository != null && resource != null) {
                // 1.推送目录
                SSHTemplate.Result result = sshService.executeWithResult(repository.getName(), String.format("mkdir -p %s", repository.getDir() + resource.getName()));
                if (!result.isSuccess()) {
                    logger.error("pushDir resource repositoryId:{} resourceId:{} result:{}", repositoryId, resourceId, result);
                    return SuccessEnum.FAIL;
                }
                // 2.update push status
                resource.setIspush(PushEnum.YES.getValue());
                resource.setUsername(userInfo.getName());
                resource.setLastmodify(new Date());
                resourceDao.update(resource);
            }

        } catch (Exception e) {
            logger.error("pushDir resource repositoryId:{} resourceId:{} error:{}", repositoryId, resourceId, e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

    public SystemResource getResourceById(int repositoryId) {
        try {
            return resourceDao.getResourceById(repositoryId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public SystemResource getResourceByName(String resourceName) {
        try {
            return resourceDao.getResourceByName(resourceName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String getRespositoryUrl(int resourceId, int respoitoryId) {

        try {
            SystemResource resource = resourceDao.getResourceById(resourceId);
            SystemResource repository = resourceDao.getResourceById(respoitoryId);

            String ip = repository.getName();
            // validate ip connect
            if (!StringUtils.isEmpty(ip)) {
                return String.format("%s%s/%s", repository.getUrl(), resource.getDir(), resource.getName());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    public String getRemoteFileContent(int resourceId, int respoitoryId) {

        try {
            SystemResource resource = resourceDao.getResourceById(resourceId);
            SystemResource repository = resourceDao.getResourceById(respoitoryId);

            String ip = repository.getName();
            // validate ip connect
            if (!StringUtils.isEmpty(ip)) {
                String command = String.format("cat %s%s/%s", repository.getDir(), resource.getDir(), resource.getName());
                SSHTemplate.Result result = sshService.executeWithResult(ip, command);
                logger.info(result.getResult());
                return result.getResult();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public SystemResource getRepository() {
        try {
            List<SystemResource> repositorylist = resourceDao.getResourceList(ResourceEnum.Repository.getValue());
            if (!CollectionUtils.isEmpty(repositorylist)) {
                return repositorylist.get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<Integer, Integer> getAppUseRedis() {
        Map<Integer, Integer> resultMap = new HashMap<>();
        try {
            List<Map<Integer, Integer>> mapList = resourceDao.getAppUseRedis();
            if (!CollectionUtils.isEmpty(mapList)) {
                for (Map<Integer, Integer> stat : mapList) {
                    resultMap.put(MapUtils.getInteger(stat, "version_id"), MapUtils.getInteger(stat, "num"));
                }
            }
        } catch (Exception e) {
            logger.error("getAppUseRedis error :{}", e.getMessage(), e);
        }
        return resultMap;
    }

    @Override
    public SystemResource getRedisResourceByCache(Integer repositoryId) {
        SystemResource resource = null;
        try {
            Map<Integer, SystemResource> resourceMap = ConstUtils.REDIS_RESOURCE;
            if (MapUtils.isNotEmpty(resourceMap)) {
                resource = resourceMap.get(repositoryId);
            }

            if (resource == null) {
                resource = this.getResourceById(repositoryId);
                if (resource != null) {
                    ConstUtils.REDIS_RESOURCE.put(Integer.valueOf(resource.getId()), resource);
                }
            }
            return resource;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getRedisVersion(Integer repositoryId) {
        String version = null;
        SystemResource redisResource = this.getRedisResourceByCache(repositoryId);
        if (redisResource != null) {
            String name = redisResource.getName();
            if (name.contains("-")) {
                String[] nameArr = name.split("-");
                if (nameArr != null && nameArr.length == 2) {
                    String[] versionArr = nameArr[1].split("\\.");
                    if (versionArr != null && versionArr.length == 3) {
                        version = nameArr[1];
                    }
                }
            }
        }
        return version;
    }

    @Override
    public boolean checkRedisVersionGreater(Integer repositoryId, int[] versions) {
        SystemResource redisResource = this.getRedisResourceByCache(repositoryId);
        if (redisResource != null) {
            String name = redisResource.getName();
            if (name.contains("-")) {
                String[] nameArr = name.split("-");
                if (nameArr != null && nameArr.length == 2) {
                    String[] versionArr = nameArr[1].split("\\.");
                    if (versionArr != null && versionArr.length == 3) {
                        for (int i = 0; i < versions.length; i++) {
                            if (Integer.valueOf(versionArr[i]) < versions[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
