package com.cosmos.diseaseforecasterpro.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

public interface IChatService {

    String deepSeekChat(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一个助手，请你回答用户问题") String systemMessage);

    String psyHealthDeepSeekChat(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一个助手，请回答用户问题") String systemMessage);

    // 同步调用deepSeek
    String deepSeekScheduleChatSync(HttpServletRequest req, String question, @RequestParam(required = false, defaultValue = "你是一个助手，请回答用户问题") String systemMessage);

    // 同步调用deepSeek
    String deepSeekVoiceChatSync(HttpServletRequest req, String question);

    /**
     * 同步调用DeepSeek不保存任何记录
     */
    String deepSeekChatTongYongSync(HttpServletRequest req, String question);
}
