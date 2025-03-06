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
    //用于存储所有用户与AI的对话记录
    private static final Map<Long, List<Message>> allUserMessageMap = new ConcurrentHashMap<>();


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
        if (!allUserMessageMap.containsKey(userId)){
            log.error("记录数据的集合不存在");
            allUserMessageMap.put(userId, new ArrayList<Message>());
        }


         allUserMessageMap.get(userId).add(message);
    }


}
