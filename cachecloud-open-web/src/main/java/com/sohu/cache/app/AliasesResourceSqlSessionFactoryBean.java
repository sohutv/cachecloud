package com.sohu.cache.app;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;

/**
 * Created by zhangyijun on 15/10/28.
 */
public class AliasesResourceSqlSessionFactoryBean extends SqlSessionFactoryBean {
    private static final Log LOGGER = LogFactory.getLog(SqlSessionFactoryBean.class);

    private String packages;

    public void setTypeAliasesPackage(String packages) {
        this.packages = packages;
    }

    public void setTypeAliasesClassResources(Resource[] resources) {
        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            try {
                String className;
                if (resource instanceof ClassPathResource) {
                    String path = ((ClassPathResource) resource).getPath();
                    className = getClassNameByPath(path);
                } else if (resource instanceof FileSystemResource) {
                    String path = ((FileSystemResource) resource).getPath();
                    className = getClassNameByPath(path);
                } else {
                    throw new RuntimeException("resources is unsupported");
                }
                className = packages + className;
                Class<?> clazz = ClassUtils.resolveClassName(className, Thread.currentThread().getContextClassLoader());
                classList.add(clazz);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
        this.setTypeAliases(classList.toArray(new Class[0]));
    }

    public String getClassNameByPath(String path) {
        String className = path.substring(path.lastIndexOf('/'), path.length());
        className = className.replace(".class", "").replace('/', '.');
        return className;
    }

}
