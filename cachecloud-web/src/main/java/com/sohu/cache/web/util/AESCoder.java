package com.sohu.cache.web.util;

import com.sohu.cache.util.StringUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class AESCoder {

    /**
     * 密钥算法
     * java6支持56位密钥，bouncycastle支持64位
     * */
    public static final String KEY_ALGORITHM="AES";

    /**
     * 加密/解密算法/工作模式/填充方式
     *
     * JAVA6 支持PKCS5PADDING填充方式
     * Bouncy castle支持PKCS7Padding填充方式
     * */
    public static final String CIPHER_ALGORITHM="AES/ECB/PKCS5Padding";

    /**
     *
     * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
     * @return byte[] 二进制密钥
     * */
    public static byte[] initkey() throws Exception{

        //实例化密钥生成器
        KeyGenerator kg=KeyGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
        kg.init(256);
        //生成密钥
        SecretKey secretKey=kg.generateKey();
        //获取二进制密钥编码形式
        return secretKey.getEncoded();
    }
    /**
     * 转换密钥
     * @param key 二进制密钥
     * @return Key 密钥
     * */
    public static Key toKey(byte[] key) throws Exception{
        //实例化DES密钥
        //生成密钥
        SecretKey secretKey=new SecretKeySpec(key,KEY_ALGORITHM);
        return secretKey;
    }

    /**
     * 加密数据
     * @param data 待加密数据
     * @return byte[] 加密后的数据
     * */
    public static String encrypt(String data, String key) throws Exception{
        if(StringUtil.isBlank(data)){
            return "";
        }
        byte[] encrypt = encrypt(data.getBytes(), key.getBytes());
        return parseByte2HexStr(encrypt);
    }

    /**
     * 加密数据
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密后的数据
     * */
    public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
        //还原密钥
        Key k=toKey(key);
        /**
         * 实例化
         * 使用 PKCS7PADDING 填充方式，按如下方式实现,就是调用bouncycastle组件实现
         * Cipher.getInstance(CIPHER_ALGORITHM,"BC")
         */
        Cipher cipher=Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(data);
    }

    /**
     * 解密数据
     * @param data 待解密数据
     * @return byte[] 解密后的数据
     * */
    public static String decrypt(String data, String key) throws Exception{
        if(StringUtil.isBlank(data)){
            return null;
        }
        byte[] dataByte = parseHexStr2Byte(data);
        if(dataByte == null || dataByte.length < 1){
            return null;
        }
        byte[] decrypt = decrypt(dataByte, key.getBytes());
        return new String(decrypt, "UTF-8");
    }

    /**
     * 解密数据
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密后的数据
     * */
    public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
        //欢迎密钥
        Key k =toKey(key);
        /**
         * 实例化
         * 使用 PKCS7PADDING 填充方式，按如下方式实现,就是调用bouncycastle组件实现
         * Cipher.getInstance(CIPHER_ALGORITHM,"BC")
         */
        Cipher cipher=Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(data);
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr    字符串
     * @return          字节数组
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf       字节数组
     * @return          字符串
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

}