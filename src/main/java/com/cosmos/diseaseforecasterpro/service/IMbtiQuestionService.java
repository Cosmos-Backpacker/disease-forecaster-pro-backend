package com.cosmos.diseaseforecasterpro.service;

import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiAnswer;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cosmos.diseaseforecasterpro.pojo.Mbti.MbtiResult;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-03-26
 */
public interface IMbtiQuestionService extends IService<MbtiQuestion> {


    // 获取所有问题
    List<MbtiQuestion> get200Questions();

    List<MbtiQuestion> get93Questions();

    List<MbtiQuestion> get145Questions();

    // 根据用户回答的问题，评估用户人格
    MbtiResult evaluatePersonality(HttpServletRequest req, List<MbtiAnswer> userAnswers);

}
