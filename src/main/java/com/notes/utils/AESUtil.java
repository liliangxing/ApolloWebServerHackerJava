package com.notes.utils;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * AES基于ECB模式加解密数据帮助类
 *
 * @author Liugl
 * @version 2020/2/19 12:06,Liugl,Ins
 * @copyright 2020/2/19 12:06
 */
public class AESUtil {
    // 算法名称
    static final String KEY_ALGORITHM = "AES";
    // 加解密算法/模式/填充方式
    static final String algorithmStr = "AES/ECB/PKCS7Padding";
    final static Charset charset = Charset.forName("UTF-8");
    private static Key key;
    private static Cipher cipher;

    static {
        //init();
        cipher = getCipher();
    }

    private static Cipher getCipher() {
        // 初始化
        Security.addProvider(new BouncyCastleProvider());
        try {
            // 初始化cipher
            cipher = Cipher.getInstance(algorithmStr, "BC");
            //cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    private static void init(byte[] keyBytes) {
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        //byte[] keyBytes = "123456abcdefghighlmnopqrstuvwxyz".getBytes(charset);
        int base = 16;
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        // 初始化
        Security.addProvider(new BouncyCastleProvider());
        // 转化成JAVA的密钥格式
        key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
    /**
     * 加密方法
     *
     * @param content
     *            要加密的字符串
     * @param keyBytes
     *            加密密钥
     * @return
     */
    public static String encrypt(String content, byte[] keyBytes) {
        byte[] contentBytes = content.getBytes(charset);
        String res = null;
        byte[] encryptedText = null;
        init(keyBytes);
        try {
            synchronized (cipher) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                encryptedText = cipher.doFinal(contentBytes);
            }
            res = new String(Base64.encodeBase64(encryptedText), charset);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    /**
     * 解密方法
     *
     *            要解密的字符串
     * @param keyBytes
     *            解密密钥
     * @return
     */
    public static String decrypt(String content, byte[] keyBytes) {
        byte[] encryptedData = Base64.decodeBase64(content);
        byte[] encryptedText = null;
        init(keyBytes);
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            encryptedText = cipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(encryptedText,charset);
    }


    public static String getTransactionID(String channelUser,String entityKey) {
        //获取当前UTC时间
        final String dateTime = UTCTimeUtil.getUTCTime(UTCTimeUtil.SDF1);
        final String random = UTCTimeUtil.buildRandom(Boolean.TRUE, 4);
        //格式：ChannelUserName|CurrentTimeStamp|RandomNumber
        final String content = String.join("", channelUser, "|", dateTime, "|", random);
        byte[] keyBytes = entityKey.getBytes(charset);
        return  encrypt(content, keyBytes);
    }

    public static void main(String[] args) {
        AESUtil aes = new AESUtil();
//   加解密 密钥
        byte[] keybytes = "123456abcdefghighlmnopqrstuvwxyz".getBytes(charset);
        String content = "1234567|2018-12-20 07:35:15.935|9876";
        // 加密字符串
        System.out.println("加密前的：" + content);
        System.out.println("加密密钥：" + new String(keybytes,charset));
        // 加密方法
        String enc = encrypt(content, keybytes);
        System.out.println("加密后的内容：" + enc);
        // 解密方法
        String dec = decrypt(enc, keybytes);
        System.out.println("解密后的内容：" + dec);

        String plainText = "some text to verify data integrity";
        String encryptedText = encrypt(plainText,keybytes);
        Queue<String> results = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 5000; i++) {

            new Thread( () -> { results.add(encrypt(plainText,keybytes)); })
                    .start();
        }
        try {
            Thread.sleep(5000);
            while(!results.isEmpty()) {
                if(!results.poll().equals(encryptedText)) {
                    System.out.println("加密后的内容：" + results.poll());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
