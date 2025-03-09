package com.cosmos.diseaseforecasterpro.utils;

import cn.xfyun.api.TtsClient;
import cn.xfyun.model.response.TtsResponse;
import cn.xfyun.service.tts.AbstractTtsWebSocketListener;
import cn.xfyun.util.StringUtils;
import com.cosmos.diseaseforecasterpro.config.XFConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.xfyun.model.response.TtsResponse;

import java.io.*;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class TTS {
    @Autowired
    private XFConfig xfConfig;
    private File f;
    private FileOutputStream os;

    public CompletableFuture<Void> convertTextToSpeech(String text, File outputFile) throws MalformedURLException, SignatureException, UnsupportedEncodingException, FileNotFoundException {
        TtsClient ttsClient = new TtsClient.Builder()
                .signature(xfConfig.getAppId(), xfConfig.getApiKey(), xfConfig.getApiSecret())
                .vcn("xiaoyan")
                .build();

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            ttsClient.send(text, new AbstractTtsWebSocketListener(outputFile) {


                @Override
                public void onSuccess(byte[] bytes) {


                    future.complete(null); // 标记未来完成
                }

                @Override
                public void onFail(WebSocket webSocket, Throwable throwable, Response response) {
                    log.error("合成失败: {}", throwable.getMessage());
                    future.completeExceptionally(throwable); // 传递异常
                }

                @Override
                public void onBusinessFail(WebSocket webSocket, TtsResponse ttsResponse) {
                    log.error("业务失败: {}", ttsResponse.toString());
                    future.completeExceptionally(new Exception(ttsResponse.toString()));
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e); // 捕获并传递异常
        }

        return future;
    }
}
