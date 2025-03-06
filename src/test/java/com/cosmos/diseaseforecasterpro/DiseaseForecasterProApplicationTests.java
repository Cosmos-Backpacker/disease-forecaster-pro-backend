package com.cosmos.diseaseforecasterpro;


import com.cosmos.diseaseforecasterpro.SSE.SseClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
class DiseaseForecasterProApplicationTests {


    @Autowired
    private SseClient sseClient;


    @Test
    void streamResponseTest() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // 流式请求参数（注意stream设置为true）
//        String requestBodyJson = """
//    {
//        "messages": [
//            {
//                "content": "You are a helpful assistant",
//                "role": "system"
//            },
//            {
//                "content": "请介绍一下你自己",
//                "role": "user"
//            }
//        ],
//        "model": "deepseek-chat",
//        "stream": true,
//        "temperature": 0.7
//    }
//    """;


        JsonObject requestBody = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject message1 = new JsonObject();
        message1.addProperty("content", "You are a helpful assistant");
        message1.addProperty("role", "system");
        messages.add(message1);

        JsonObject message2 = new JsonObject();
        message2.addProperty("content", "请介绍一下你自己");
        message2.addProperty("role", "user");
        messages.add(message2);

        requestBody.add("messages", messages);
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("stream", true);
        requestBody.addProperty("temperature", 0.7);

        String requestBodyJson = new Gson().toJson(requestBody);

        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions")
                .post(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer sk-0b7d418731b640659619b65342360fa4")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.err.println("响应失败: " + response);
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    System.err.println("响应体为空");
                    return;
                }

                try (InputStream is = responseBody.byteStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                    String line;
                    StringBuilder contentBuffer = new StringBuilder();
                    System.out.println("开始读取响应...");

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonData = line.substring(6).trim();
                            if (!jsonData.equals("[DONE]")) {
                                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                                JsonArray choices = json.getAsJsonArray("choices");
                                if (!choices.isEmpty()) {
                                    JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                                    if (delta.has("content")) {
                                        String contentChunk = delta.get("content").getAsString();
                                        contentBuffer.append(contentChunk);
                                        System.out.println("发送内容：" + contentChunk);
                                        // 通过 SSE 发送数据到前端
                                        sseClient.sendMessage(11, contentChunk);

                                    }
                                }
                            }
                        }
                    }

                    // 最终完整内容
                    System.out.println("完整内容：\n" + contentBuffer.toString());
                } catch (IOException e) {
                    System.err.println("读取响应失败: " + e.getMessage());
                }
            }
        });
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
