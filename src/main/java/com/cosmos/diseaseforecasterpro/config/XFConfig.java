package com.cosmos.diseaseforecasterpro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author: Cosmos
 * @CreateTime: 2023-10-17  13:58
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@Component
@ConfigurationProperties("xf.config")
public class XFConfig {
    private String appId;
    private String apiSecret;
    private String apiKey;

    private String hostUrlWebsocket;  //大模型接口

    private String hostUrlOcr;         //OCR调用接口

    private Integer maxResponseTime;

    private String hostUrlImageU;    //图片理解接口

    private String hostUrlImageG;    //图片生成接口


}
