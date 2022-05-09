package com.sohu.cache.constant;

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
    
    
}
