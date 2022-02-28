package com.sansi.data.utils;

import com.sansi.data.entity.RequestInfo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 南京飞博签名生成工具类
 */
public class SignUtil {

    public static String forSignString;
    /**
     * 构造签名字符串
     *
     * @param requestInfo 请求对象
     * @return ⼤于 0 执⾏时产⽣的业务异常
     * 1.1.2 签名示例
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String buildSign(RequestInfo requestInfo) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String sortString = "appid=" + requestInfo.getAppid();
        sortString += "&data=" + requestInfo.getData();
        sortString += "&format=" + requestInfo.getFormat();
        sortString += "&method=" + requestInfo.getMethod();
        sortString += "&nonce=" + requestInfo.getNonce();
        sortString += "&timestamp=" + requestInfo.getTimestamp();
        sortString += "&version=" + requestInfo.getVersion();
        forSignString = sortString + "&appsecret=" + requestInfo.getAppSecret();
        forSignString = forSignString.toLowerCase();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] bytes = md5.digest(forSignString.getBytes("utf-8"));
        String sign = convertBinaryToHexValueString(bytes);
        return sign.toLowerCase();
    }

    /**
     * 转换⼆进制位16进制字符串
     *
     * @param bytes
     * @return
     */
    private static String convertBinaryToHexValueString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return "";
        }
        for (byte b : bytes) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }

        return stringBuilder.toString();
    }
}

