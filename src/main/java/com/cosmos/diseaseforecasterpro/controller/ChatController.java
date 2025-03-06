package com.cosmos.diseaseforecasterpro.controller;

import com.cosmos.diseaseforecasterpro.SSE.SseClient;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import okhttp3.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static com.cosmos.diseaseforecasterpro.service.impl.UserServiceImpl.USER_LOGIN_STATE;

@CrossOrigin(origins = "*") // 允许任意前端地址访问
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private IChatService chatService;


    @GetMapping("/DeepSeek")
    public Result streamResponse(HttpServletRequest req, String question) {
        //直接执行方法即可
        return Result.success(chatService.deepSeekChat(req, question));
    }


}
