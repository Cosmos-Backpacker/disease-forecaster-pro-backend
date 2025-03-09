package com.cosmos.diseaseforecasterpro.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("bd.config")
public class BDConfig {

    //有OCR功能的应用
    private String ocrAppId;

    private String ocrSecretKey;

    private String ocrApiKey;


    //有图片增强功能的应用
    private  String enhanceAppId;

    private String enhanceSecretKey;

    private String enhanceApiKey;




}
