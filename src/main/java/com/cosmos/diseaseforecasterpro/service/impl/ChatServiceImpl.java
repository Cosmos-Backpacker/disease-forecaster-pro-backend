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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Autowired
    private SseClient sseClient;

    @Autowired
    private IUserService iUserService;

    private final Gson gson = new Gson();

    public String systemMessage = "你是一个助手，请回答用户的问题";


    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();


    @Override
    public String deepSeekChat(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一个助手，请回答用户问题") String systemMessage, @RequestParam(required = false, defaultValue = "这是检索出来的专业医疗数据")String retrievedInfo) {

        //获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);

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

        } else {
            log.error("存在");
            System.out.println(userMessages.toString());
        }


        /**
         * todo:优化组装请求Message，当对话过多的时候将最先放入的几条移除以免每次询问都会包含第一次内容
         */

        //如果没有创建系统设置就创建系统设置，创建了之后就不需要
        if (userMessages.isEmpty() || !userMessages.get(0).getRole().equals("system"))
            userMessages.add(Message.createSystemMessage(systemMessage));

        //创建用户问题
        userMessages.add(Message.createUserMessage(question));

        //将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();


        JsonObject requestBody = new JsonObject();

        JsonObject message1 = new JsonObject();


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
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
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
                                        sseClient.sendMessage(userId, contentChunk.replace(" ", "[SPC]"));
                                    }
                                }
                            }
                        }
                    }

                    try {
                        //发送完整的数据
                        //sseClient.sendMessage(userId, "完整内容：\n" + contentBuffer.toString());
                        sseClient.sendMessage(userId, "[DONE]");
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


    // 同步调用deepSeek日程安排功能专用
    @Override
    public String deepSeekScheduleChatSync(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一个助手，请回答用户问题") String systemMessage) {
        // 获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);

        List<Message> userMessages = Message.getAllUserScheduleMessageMap().get(userId);
        if (userMessages == null) {
            log.error("userID: {},userMessages不存在", userId);
            // 创建集合
            userMessages = new ArrayList<>();
            // 将集合放入map
            Message.getAllUserScheduleMessageMap().put(userId, userMessages);
        } else {
            log.error("存在");
            System.out.println(userMessages.toString());
        }

        // 如果没有创建系统设置就创建系统设置，创建了之后就不需要
        if (userMessages.isEmpty() || !userMessages.get(0).getRole().equals("system"))
            userMessages.add(Message.createSystemMessage(systemMessage));

        // 创建用户问题
        userMessages.add(Message.createUserMessage(question));
        log.error(question);

        // 将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("stream", false); // 设置为false以获取同步响应
        requestBody.addProperty("temperature", 0.7);

        String requestBodyJson = new Gson().toJson(requestBody);

        log.error("请求体: {}", requestBodyJson);

        // 构造请求体
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions") // 替换为实际的API URL
                .post(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer sk-0b7d418731b640659619b65342360fa4") // 替换为实际的API密钥
                .build();

        // 同步调用deepSeek
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }

            String responseBodyString = responseBody.string();
            JsonObject jsonResponse = JsonParser.parseString(responseBodyString).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                String res = message.get("content").getAsString();
                Message.recordScheduleMessage(userId, Message.createAssistantMessage(res));
                return res;
            } else {
                throw new IOException("No choices in response");
            }
        } catch (IOException e) {
            log.error("Error calling deepSeekChatSync", e);
            throw new RuntimeException("Error calling deepSeekChatSync", e);
        }

    }


    /**
     * 同步调用deepSeek，不保存任何记录版本
     *
     * @param req
     * @param question
     * @return
     */
    @Override
    public String deepSeekChatTongYongSync(HttpServletRequest req, String question) {

        // 获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);
        List<Message> userMessages = new ArrayList<>();
        // 如果没有创建系统设置就创建系统设置，创建了之后就不需要
        userMessages.add(Message.createSystemMessage(systemMessage));
        // 创建用户问题
        userMessages.add(Message.createUserMessage(question));
        log.error(question);
        // 将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("stream", false); // 设置为false以获取同步响应
        requestBody.addProperty("temperature", 0.7);
        String requestBodyJson = new Gson().toJson(requestBody);
        log.error("请求体: {}", requestBodyJson);

        // 构造请求体
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions") // 替换为实际的API URL
                .post(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer sk-0b7d418731b640659619b65342360fa4") // 替换为实际的API密钥
                .build();

        // 同步调用deepSeek
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }

            String responseBodyString = responseBody.string();
            JsonObject jsonResponse = JsonParser.parseString(responseBodyString).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new IOException("No choices in response");
            }
        } catch (IOException e) {
            log.error("Error calling deepSeekChatSync", e);
            throw new RuntimeException("Error calling deepSeekChatSync", e);
        }
    }


    @Override
    public String deepSeekVoiceChatSync(HttpServletRequest req, String question) {

        // 获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);
        List<Message> userMessages = Message.getAllUserVoiceMessageMap().get(userId);
        if (userMessages == null) {
            log.error("userID: {},userMessages不存在", userId);
            // 创建集合
            userMessages = new ArrayList<>();
            // 将集合放入map
            Message.getAllUserVoiceMessageMap().put(userId, userMessages);
        } else {
            log.error("存在");
            System.out.println(userMessages.toString());
        }

        // 如果没有创建系统设置就创建系统设置，创建了之后就不需要
        if (userMessages.isEmpty() || !userMessages.get(0).getRole().equals("system"))
            userMessages.add(Message.createSystemMessage("你是一个排忧解难小助手，请陪用户聊天，但是每次回答要一到两句话不要过长,随着聊天深入回答可以稍微加上一点"));

        // 创建用户问题
        userMessages.add(Message.createUserMessage(question));
        log.error(question);
        // 将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("stream", false); // 设置为false以获取同步响应
        requestBody.addProperty("temperature", 0.7);

        String requestBodyJson = new Gson().toJson(requestBody);

        log.error("请求体: {}", requestBodyJson);

        // 构造请求体
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions") // 替换为实际的API URL
                .post(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer sk-0b7d418731b640659619b65342360fa4") // 替换为实际的API密钥
                .build();

        // 同步调用deepSeek
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }

            String responseBodyString = responseBody.string();
            JsonObject jsonResponse = JsonParser.parseString(responseBodyString).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                String res = message.get("content").getAsString();
                Message.recordVoiceMessage(userId, Message.createAssistantMessage(res));
                return res;
            } else {
                throw new IOException("No choices in response");
            }
        } catch (IOException e) {
            log.error("Error calling deepSeekChatSync", e);
            throw new RuntimeException("Error calling deepSeekChatSync", e);
        }
    }


    @Override
    public String psyHealthDeepSeekChat(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一名心理医生，不论用户说什么请你根据他所说的内容进一步的对他提问要用关怀的语气,直接询问用户的一个问题即可，不可以多说其他的内容") String systemMessage) {

        //获取用户id，用户id就是sse连接id
        Long userId = iUserService.getUserId(req);
        log.error("userId为: {}", userId);

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

        List<Message> userMessages = Message.getAllUserPsyHealthMessageMap().get(userId);
        if (userMessages == null) {
            log.error("userID: {},userMessages不存在", userId);
            //创建集合
            userMessages = new ArrayList<>();
            //将集合放入map
            Message.getAllUserPsyHealthMessageMap().put(userId, userMessages);

        } else {
            log.error("存在");
            System.out.println(userMessages.toString());
        }


        /**
         * todo:优化组装请求Message，当对话过多的时候将最先放入的几条移除以免每次询问都会包含第一次内容
         */

        //如果没有创建系统设置就创建系统设置，创建了之后就不需要
        if (userMessages.isEmpty() || !userMessages.get(0).getRole().equals("system"))
            userMessages.add(Message.createSystemMessage(systemMessage));

        //创建用户问题
        userMessages.add(Message.createUserMessage(question));

        //将集合转换成json
        JsonArray messages = gson.toJsonTree(userMessages).getAsJsonArray();


        JsonObject requestBody = new JsonObject();

        JsonObject message1 = new JsonObject();


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
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
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
                                        sseClient.sendMessage(userId, contentChunk.replace(" ", "[SPC]"));
                                    }
                                }
                            }
                        }
                    }

                    try {
                        //发送完整的数据
                        //sseClient.sendMessage(userId, "完整内容：\n" + contentBuffer.toString());
                        sseClient.sendMessage(userId, "[DONE]");
                    } finally {
                        //关闭连接
                        //sseClient.closeSse(userId);
                        Message.recordPsyHealthMessage(userId, Message.createAssistantMessage(contentBuffer.toString()));
                        List<Message> temp = Message.getAllUserPsyHealthMessageMap().get(userId);
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


    @Override
    public String extractInfo(HttpServletRequest req, String info) {

        //模拟提取关键信息
        return "心脏病，血压高";
    }


}
