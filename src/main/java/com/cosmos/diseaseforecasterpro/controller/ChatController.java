package com.cosmos.diseaseforecasterpro.controller;

import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.otherService.DiseaseInquireService;
import com.cosmos.diseaseforecasterpro.service.otherService.SiZhiRobot;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // 允许任意前端地址访问
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private IChatService chatService;

    @Autowired
    private SiZhiRobot robot = new SiZhiRobot();

    @Autowired
    private DiseaseInquireService diseaseInquireService;

    @GetMapping("/DeepSeek")
    public Result usualDeepChat(HttpServletRequest req, String question) {
        //对系统进行设置
        String systemMessage = "你是医疗专家，精通各种疾病药物等知识，请你回答用户的问题";

        //分析提取用户信息中的关键因素
        String importantInfo = chatService.extractInfo(req, question);

        //检索专业信息
        String retrievedInfo = diseaseInquireService.DiseaseInquireJ(importantInfo);

        //融合生成结果
        return Result.success(chatService.deepSeekChat(req, question, systemMessage, retrievedInfo));
    }


    @PostMapping("/scheduleDeepSeek")
    public Result scheduleDeepSeek(String question, HttpServletRequest req) {
        log.error(question);
        if (StringUtils.isBlank(question))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");

        //对系统进行设置
        String systemMessage = "你是一个日程安排小能手，现在请你根据用户的问题，生成一个日程安排，日程安排的格式为后面的模板部分，";
        String model = "日程安排模版如下,必须严格按照模版的格式给我返回结果，其中description部分的内容是对taskName的具体描述：同时不要出现类似于12:00这样的具体时间，" +
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
        String systemMessage = "你是一名心理医生，不论用户说什么请你根据他所说的内容进一步的对他提问要用关怀的语气,要主动询问用户的相关问题，而且每次只能提问一个问题，不可以多说其他的内容，问题分为以下5个基础分类，并采用「覆盖率+权重」计算问诊度\n" +
                "\n" +
                "1. **情绪状态**\n" +
                "2. **压力源**\n" +
                "3. **人际关系**\n" +
                "4. **生理基础**\n" +
                "5. **自我认知**\n" +
                "然后根据用户的回答进行计分，我们采用双维度计分，每类问题首次回答+1分，然后根据回答详细程度+0.5~1分（如描述具体事例/感受）当总问诊度达标=所有分类均≥1.5分（可调整）的时候结束主动问询\n" +
                "\n然后根据用户所述内容给出心理学分析，以及建议，可以用到一些专业的心理学术语，再回答过程中不要提到问诊度等相关信息";
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
