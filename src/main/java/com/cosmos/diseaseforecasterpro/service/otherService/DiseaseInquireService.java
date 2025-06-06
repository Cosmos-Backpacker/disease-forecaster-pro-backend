package com.cosmos.diseaseforecasterpro.service.otherService;

import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.fasterxml.jackson.databind.ObjectMapper; // 引入Jackson库解析JSON
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DiseaseInquireService {


    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();


    // 定义字段关键字
    private static final String[] KEYS = {
            "start介绍", "start症状", "start病因", "start并发症", "start推荐食物",
            "start忌食", "start用药", "start预防", "start如何治疗", "start治疗时间",
            "start治好概率", "start易发病人群", "start需要检查项目"
    };

    // 定义问题匹配关键字
    private static final String[] MATCH_KEYS = {
            "是什么病", "的症状是什么", "的病因是什么", "并发症有哪些", "该吃什么", "不能吃什么",
            "该用什么药", "如何预防", "怎么治疗", "疾病要治疗多久", "多大概率能治好", "易发人群",
            "检查项目",
    };


    // 调用查询服务
    public Result diseaseInquire(String question) {
        // 模拟从网络获取的JSON数据
        String jsonResponse = DiseaseInquire(question);

        // 解析JSON数据
        Map<String, String> result = parseJsonResponse(jsonResponse);

        // 初始化结果存储
        Map<String, StringBuilder> resultMap = new LinkedHashMap<>();
        for (String key : KEYS) {
            resultMap.put(key, new StringBuilder());
        }

        // 模拟逐行读取和处理逻辑
        for (Map.Entry<String, String> entry : result.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 根据问题文本内容匹配字段
            for (int i = 0; i < MATCH_KEYS.length; i++) {
                if (key.contains(MATCH_KEYS[i])) {
                    log.info("匹配到字段：{}", KEYS[i]);
                    log.info("匹配到内容：{}", value);
                    resultMap.get(KEYS[i]).append(value); // 添加 "end " 标志
                    break;
                }
            }
        }

        // 将 StringBuilder 转换为 String 并封装到最终结果中
        Map<String, String> finalResult = new LinkedHashMap<>();
        for (Map.Entry<String, StringBuilder> entry : resultMap.entrySet()) {
            finalResult.put(entry.getKey(), entry.getValue().toString());
        }

        log.info("查询结果：{}", finalResult);
        return Result.success("查询成功", finalResult);
    }


    public String DiseaseInquire(String question) {
        String responseBody;

        // 调用 Python 服务
        HashMap<String, String> map = new LinkedHashMap<>();
        String url = "http://localhost:8000/query_disease";

        try {
            // 构建请求体
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, "{\"disease\": \"" + question + "\"}");

            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            // 发送请求并获取响应
            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                responseBody = response.body().string();
                log.info("Python 服务响应：{}", responseBody);
                map.put("result", responseBody);
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Python 服务调用失败，状态码：" + response.code());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage());
        }
        return responseBody;
    }


    public String DiseaseInquireJ(String question) {

        return "查询之后的信息";
    }

    // 解析JSON数据
    private Map<String, String> parseJsonResponse(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 将JSON字符串解析为Map
            Map<String, String> result = objectMapper.readValue(jsonResponse, Map.class);
            return result;
        } catch (Exception e) {
            log.error("解析JSON数据失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
    }


    // 调用聊天机器人服务
    public Result chatbotInquire(String question) {
        log.info("聊天机器人查询：{}", question);
        String jsonResponse = ChatbotInquire(question);

        // 解析 JSON 数据
        Map<String, String> result = parseJsonResponse(jsonResponse);



        log.info("聊天机器人查询结果：{}", result);
        return Result.success("查询成功", result);
    }

    // 调用聊天机器人服务
    public String ChatbotInquire(String question) {
        String responseBody;

        // 调用 Python 服务
        String url = "http://localhost:8005/chat";

        try {
            // 构建请求体
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, "{\"question\": \"" + question + "\"}");

            // 构建请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            // 发送请求并获取响应
            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                responseBody = response.body().string();
                log.info("聊天机器人服务响应：{}", responseBody);
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "聊天机器人服务调用失败，状态码：" + response.code());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage());
        }
        return responseBody;
    }


}
