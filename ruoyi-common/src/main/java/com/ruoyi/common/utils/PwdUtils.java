package com.ruoyi.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 密码加解密工具类
 * 用于保护配置文件中的敏感信息
 *
 * 注意：此类的代码会被 ClassFinal 加密保护
 *
 * @author ruoyi
 */
public class PwdUtils
{
    /**
     * AES密钥（16位）- 此密钥会被 ClassFinal 加密保护
     * 如需更换密钥，请修改此值并重新生成所有加密密码
     */
    private static final String SECRET_KEY = "RuoYi@WMS#2026!F";

    /**
     * AES初始向量（16位）
     */
    private static final String IV_PARAMETER = "FreeBranch@2026!";

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 加密密码
     *
     * @param plainPassword 明文密码
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String plainPassword)
    {
        if (plainPassword == null || plainPassword.isEmpty())
        {
            return plainPassword;
        }
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV_PARAMETER.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 解密密码
     *
     * @param encryptedPassword 加密后的Base64字符串
     * @return 明文密码
     */
    public static String decrypt(String encryptedPassword)
    {
        if (encryptedPassword == null || encryptedPassword.isEmpty())
        {
            return encryptedPassword;
        }
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV_PARAMETER.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new RuntimeException("密码解密失败，请检查密文是否正确", e);
        }
    }

    /**
     * 判断字符串是否为加密后的密码（以 ENC( 开头，以 ) 结尾）
     *
     * @param password 密码字符串
     * @return 是否为加密密码
     */
    public static boolean isEncrypted(String password)
    {
        return password != null && password.startsWith("ENC(") && password.endsWith(")");
    }

    /**
     * 解密密码（自动判断是否需要解密）
     * 如果密码格式为 ENC(xxx)，则解密；否则直接返回原值
     *
     * @param password 密码字符串
     * @return 明文密码
     */
    public static String decryptIfNeeded(String password)
    {
        if (isEncrypted(password))
        {
            // 去掉 ENC() 包装
            String encrypted = password.substring(4, password.length() - 1);
            return decrypt(encrypted);
        }
        return password;
    }

    /**
     * 生成加密密码（用于开发时生成密文）
     * 使用方法：运行此类的main方法
     *
     * @param args 命令行参数，第一个参数为要加密的明文密码
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            String plain = args[0];
            String encrypted = encrypt(plain);
            System.out.println("========================================");
            System.out.println("明文密码: " + plain);
            System.out.println("密文密码: ENC(" + encrypted + ")");
            System.out.println("========================================");
            System.out.println("请将以下内容复制到配置文件中:");
            System.out.println("password: ENC(" + encrypted + ")");
            System.out.println("========================================");
        }
        else
        {
            System.out.println("使用方法: java PwdUtils <明文密码>");
            System.out.println("示例: java PwdUtils 123456");
        }
    }
}
