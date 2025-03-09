package com.cosmos.diseaseforecasterpro.utils;

import cn.xfyun.api.IatClient;
import cn.xfyun.model.response.iat.IatResponse;
import cn.xfyun.model.response.iat.IatResult;
import cn.xfyun.service.iat.AbstractIatWebSocketListener;
import com.cosmos.diseaseforecasterpro.config.XFConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ASR {

    @Autowired
    private XFConfig xfConfig;

    public CompletableFuture<String> convertSpeechToTextAsync(File audioFile) throws FileNotFoundException, SignatureException, MalformedURLException {


        IatClient iatClient = new IatClient.Builder()
                .signature(xfConfig.getAppId(), xfConfig.getApiKey(), xfConfig.getApiSecret())
                .build();

        StringBuffer finalResult = new StringBuffer();
        CompletableFuture<String> future = new CompletableFuture<>();

        iatClient.send(audioFile, new AbstractIatWebSocketListener() {
            @Override
            public void onSuccess(WebSocket webSocket, IatResponse iatResponse) {
                if (iatResponse.getCode() != 0) {
                    System.out.println("code=>" + iatResponse.getCode() + " error=>" + iatResponse.getMessage() + " sid=" + iatResponse.getSid());
                    System.out.println("错误码查询链接：https://www.xfyun.cn/document/error-code");
                    future.completeExceptionally(new RuntimeException(iatResponse.getMessage()));
                    return;
                }

                if (iatResponse.getData() != null) {
                    if (iatResponse.getData().getResult() != null) {
                        IatResult.Ws[] wss = iatResponse.getData().getResult().getWs();
                        String text = "";
                        for (IatResult.Ws ws : wss) {
                            IatResult.Cw[] cws = ws.getCw();

                            for (IatResult.Cw cw : cws) {
                                text += cw.getW();
                            }
                        }

                        try {
                            finalResult.append(text);
                            System.out.println("中间识别结果 ==》" + text);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }

                    if (iatResponse.getData().getStatus() == 2) {
                        // resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
                        System.out.println("session end ");
                        iatClient.closeWebsocket();
                        future.complete(finalResult.toString());
                    } else {
                        // 根据返回的数据处理
                        //System.out.println(StringUtils.gson.toJson(iatResponse));
                    }
                }
            }

            @Override
            public void onFail(WebSocket webSocket, Throwable t, Response response) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }
}
