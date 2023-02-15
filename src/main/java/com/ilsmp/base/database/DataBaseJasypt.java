package com.ilsmp.base.database;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Author: zhangjiahao04 Title: DataBaseJasypt Package: com.data.export.tool.database Description: 配置文件加解密生成 Date:
 * 2022/4/7 18:23
 */
public class DataBaseJasypt {

    private static final String PBEWITHHMACSHA512ANDAES_256 = "PBEWITHHMACSHA512ANDAES_256";


    private static PooledPBEStringEncryptor creatEncrypt(String factor) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        // 2. 加解密配置
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(factor);
        config.setAlgorithm(PBEWITHHMACSHA512ANDAES_256);
        // 为减少配置文件的书写，以下都是 Jasyp 3.x 版本，配置文件默认配置
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * Description: Jasyp 加密（PBEWITHHMACSHA512ANDAES_256）
     *
     * @param plainText
     *         待加密的原文
     * @param factor
     *         加密秘钥
     * @return java.lang.String
     */
    public static String encryptWithSHA512andAES256(String plainText, String factor) {
        // 1. 创建加解密工具实例
        PooledPBEStringEncryptor encryptor = creatEncrypt(factor);
        // 3. 加密
        return encryptor.encrypt(plainText);
    }

    /**
     * Description: Jaspy解密（PBEWITHHMACSHA512ANDAES_256）
     *
     * @param encryptedText
     *         待解密密文
     * @param factor
     *         解密秘钥
     * @return java.lang.String
     */
    public static String decryptWithSHA512andAES256(String encryptedText, String factor) {
        // 1. 创建加解密工具实例
        PooledPBEStringEncryptor encryptor = creatEncrypt(factor);
        // 3. 解密
        return encryptor.decrypt(encryptedText);
    }

    public static void main(String[] args) {
        // 加盐
        String factor = "ENC(zjh)";
        // 待加密文本
        String plainText = "bdl";
        String encryptWithSHA512Str = encryptWithSHA512andAES256(plainText, factor);
        String decryptWithSHA512Str = decryptWithSHA512andAES256(encryptWithSHA512Str, factor);
        System.out.println("采用AES256加密前原文密文：ENC(" + encryptWithSHA512Str + ")");
        System.out.println("采用AES256解密后密文原文:" + decryptWithSHA512Str);
    }

}
