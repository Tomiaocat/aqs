package com.httpserver.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

/**
 * AES加密工具类
 * 服务端和客户端共用同一套加密逻辑
 */
public class AesUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 16; // AES-128

    /**
     * 密钥派生: MD5后取前16字节
     */
    private static byte[] deriveKey(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(hash, KEY_LENGTH);
    }

    /**
     * 加密: 返回 Base64(IV + ciphertext)
     */
    public static String encrypt(String plaintext, String key) throws Exception {
        byte[] keyBytes = deriveKey(key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 随机IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 加密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 拼接IV和密文
        byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 解密: 输入 Base64(IV + ciphertext)
     */
    public static String decrypt(String encryptedBase64, String key) throws Exception {
        byte[] keyBytes = deriveKey(key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

        // 提取IV和密文
        byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);

        // 解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(ciphertext);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * JSON最外层value加密
     */
    public static String encryptJsonValues(String jsonString, String key) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonString);
        ObjectNode result = mapper.createObjectNode();

        if (root.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String encrypted = encrypt(entry.getValue().toString(), key);
                result.put(entry.getKey(), encrypted);
            }
        }
        return result.toString();
    }

    /**
     * JSON最外层value解密
     */
    public static String decryptJsonValues(String encryptedJson, String key) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encryptedJson);
        ObjectNode result = mapper.createObjectNode();

        if (root.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String decrypted = decrypt(entry.getValue().asText(), key);
                // 尝试解析为JSON，失败则作为字符串
                try {
                    result.set(entry.getKey(), mapper.readTree(decrypted));
                } catch (Exception e) {
                    result.put(entry.getKey(), decrypted);
                }
            }
        }
        return result.toString();
    }
}
