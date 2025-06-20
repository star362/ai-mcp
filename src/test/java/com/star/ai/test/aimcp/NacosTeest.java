package com.star.ai.test.aimcp;
import java.security.SecureRandom;
import java.util.Base64;



public class NacosTeest {

    public static void main(String[] args) {
        // 生成 32 字节的随机数据
        byte[] randomBytes = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        // 将随机字节数组进行 Base64 编码
        String base64EncodedSecretKey = Base64.getEncoder().encodeToString(randomBytes);

        // 输出 Base64 编码后的 secret key
        System.out.println("Base64 Encoded Secret Key: " + base64EncodedSecretKey);
    }
}
