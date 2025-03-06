package com.cosmos.diseaseforecasterpro.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class LinkSseEvent extends ApplicationEvent {
    private final Long userId; // 用户唯一标识
    private final String msg;

    public LinkSseEvent(Object source, Long userId, String msg) {
        super(source);
        this.userId = userId;
        this.msg = msg;
    }


}