package com.cosmos.diseaseforecasterpro.service;

import com.cosmos.diseaseforecasterpro.pojo.HealthyQuestions;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-03-27
 */
@Service
public interface IHealthyQuestionsService extends IService<HealthyQuestions> {

    // 获取所有健康问卷问题
    List<HealthyQuestions> getQuestions();

    // 根据用户答案计算健康得分
    byte[] evaluateHealth(HttpServletRequest req, Map<Integer, Integer> answers);

}
