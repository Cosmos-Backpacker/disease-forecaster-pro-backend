package com.cosmos.diseaseforecasterpro.service.otherService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
public class SiZhiRobot {

    public String getAnswer(String issue) {
        String errorTip = "出错了，请联系管理员处理";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "https://api.ownthink.com/bot?appid=686ad2b95029592a5311d0ec5edcbca7&userid=user&spoken=" + issue;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // 使用 Gson 解析 JSON 数据
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonObject dataJson = jsonObject.getAsJsonObject("data");
                JsonObject infoJson = dataJson.getAsJsonObject("info");

                return infoJson.get("text").getAsString();
            } else {
                System.out.println("请求失败，错误码：" + response.code());
                return "请求失败，错误码：" + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorTip;
    }
}
