package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.pojo.HealthyQuestions;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IHealthyQuestionsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healthyQuestions")
public class HealthyQuestionsController {

    @Autowired
    private IHealthyQuestionsService healthyQuestionsService;


    @GetMapping("/questions")
    public Result getQuestions() {
        List<HealthyQuestions> questions = healthyQuestionsService.getQuestions();
        log.info("获取所有健康问卷问题成功 {}", questions);
        return Result.success("获取所有健康问卷问题成功", questions);
    }

    @PostMapping("/submit")
    public ResponseEntity<byte[]> submitAnswers(HttpServletRequest req, @RequestBody Map<Integer, Integer> answers) {
        log.info("接收到用户的健康评测答案: {}", answers);
        // 调用服务层生成PDF
        byte[] pdfBytes = healthyQuestionsService.evaluateHealth(req, answers);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "health_report.pdf");
        headers.setContentLength(pdfBytes.length);
        log.info("健康评测成功");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
