package com.cosmos.diseaseforecasterpro.EventListener;

import com.cosmos.diseaseforecasterpro.DeepSeekBean.Message;
import com.cosmos.diseaseforecasterpro.Events.LinkSseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
public class MyEventListener {


    @EventListener
    public void onEvent(LinkSseEvent event) {
        // 处理事件
        log.info("开始处理事件{},用户id：{}", event.getMsg(), event.getUserId());

        //创建对话集合
        if(!(Message.getAllUserMessageMap().containsKey(event.getUserId())))
            Message.getAllUserMessageMap().put(event.getUserId(), new ArrayList<Message>());
        log.info("事件处理完成");

    }




}
