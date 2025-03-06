package com.cosmos.diseaseforecasterpro.service.impl;

import com.cosmos.diseaseforecasterpro.DeepSeekBean.Message;
import com.cosmos.diseaseforecasterpro.SSE.SseClient;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Autowired
    private SseClient sseClient;

    @Autowired
    private IUserService iUserService;

    private final Gson gson = new Gson();


    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String deepSeekChat(HttpServletRequest req, String question) {
        //获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);
//        long userId = 11;

        SseEmitter emitter = null;
        emitter = SseClient.getEmitters().get(userId);
        if (emitter == null) {
            //执行对话前先确保连接sse连接存在
            log.error("userId为: {}", userId);

            throw new RuntimeException("sse连接不存在");
        } else {
            log.error("userId为: {}", userId);
            log.error("emitter为: {}", emitter);
        }


        List<Message> userMessages = Message.getAllUserMessageMap().get(userId);
        if (userMessages == null) {
            log.error("userID: {},userMessages不存在", userId);
            //创建集合
            userMessages = new ArrayList<>();
            //将集合放入map
            Message.getAllUserMessageMap().put(userId, userMessages);

        }else {
            log.error("存在");
            System.out.println(userMessages.toString());
        }

        //获取用户的与AI对话的集合,如果不存在自动创建空的集合
        //List<Message> userMessages = Message.getAllUserMessageMap().computeIfAbsent(userId, k -> new ArrayList<Message>());


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


        /**
         * todo:优化组装请求Message，当对话过多的时候将最先放入的几条移除以免每次询问都会包含第一次内容
         */
        //如果没有创建系统设置就创建系统设置，创建了之后就不需要
        if (userMessages.isEmpty() || !userMessages.get(0).getRole().equals("system"))
            userMessages.add(Message.createSystemMessage("你是一个助手，请回答用户的问题"));

        //创建用户问题
        userMessages.add(Message.createUserMessage(question));

        //将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();


        JsonObject requestBody = new JsonObject();
//        JsonArray messages = new JsonArray();

        JsonObject message1 = new JsonObject();


//        message1.addProperty("content", "你是一个模仿者，你只需要模仿用户说的话即可，不能回复别的内容");
//        message1.addProperty("role", "system");
//        messages.add(message1);
//
//        JsonObject message2 = new JsonObject();
//        message2.addProperty("content", "请介绍一下你自己");
//        message2.addProperty("role", "user");
//        messages.add(message2);

        requestBody.add("messages", messages);
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("stream", true);
        requestBody.addProperty("temperature", 0.7);


        String requestBodyJson = new Gson().toJson(requestBody);

        log.error("请求体: {}", requestBodyJson);


        // 构造请求体
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions") // 替换为实际的API URL
                .post(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer sk-0b7d418731b640659619b65342360fa4") // 替换为实际的API密钥
                .build();


        //异步调用deepSeek
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    sseClient.sendMessage(userId, null, "请求失败: " + e.getMessage());

                } finally {
                    sseClient.completeWithError(userId, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    try {

                        sseClient.sendMessage(userId, "响应失败: " + response);
                    } finally {
                        sseClient.completeWithError(userId, new RuntimeException("响应失败"));
                    }
                    return;
                }

                ResponseBody responseBody = response.body();

                if (responseBody == null) {
                    try {
                        sseClient.sendMessage(userId, "响应体为空");

                    } finally {
                        sseClient.completeWithError(userId, new RuntimeException("响应体为空"));

                    }
                    return;
                }

                try (InputStream is = responseBody.byteStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                    String line;
                    StringBuilder contentBuffer = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonData = line.substring(6);
                            if (!jsonData.equals("[DONE]")) {
                                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                                JsonArray choices = json.getAsJsonArray("choices");
                                if (!choices.isEmpty()) {
                                    JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                                    if (delta.has("content")) {
                                        String contentChunk = delta.get("content").getAsString();
                                        //System.out.println("发送内容：" + contentChunk);
                                        contentBuffer.append(contentChunk);
                                        // 通过 SSE 发送数据到前端
                                        sseClient.sendMessage(userId, contentChunk.replace(" ","[SPC]"));
                                    }
                                }
                            }
                        }
                    }

                    try {

                        //发送完整的数据
                        //sseClient.sendMessage(userId, "完整内容：\n" + contentBuffer.toString());
                        sseClient.sendMessage(userId, "[DONE]");
                        //存储AI回答的内容
                        //userMessages.add(Message.createAssistantMessage(contentBuffer.toString()));
                        //更新map

                    } finally {
                        //关闭连接
                        //sseClient.closeSse(userId);
                        Message.recordMessage(userId, Message.createAssistantMessage(contentBuffer.toString()));
                        List<Message> temp = Message.getAllUserMessageMap().get(userId);
                        log.error("存入成功: {}", temp.toString());

                    }
                } catch (IOException e) {
                    try {
                        sseClient.sendMessage(userId, "读取响应失败: " + e.getMessage());
                    } finally {
                        sseClient.completeWithError(userId, e);
                    }
                }
            }
        });

        return "响应成功";

    }


}
