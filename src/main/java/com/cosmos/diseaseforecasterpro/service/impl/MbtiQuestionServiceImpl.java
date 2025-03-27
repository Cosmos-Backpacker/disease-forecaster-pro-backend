package com.cosmos.diseaseforecasterpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.Dimension;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiAnswer;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiQuestion;
import com.cosmos.diseaseforecasterpro.mapper.MbtiQuestionMapper;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiResult;
import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.IMbtiQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-03-26
 */
@Slf4j
@Service
public class MbtiQuestionServiceImpl extends ServiceImpl<MbtiQuestionMapper, MbtiQuestion> implements IMbtiQuestionService {

    @Autowired
    private MbtiQuestionMapper mbtiQuestionMapper;

    @Autowired
    private IChatService chatService;

    // 获取所有MBTI问题
    @Override
    public List<MbtiQuestion> get200Questions() {
        LambdaQueryWrapper<MbtiQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("ORDER BY RAND() LIMIT 200");
        return mbtiQuestionMapper.selectList(wrapper);
    }

    @Override
    public List<MbtiQuestion> get93Questions() {
        LambdaQueryWrapper<MbtiQuestion> wrapper = new LambdaQueryWrapper<>();
        // 合并排序和限制为一个SQL片段
        wrapper.last("ORDER BY RAND() LIMIT 93");

        return mbtiQuestionMapper.selectList(wrapper);
    }

    @Override
    public List<MbtiQuestion> get145Questions() {
        LambdaQueryWrapper<MbtiQuestion> wrapper = new LambdaQueryWrapper<>();
        // 合并排序和限制为一个SQL片段
        wrapper.last("ORDER BY RAND() LIMIT 145");

        return mbtiQuestionMapper.selectList(wrapper);
    }

    @Override
    public MbtiResult evaluatePersonality(HttpServletRequest req, List<MbtiAnswer> userAnswers) {
        //插入用户答案
        /*    mbtiMapper.insertUserAnswers(userAnswers);*/

        // 简单的人格判断逻辑
        Map<String, Integer> scores = new HashMap<>();
        scores.put("E", 0);
        scores.put("I", 0);
        scores.put("S", 0);
        scores.put("N", 0);
        scores.put("T", 0);
        scores.put("F", 0);
        scores.put("J", 0);
        scores.put("P", 0);

        for (MbtiAnswer answer : userAnswers) {
            MbtiQuestion question = mbtiQuestionMapper.selectById(answer.getQuestionId());
            if ("A".equals(answer.getSelectedOption())) {
                scores.put(question.getDimension().substring(0, 1), scores.getOrDefault(question.getDimension().substring(0, 1), 0) + question.getOptionAScore());
            } else {
                scores.put(question.getDimension().substring(1, 2), scores.getOrDefault(question.getDimension().substring(1, 2), 0) + question.getOptionBScore());
            }
        }

        // 构建MBTI类型
        String personalityType = buildPersonalityType(scores);


        // 获取AI分析结果（修改为返回结构化数据）
        MbtiResult result = new MbtiResult();
        result.setMbtiType(personalityType);

        // 构建维度分析数据
        List<Dimension> dimensions = new ArrayList<>();
        String[] dimensionPairs = {"E-I", "S-N", "T-F", "J-P"};
        String[] dimensionNames = {"精力来源", "信息获取", "决策方式", "生活态度"};


        for (int i = 0; i < dimensionPairs.length; i++) {
            String[] traits = dimensionPairs[i].split("-");
            String trait1 = traits[0];
            String trait2 = traits[1];

            int score1 = scores.get(trait1);
            int score2 = scores.get(trait2);
            double total = score1 + score2;

            Dimension dim = new Dimension();
            dim.setDimension(dimensionNames[i]);
            dim.setSelected(score1 >= score2 ?
                    getTraitName(trait1) + "-" + trait1 :
                    getTraitName(trait2) + "-" + trait2);
            dim.setPercentage(score1 >= score2 ?
                    Math.round((score1 / total) * 100) :
                    Math.round((score2 / total) * 100));
            dimensions.add(dim);
        }
        result.setDimensions(dimensions);

        // 获取AI分析（修改提示词获取结构化数据）
        String prompt = buildAnalysisPrompt(personalityType, dimensions);
        String aiResponse = chatService.deepSeekChatTongYongSync(req, prompt);

        // 在try代码块前添加预处理
        String cleanedResponse = preprocessAIResponse(aiResponse);
        log.info("Cleaned AI Response: {}", cleanedResponse); // 添加日志用于调试

        // 解析AI响应（假设返回JSON格式）
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(cleanedResponse);

            result.setTypeDescription(root.path("typeDescription").asText());
            result.setSummary(root.path("summary").asText());

            JsonNode dimNodes = root.path("dimensions");
            for (int i = 0; i < dimNodes.size(); i++) {
                JsonNode node = dimNodes.get(i);
                dimensions.get(i).setDescription(node.path("description").asText());
            }
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            // 设置默认描述
            result.setTypeDescription("默认类型描述,可以根据数据库的专业知识返回");
            result.setSummary("默认总结建议");
            dimensions.forEach(d -> d.setDescription("默认维度描述"));
        }
//
//        StringBuilder personality = new StringBuilder();
//        personality.append(scores.get("E") >= scores.get("I") ? "E" : "I");
//        personality.append(scores.get("S") >= scores.get("N") ? "S" : "N");
//        personality.append(scores.get("T") >= scores.get("F") ? "T" : "F");
//        personality.append(scores.get("J") >= scores.get("P") ? "J" : "P");
//
//        log.info("MBTI人格结果：{}", personality);

//        // 添加提示词以便AI更好地分析用户的人格
//        String prompt = "根据MBTI人格测试结果，用户的人格类型是：" + personality + "。请提供对该人格类型的详细分析，包括该人格类型的特点、优势、劣势以及可能的职业倾向,要有和用户沟通的感觉，语气委婉。";

        return result;
    }


    // 辅助方法
    private String buildPersonalityType(Map<String, Integer> scores) {
        return new StringBuilder()
                .append(scores.get("E") >= scores.get("I") ? "E" : "I")
                .append(scores.get("S") >= scores.get("N") ? "S" : "N")
                .append(scores.get("T") >= scores.get("F") ? "T" : "F")
                .append(scores.get("J") >= scores.get("P") ? "J" : "P")
                .toString();
    }


    private String getTraitName(String trait) {
        Map<String, String> traitNames = new HashMap<>();
        traitNames.put("E", "外倾");
        traitNames.put("I", "内倾");
        traitNames.put("S", "实感");
        traitNames.put("N", "直觉");
        traitNames.put("T", "思考");
        traitNames.put("F", "情感");
        traitNames.put("J", "判断");
        traitNames.put("P", "感知");
        return traitNames.get(trait);
    }

    private String buildAnalysisPrompt(String personalityType, List<Dimension> dimensions) {
        return "请生成严格遵循以下JSON格式的性格分析报告，必须且只能包含纯JSON内容：\n" +
                "{\n" +
                "  \"typeDescription\": \"300字左右的类型详细分析\",\n" +
                "  \"dimensions\": [\n" +
                "    {\"description\": \"精力来源维度分析\"},\n" +
                "    {\"description\": \"信息获取维度分析\"},\n" +
                "    {\"description\": \"决策方式维度分析\"},\n" +
                "    {\"description\": \"生活态度维度分析\"}\n" +
                "  ],\n" +
                "  \"summary\": \"200字左右的个性化建议\"\n" +
                "}\n" +
                "要求：\n" +
                "1. 只输出JSON格式内容\n" +
                "2. 禁止使用Markdown语法\n" +
                "3. 禁止包含注释或解释性文字\n" +
                "测试结果：\n" +
                "MBTI类型：" + personalityType + "\n" +
                dimensions.stream()
                        .map(d -> d.getDimension() + ": " + d.getSelected())
                        .collect(Collectors.joining("\n"));
    }

    // 在解析前添加预处理步骤
    private String preprocessAIResponse(String rawResponse) {
        // 去除Markdown代码块标识
        String cleaned = rawResponse.replaceAll("```json", "")
                .replaceAll("```", "");
        // 提取第一个出现的JSON对象
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1) {
            return cleaned.substring(firstBrace, lastBrace + 1);
        }
        return cleaned;
    }

}
