package com.cosmos.diseaseforecasterpro.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class LinkSseEvent extends ApplicationEvent {
    private final Long userId; // 用户唯一标识
    private final String msg;
    private final String type; // 消息类型

    public LinkSseEvent(Object source, Long userId, String msg, String type) {
        super(source);
        this.userId = userId;
        this.msg = msg;
        this.type = type;
    }


}