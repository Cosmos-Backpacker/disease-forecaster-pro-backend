package com.cosmos.diseaseforecasterpro.DeepSeekBean;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@Slf4j
public class Message {

    public static final String ROLE_USER = "user";

    public static final String ROLE_ASSISTANT = "assistant";

    public static final String ROLE_SYSTEM = "system";

    public static final String ROLE_TOOL = "tool";

    public String role;
    public String content;


    @Getter
    //用于存储所有用户与通用AI的对话记录
    private static final Map<Long, List<Message>> allUserMessageMap = new ConcurrentHashMap<>();


    //用于存储所有用户与日程安排小能手的对话记录
    @Getter
    private static final Map<Long, List<Message>> allUserScheduleMessageMap = new ConcurrentHashMap<>();


    @Getter
    //用于存储所有用户与通用AI的对话记录
    private static final Map<Long, List<Message>> allUserPsyHealthMessageMap = new ConcurrentHashMap<>();


    @Getter
    //用于存储所有用户与通用AI的对话记录
    private static final Map<Long, List<Message>> allUserVoiceMessageMap = new ConcurrentHashMap<>();


    public Message(String content, String role) {
        this.content = content;
        this.role = role;
    }


    public static Message createUserMessage(String content) {
        return new Message(content, ROLE_USER);
    }


    public static Message createAssistantMessage(String content) {
        return new Message(content, ROLE_ASSISTANT);
    }


    public static Message createSystemMessage(String content) {
        return new Message(content, ROLE_SYSTEM);
    }


    public static Message createToolMessage(String content) {
        return new Message(content, ROLE_TOOL);
    }


    public static void recordMessage(Long userId, Message message) {
        if (!allUserMessageMap.containsKey(userId)) {
            log.error("记录数据的集合不存在");
            allUserMessageMap.put(userId, new ArrayList<Message>());
        }
        allUserMessageMap.get(userId).add(message);
    }


    public static void recordScheduleMessage(Long userId, Message message) {
        if (!allUserScheduleMessageMap.containsKey(userId)) {
            log.error("记录数据的集合不存在");
            allUserScheduleMessageMap.put(userId, new ArrayList<Message>());
        }
        allUserScheduleMessageMap.get(userId).add(message);
        log.error("记录成功");
    }

    public static void recordPsyHealthMessage(Long userId, Message message) {
        if (!allUserPsyHealthMessageMap.containsKey(userId)) {
            log.error("记录数据的集合不存在");
            allUserPsyHealthMessageMap.put(userId, new ArrayList<Message>());
        }
        allUserPsyHealthMessageMap.get(userId).add(message);
        log.error("记录成功");
    }


    public static void recordVoiceMessage(Long userId, Message message) {
        if (!allUserVoiceMessageMap.containsKey(userId)) {
            log.error("记录数据的集合不存在");
            allUserVoiceMessageMap.put(userId, new ArrayList<Message>());
        }
        allUserVoiceMessageMap.get(userId).add(message);
        log.error("记录成功");
    }
}
