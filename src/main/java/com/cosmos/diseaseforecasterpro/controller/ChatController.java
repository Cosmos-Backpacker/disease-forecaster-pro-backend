package com.cosmos.diseaseforecasterpro.controller;

import com.cosmos.diseaseforecasterpro.SSE.SseClient;
import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import com.cosmos.diseaseforecasterpro.service.otherService.SiZhiRobot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private SiZhiRobot robot = new SiZhiRobot();


    @GetMapping("/DeepSeek")
    public Result usualDeepChat(HttpServletRequest req, String question) {
        //对系统进行设置
        String systemMessage = "你是一个助手，请你回答用户问题";
        //直接执行方法即可
        return Result.success(chatService.deepSeekChat(req, question, systemMessage));
    }


    @PostMapping("/scheduleDeepSeek")
    public Result scheduleDeepSeek(String question, HttpServletRequest req) {
        log.error(question);
        if (StringUtils.isBlank(question))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");

        //对系统进行设置
        String systemMessage = "你是一个日程安排小能手，现在请你根据用户的问题，生成一个日程安排，日程安排的格式为后面的模板部分，";
        String model = "日程安排模版如下,必须严格按照模版的格式给我返回结果，其中description部分的内容是对taskName的具体描述：" +
                "模版为：" +
                "计划名1:计划描述1\n" +
                "计划名2:计划描述2\n" +
                "计划名3:计划描述3\n" +
                "等等";


        //直接执行方法即可
        return Result.success("响应成功", chatService.deepSeekScheduleChatSync(req, question, systemMessage + model));
    }


    @GetMapping("/psyHealthDeepSeek")
    public Result psyHealthDeepSeek(HttpServletRequest req, String question) {
        //对系统进行设置
        String systemMessage = "你是一名心理医生，不论用户说什么请你根据他所说的内容进一步的对他提问要用关怀的语气,直接询问用户的一个问题即可，不可以多说其他的内容，再询问了2到3次问题之后，根据用户所述内容给出心理学分析，以及建议，可以用到一些专业的心理学术语";
        //直接执行方法即可
        return Result.success(chatService.psyHealthDeepSeekChat(req, question, systemMessage));
    }


    @GetMapping("/chatRobot")
    public Result chatRobot(String issue) {
        //因为只有这一个功能所以就直接在这里封装了

        String answer = robot.getAnswer(issue);

        return Result.success("success", answer);
    }


}
