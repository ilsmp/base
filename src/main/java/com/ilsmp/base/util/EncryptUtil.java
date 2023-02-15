/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.ilsmp.base.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

/**
 * md5/AES/crc32/密码加密处理 字符串填充
 */
@Slf4j
public class EncryptUtil {

    private static MessageDigest messagedigest;
    private static SnowflakeGenerator snowflake;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nae) {
            log.info("EncryptUtil", nae);
        }
    }

    /**
     * 返回32位md5结果
     *
     * @param s
     * @return
     */
    public static String md5FromString(String s) {
        return md5ToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String md5ToString(byte[] bytes) {
        messagedigest.update(bytes);
        return hexFrom(messagedigest.digest());
    }

    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr
     *         待解密的base 64 code
     * @param decryptKey
     *         解密密钥
     * @return 解密后的string
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtil.isEmpty(encryptStr) ? null : aesDecryptFromBytes(base64Decode(encryptStr), decryptKey);
    }

    /**
     * AES加密为base 64 code
     *
     * @param content
     *         待加密的内容
     * @param encryptKey
     *         加密密钥
     * @return 加密后的base 64 code
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * AES加密
     *
     * @param content
     *         待加密的内容
     * @param encryptKey
     *         加密密钥
     * @return 加密后的byte[]
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator ken = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        ken.init(128, random);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ken.generateKey().getEncoded(), "AES"));
        return cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * AES解密
     *
     * @param encryptBytes
     *         待解密的byte[]
     * @param decryptKey
     *         解密密钥
     * @return 解密后的String
     */
    public static String aesDecryptFromBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes());
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

    /**
     * base 64 encode
     *
     * @param bytes
     *         待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static String base64Encode(byte[] bytes) {
        return Base64Utils.encodeToString(bytes);
    }

    /**
     * base 64 decode
     *
     * @param base64Code
     *         待解码的base 64 code
     * @return 解码后的byte[]
     */
    public static byte[] base64Decode(String base64Code) {
        return Base64Utils.decodeFromString(base64Code);
    }

    /**
     * HMAC SHA256
     *
     * @param secret
     * @param message
     * @return
     */
    public static String hmacSha256(String message, String secret) throws Exception {
        byte[] keyBytes = secret.getBytes();
        byte[] plainBytes = message.getBytes();
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hashs = sha256_HMAC.doFinal(plainBytes);
        StringBuilder sb = new StringBuilder();
        for (byte x : hashs) {
            String b = Integer.toHexString(x & 0XFF);
            if (b.length() == 1) {
                b = '0' + b;
            }
            sb.append(b);
        }
        return sb.toString();
    }

    /*
     * Author: zhangjiahao04
     * Description: getCrc32Value 生成crc32位
     * Date: 2022/11/9 12:07
     * Param: [str]
     * return: java.lang.Long
     **/
    public static Long crc32FromStr(String str) {
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        return Math.abs(crc32.getValue());
    }

    /**
     * 生成规则: CRC32(id)%100
     *
     * @param id
     * @return
     */
    public static Integer crc32ToBucketId(String id) {
        return Long.valueOf(crc32FromStr(id) % 100).intValue();
    }

    /**
     * 生成规则: abs(id)%100
     *
     * @param id
     * @returnLong
     */
    public static Integer crc32ToBucketId(Long id) {
        return Long.valueOf(Math.abs(id) % 100).intValue();
    }

    /**
     * 生成es的{bucketId}
     *
     * @param tenantId
     * @param projectId
     * @param partitionNum
     * @return
     */
    public static Integer crc32ToEsBucketId(Long tenantId, Long projectId, Long partitionNum) {
        String str = tenantId + "_" + projectId;
        return Long.valueOf(crc32FromStr(str) % partitionNum).intValue();
    }

    /**
     * 生成es的{bucketId}
     *
     * @param tenantId
     * @param projectId
     * @return
     */
    public static Integer crc32ToEsBucketId(Long tenantId, Long projectId) {
        String str = tenantId + "_" + projectId;
        return Long.valueOf(crc32FromStr(str) % 100L).intValue();
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param password
     *         the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String hashFrom(String password) throws Exception {
        return hashFrom(password.toCharArray());
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param password
     *         the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String hashFrom(char[] password) throws Exception {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24];
        random.nextBytes(salt);
        // Hash the password
        byte[] hash = pbkdf2(password, salt, 10, 24);
        // format salt:hash
        return hexFrom(salt) + ":" + hexFrom(hash);
    }

    /**
     * Validates a password using a hash.
     *
     * @param password
     *         the password to check
     * @param correctHash
     *         the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean hashValidate(String password, String correctHash) throws Exception {
        return hashValidate(password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     *
     * @param password
     *         the password to check
     * @param correctHash
     *         the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean hashValidate(char[] password, String correctHash) throws Exception {
        // Decode the hash into its parameters
        String[] params = correctHash.split(":");
        byte[] salt = hexFrom(params[0]);
        byte[] hash = hexFrom(params[1]);
        // Compute the hash of the provided password, using the same salt iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, 10, hash.length);
        // Compare the hashes in constant time. The password is correct if both hashes match.
        return bytesEqual(hash, testHash);
    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method is used so that password hashes cannot
     * be extracted from an on-line system using a timing attack and then attacked off-line.
     *
     * @param a
     *         the first byte array
     * @param b
     *         the second byte array
     * @return true if both byte arrays are the same, false if not
     */
    public static boolean bytesEqual(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Computes the PBKDF2 hash of a password.
     *
     * @param password
     *         the password to hash.
     * @param salt
     *         the salt
     * @param iterations
     *         the iteration count (slowness factor)
     * @param bytes
     *         the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param hex
     *         the hex string
     * @return the hex string decoded into a byte array
     */
    public static byte[] hexFrom(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param array
     *         the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    public static String hexFrom(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    public static void hexAppendByteTo(StringBuffer stringbuffer, byte b) {
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char c0 = digits[(b & 0xf0) >> 4];
        char c1 = digits[b & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     * 手机号验证
     *
     * @param phoneNum
     * @return
     */
    public static boolean phoneValidate(String phoneNum) {
        Pattern phonePattern = Pattern.compile("^[1][0-9]{10}$");
        Matcher m = phonePattern.matcher(phoneNum);
        return m.matches();
    }

    /*
     * Author: zhangjiahao04
     * Description: province 地址转换
     * Date: 2022/11/9 12:30
     * Param: [address]
     * return: java.util.List<java.lang.String>
     **/
    public static List<String> province(String address) {
        String regex = "(?<province>[^省]+自治区|.*?省|.*?行政区|.*?市)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String province;
        List<String> table = new ArrayList<>();
        while (m.find()) {
            province = m.group("province");
            if (!StringUtil.isEmpty(province)) {
                table.add(province.trim());
            }
        }
        return table;
    }

    public static List<String> city(String address) {
        String regex = "(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String city;
        List<String> table = new ArrayList<>();
        while (m.find()) {
            city = m.group("city");
            if (!StringUtil.isEmpty(city)) {
                table.add(city.trim());
            }
        }
        return table;
    }

    public static List<String> county(String address) {
        String regex = "(?<county>[^县]+县|.+区|.+市|.+旗|.+海域|.+岛)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String county;
        List<String> table = new ArrayList<>();
        while (m.find()) {
            county = m.group("county");
            if (!StringUtil.isEmpty(county)) {
                table.add(county.trim());
            }
        }
        return table;
    }

    public static List<String> town(String address) {
        String regex = "(?<town>[^区]+区|.+镇)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String town;
        List<String> table = new ArrayList<>();
        while (m.find()) {
            town = m.group("town");
            if (!StringUtil.isEmpty(town)) {
                table.add(town.trim());
            }

        }
        return table;
    }

    public static List<String> village(String address) {
        String regex = "(?<village>.*)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String village;
        List<String> table = new ArrayList<>();
        while (m.find()) {
            village = m.group("village");
            if (!StringUtil.isEmpty(village)) {
                table.add(village.trim());
            }
        }
        return table;
    }

    /**
     * Author: zhangjiahao04 Description: 获取第一个有效的地区 Date: 2022/11/9 12:41 Param: return:
     **/
    public static String regionFirstValid(String address) {
        List<String> province = province(address);
        if (!province.isEmpty()) {
            return province.get(0);
        }
        List<String> city = city(address);
        if (!city.isEmpty()) {
            return city.get(0);
        }
        List<String> county = county(address);
        if (!county.isEmpty()) {
            return county.get(0);
        }
        List<String> town = town(address);
        if (!town.isEmpty()) {
            return town.get(0);
        }
        return address;
    }

    /**
     * byte 与 int 的相互转换
     */
    @SneakyThrows
    public static byte byteFromInt(int x) {
        if (x < -128 || x >= 128) {
            throw new Throwable("超出一字节-128-127限制");
        }
        return (byte) x;
    }

    /**
     * Byte转Bit
     */
    public static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) +
                (byte) ((b >> 6) & 0x1) +
                (byte) ((b >> 5) & 0x1) +
                (byte) ((b >> 4) & 0x1) +
                (byte) ((b >> 3) & 0x1) +
                (byte) ((b >> 2) & 0x1) +
                (byte) ((b >> 1) & 0x1) +
                (byte) ((b) & 0x1);
    }

    /**
     * Bit转Byte
     */
    public static byte byteFromBit(String bitStr) {
        int re, len;
        if (null == bitStr) {
            return 0;
        }
        len = bitStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {
            // 8 bit处理
            if (bitStr.charAt(0) == '0') {
                // 正数
                re = Integer.parseInt(bitStr, 2);
            } else {
                // 负数
                re = Integer.parseInt(bitStr, 2) - 256;
            }
        } else {
            //4 bit处理
            re = Integer.parseInt(bitStr, 2);
        }
        return (byte) re;
    }

    /**
     * Author: zhangjiahao04 Description: 左填充 Date: 2021/12/17 14:10 Param: return:
     **/
    public static String leftPad(String str, int len, char ch) {
        if (str.length() >= len) {
            return str;
        }
        char[] chs = new char[len];
        Arrays.fill(chs, ch);
        char[] src = str.toCharArray();
        System.arraycopy(src, 0, chs, len - src.length, src.length);
        return new String(chs);
    }

    /**
     * 返回左移n位字符串方法
     */
    public static String moveToLeft(String str, int position) {
        String str1 = str.substring(position);
        String str2 = str.substring(0, position);
        return str1 + str2;
    }

    /**
     * 返回右移n位字符串方法
     */
    public static String moveToRight(String str, int position) {
        String str1 = str.substring(str.length() - position);
        String str2 = str.substring(0, str.length() - position);
        return str1 + str2;
    }

    /**
     * Author: zhangjiahao04 Description: snowflakeGenerator雪花算法 Date: 2022/11/9 13:53
     * Param: [workerId, dataCenterId]
     * return: java.lang.Long
     **/
    public static Long snowflakeGenerator(long workerId, long dataCenterId) {
        if (snowflake == null) {
            snowflake = new SnowflakeGenerator(workerId, dataCenterId);
        } else {
            if (workerId != 0 || dataCenterId != 0) {
                snowflake = new SnowflakeGenerator(workerId, dataCenterId);
            }
        }
        return snowflake.next();
    }

    public static Long snowflakeGenerator() {
        if (snowflake == null) {
            snowflake = new SnowflakeGenerator(5, 5);
        }
        return snowflake.next();
    }

}
