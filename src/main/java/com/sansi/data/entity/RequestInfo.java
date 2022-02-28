package com.sansi.data.entity;


import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 南京飞博请求对象
 */
@Data
@Component
public class RequestInfo {
    //供应商appId
    private String appid;
    //供应商appSecret
    private String appSecret;
    //json数据字符串
    private String data;
    //格式，json
    private String format;
    //接⼝标识符，⽐如upload.envMonitor
    private String method;
    //随机数
    private String nonce;
    //时间戳
    private String timestamp;
    //版本，⽐如1.0
    private String version;
}
