package com.sohu.cache.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * app详情枚举(以后有关app详情的都往这里面迁)
 * 
 * @author leifu
 * @Date 2015年1月26日
 * @Time 上午11:34:22
 */
public class AppDescEnum {
    
    /**
     * 应用类型：0 正式，1：测试 2：试用
     */
    public static enum AppTest {
        IS_TEST(1),
        NOT_TEST(0),
        IS_TRAIL(2);

        private int value;

        private AppTest(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * 应用重要度
     */
    public static enum AppImportantLevel {
        SUPER_IMPORTANT(1, "S"),
        VERY_IMPORTANT(2, "A"),
        IMPORTANT(3, "B"),
        COMMON(4, "C");

        private int value;
        
        private String info;

        private AppImportantLevel(int value, String info) {
            this.value = value;
            this.info = info;
        }

        public int getValue() {
            return value;
        }

        public String getInfo() {
            return info;
        }


    }
    
    
    /**
     * 应用内存淘汰策略
     */
    public static enum MaxmemoryPolicyType {
        NOEVICTION(0, "noeviction","不淘汰，占满写入失败"),
        ALLKEYSLRU(1, "allkeys-lru","所有键-最近最少使用"),
        ALLKEYSLFU(2, "allkeys-lfu","所有键-最少频率使用"),
        VOLATILELRU(3, "volatile-lru","有过期时间的键-最近最少使用"),
        VOLATILELFU(4, "volatile-lfu","有过期时间的键-最少频率使用"),
        ALLKEYSRANDOM(5, "allkeys-random","所有键-随机"),
        VOLATILERANDOM(6, "volatile-random","有过期时间的键-随机"),
        VOLATILETTL(7, "volatile-ttl","有过期时间的键-剩余时间最短");

        private int type;

        private String name;

        private String desc;

        private MaxmemoryPolicyType(int type, String name, String desc) {
           this.type = type;
           this.name = name;
           this.desc = desc;
        }

        public int getType(){
            return type;
        }

        public String getName(){
            return name;
        }

        public String getDesc(){
            return desc;
        }

        public static MaxmemoryPolicyType getByType(int type){
            Optional<MaxmemoryPolicyType> policyTypeOptional = Arrays.asList(MaxmemoryPolicyType.values()).stream().filter(maxmemoryPolicyType -> maxmemoryPolicyType.type == type).findFirst();
            if(policyTypeOptional.isPresent()){
                return policyTypeOptional.get();
            }
            return null;
        }

        public static List<MaxmemoryPolicyType> getAll(){
            MaxmemoryPolicyType[] values = MaxmemoryPolicyType.values();
            return Arrays.asList(values);
        }
    }

}
