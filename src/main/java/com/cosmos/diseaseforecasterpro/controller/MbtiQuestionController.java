package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiAnswer;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiQuestion;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiResult;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IMbtiQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/mbtiTest")
public class MbtiQuestionController {

    @Autowired
    private IMbtiQuestionService mbtiService;

    // 获取所有MBTI问题
    @GetMapping("/questions")
    public Result getQuestions(@RequestParam(value = "option") String option) {
        log.error(option);
        List<MbtiQuestion> questions = mbtiService.getAllQuestions();
        // 返回json结果
        return Result.success("获取所有MBTI问题成功", questions);
    }

    // 提交用户答案并获取人格结果
    @PostMapping("/submit")
    public Result submitAnswers(HttpServletRequest req, @RequestBody List<MbtiAnswer> userAnswers) {
        if (userAnswers == null || userAnswers.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案不能为空");
        }

        MbtiResult result = mbtiService.evaluatePersonality(req, userAnswers);
        log.info("人格结果为 {}", result);
        // 返回json结果
        return Result.success("人格测试完成", result);
    }


}
