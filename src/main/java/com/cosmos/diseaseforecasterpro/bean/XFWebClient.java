package com.cosmos.diseaseforecasterpro.bean;

import com.cosmos.diseaseforecasterpro.bean.ImageUnderstand.XfMessageImgU;
import com.cosmos.diseaseforecasterpro.config.XFConfig;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * @Author: CosmosBackpacker
 * @CreateTime: 2023-10-19  11:04
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Component
public class XFWebClient {

    @Autowired
    private XFConfig xfConfig;


    public Gson gson = new Gson();


    /**
     * 通用鉴权方法
     *
     * @param hostUrl   域 名
     * @param apiKey    应 用ID
     * @param apiSecret 应 用密 钥
     * @return 鉴权url
     * @throws Exception 异常
     */
    public static String getAuthUrl_WebSocket(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));

        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }


//=======================================图片理解=========================================

//鉴权方法和大模型方法一致

    //构造请求体参数


    public JsonObject createRequestParams_Image(String uid, List<XfMessageImgU> questions) {
        JsonObject requestJson = new JsonObject();
        Gson gson = new Gson();

        // header参数
        JsonObject header = new JsonObject();
        header.addProperty("app_id", xfConfig.getAppId());
        header.addProperty("uid", uid);

        // parameter参数
        JsonObject parameter = new JsonObject();
        JsonObject chat = new JsonObject();
        chat.addProperty("domain", "imagev3"); // 修改domain值
        chat.addProperty("temperature", 0.5);
        chat.addProperty("top_k", 4); // 添加top_k参数
        chat.addProperty("max_tokens", 2028); // 修改max_tokens值
        parameter.add("chat", chat);

        // payload参数
        JsonObject payload = new JsonObject();
        JsonObject message = new JsonObject();
        JsonArray text = new JsonArray();

        // 将questions中的内容添加到text数组中
        for (XfMessageImgU question : questions) {
            JsonObject questionJson = new JsonObject();
            questionJson.addProperty("role", question.getRole()); // 假设XfMessageImgU有getRole方法
            questionJson.addProperty("content", question.getContent()); // 假设XfMessageImgU有getContent方法
            questionJson.addProperty("content_type", question.getContentType()); // 假设XfMessageImgU有getContentType方法
            text.add(questionJson);
        }

        message.add("text", text);
        payload.add("message", message);

        // 组装最终的请求参数
        requestJson.add("header", header);
        requestJson.add("parameter", parameter);
        requestJson.add("payload", payload);

        System.out.println(requestJson.toString());

        return requestJson;
    }


    //====================================建立连接并发送消息函数==========================================
    public WebSocket sendImageMessage(String uid, ArrayList<XfMessageImgU> questions, WebSocketListener listener) {
        // 获取鉴权url
        String authUrl = null;
        try {
            authUrl = getAuthUrl_WebSocket(xfConfig.getHostUrlImageU(), xfConfig.getApiKey(), xfConfig.getApiSecret());
        } catch (Exception e) {
            log.error("鉴权失败：{}", e);
            return null;
        }
        // 鉴权方法生成失败，直接返回 null
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        // 将 https/http 连接替换为 ws/wss 连接
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();
        // 建立 wss 连接
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        // 组装请求参数
        JsonObject requestDTO = createRequestParams_Image(uid, questions);

// 将JsonObject转换为JSON字符串
        String requestJsonString = gson.toJson(requestDTO);
        // 发送请求
        webSocket.send(requestJsonString);


        return webSocket;

    }


    //=======================================图片生成=========================================
    //http通用鉴权
    public static String getAuthUrl_ImageG(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // date="Thu, 12 Oct 2023 03:05:28 GMT";
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "POST " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }


    //里面可以更改生成的图片的大小
    public String createRequestParams_ImageG(String uid, String content) throws IOException {
        String params = "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + xfConfig.getAppId() + "\",\n" +
                "    \"uid\": \"" + uid + "\"\n" +
                "  },\n" +
                "  \"parameter\": {\n" +
                "    \"chat\": {\n" +
                "      \"domain\": \"s291394db\",\n" +
                "      \"temperature\": 0.5,\n" +
                "      \"max_tokens\": 4096,\n" +
                "      \"width\": 512,\n" +
                "      \"height\": 512\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"message\": {\n" +
                "      \"text\": [\n" +
                "        {\n" +
                "          \"role\": \"user\",\n" +
                "          \"content\": \"" + content + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        return params;
    }

    //=======================图片生成请求方法===============


    public String ImageGenerationRequest(String uid, String content) {
        String authUrl = null;
        String resultString = "";

        try {
            authUrl = getAuthUrl_ImageG(xfConfig.getHostUrlImageG(), xfConfig.getApiKey(), xfConfig.getApiSecret());
            OkHttpClient client = new OkHttpClient(); // 创建OkHttpClient实例

            // 创建请求内容
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    createRequestParams_ImageG(uid, content).toString()
            );

            // 创建请求对象
            Request request = new Request.Builder()
                    .url(authUrl) // 设置请求URL
                    .post(requestBody) // 设置POST请求体
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    resultString = response.body().string(); // 获取响应内容
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resultString;
    }
}


