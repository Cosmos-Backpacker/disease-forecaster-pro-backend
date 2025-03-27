package com.cosmos.diseaseforecasterpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cosmos.diseaseforecasterpro.pojo.HealthyQuestions;
import com.cosmos.diseaseforecasterpro.mapper.HealthyQuestionsMapper;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.IHealthyQuestionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cosmos.diseaseforecasterpro.utils.ConvertTextToPdf;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HealthyQuestionsServiceImpl extends ServiceImpl<HealthyQuestionsMapper, HealthyQuestions> implements IHealthyQuestionsService {

    // 维度配置枚举
    private enum HealthDimension {
        BODY_ACTIVITY("身体活动能力", 1, 8, 10, 20),
        DAILY_ACTIVITY("日常活动能力", 9, 16, 10, 20),
        MENTAL_HEALTH("心理健康与情绪", 17, 24, 10, 20),
        DIET_HABITS("饮食习惯与营养", 25, 32, 10, 20),
        SLEEP_QUALITY("睡眠质量", 33, 36, 10, 20),
        CHRONIC_DISEASES("慢性疾病与症状", 37, 40, 10, 20);

        final String chineseName;
        final int startId;
        final int endId;
        final int lowThreshold;
        final int mediumThreshold;

        HealthDimension(String chineseName, int startId, int endId, int lowThreshold, int mediumThreshold) {
            this.chineseName = chineseName;
            this.startId = startId;
            this.endId = endId;
            this.lowThreshold = lowThreshold;
            this.mediumThreshold = mediumThreshold;
        }
    }

    @Autowired
    private IChatService chatService;

    @Autowired
    private ConvertTextToPdf convertTextToPdf;

    @Override
    public List<HealthyQuestions> getQuestions() {
        return this.list(new LambdaQueryWrapper<>());
    }

    @Override
    public byte[] evaluateHealth(HttpServletRequest req, Map<Integer, Integer> answers) {
        Map<HealthDimension, Integer> scores = calculateAllDimensionsScores(answers);
        String combinedDescription = buildCombinedDescription(scores);

        log.debug("健康评估得分: {}", scores);
        log.info("生成综合描述: {}", combinedDescription);
        String aiResult = chatService.deepSeekChatTongYongSync(req,
                combinedDescription + "，分六点用合适语气阐述报告以及建议，不要有多余的符号。");
        log.info("AI生成报告内容: {}", aiResult);

        return convertTextToPdf.generatePdf(aiResult);
    }

    private Map<HealthDimension, Integer> calculateAllDimensionsScores(Map<Integer, Integer> answers) {
        Map<HealthDimension, Integer> scores = new EnumMap<>(HealthDimension.class);
        for (HealthDimension dimension : HealthDimension.values()) {
            scores.put(dimension, calculateDimensionScore(answers, dimension));
        }
        return scores;
    }

    private int calculateDimensionScore(Map<Integer, Integer> answers, HealthDimension dimension) {
        int score = 0;
        for (int i = dimension.startId; i <= dimension.endId; i++) {
            score += answers.getOrDefault(i, 0);
        }
        return score;
    }

    private String buildCombinedDescription(Map<HealthDimension, Integer> scores) {
        StringBuilder description = new StringBuilder(200);
        for (HealthDimension dimension : HealthDimension.values()) {
            int score = scores.get(dimension);
            description.append(generateDimensionDescription(dimension, score))
                    .append("\n");
        }
        return description.toString().trim();
    }

    private String generateDimensionDescription(HealthDimension dimension, int score) {
        String template = "%s（得分 %d）: %s";
        String assessment;

        if (score <= dimension.lowThreshold) {
            assessment = "需要改善，建议重点关注。";
        } else if (score <= dimension.mediumThreshold) {
            assessment = "表现良好，但仍有提升空间。";
        } else {
            assessment = "表现优秀，继续保持！";
        }

        return String.format(template, dimension.chineseName, score, assessment);
    }
}