package com.cosmos.diseaseforecasterpro.SSE;

import cn.hutool.core.util.StrUtil;
import com.cosmos.diseaseforecasterpro.DeepSeekBean.Message;
import com.cosmos.diseaseforecasterpro.Events.LinkSseEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
@Component
public class SseClient {


    @Getter
    // 存储所有连接
    private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 创建连接
     */
    public SseEmitter createSse(long userId) {

        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        try {
            // 发送一个事件，让客户端重新连接（如果连接断开的话，如果没有断开就不会重新连接）
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));

        } catch (IOException e) {
            e.printStackTrace();
        }

        emitters.put(userId, sseEmitter);
        //发布事件，连接成功之后自动创建一个对话集合
        eventPublisher.publishEvent(new LinkSseEvent(this, userId, "sse连接成功！"));
        log.info("[{}]创建sse连接成功！emitter为：{}", userId,emitters.get(userId));


        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", userId);
            emitters.remove(userId);
        });

        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", userId);
        });

        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", userId, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(String.valueOf(userId))
                                .name("发生异常！")
                                .data("发生异常请重试！")
                                .reconnectTime(3000));
                        emitters.put(userId, sseEmitter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );


        return sseEmitter;
    }

    /**
     * 给指定用户发送消息
     */
    public boolean sendMessage(long userId, String messageId, String message) {
        if (message==null) {
            log.info("参数异常，msg为null,用户ID：{}", userId);
            return false;
        }
        SseEmitter sseEmitter = emitters.get(userId);
        if (sseEmitter == null) {
            log.info("消息推送失败userId:[{}],没有创建连接，请重试。", userId);
            return false;
        }


        try {
            sseEmitter.send(SseEmitter.event().id(messageId).reconnectTime(60 * 1000L).data(message));
            //\log.info("用户{},消息id:{},推送成功:{}", userId, messageId, message);
            return true;
        } catch (Exception e) {
            emitters.remove(userId);
            log.info("用户{},消息id:{},推送异常:{}", userId, messageId, e.getMessage());
            sseEmitter.complete();
            return false;
        }
    }


    /**
     * 重写一个没有messageId的方法
     *
     * @param userId
     * @param message
     * @return
     */
    public boolean sendMessage(long userId, String message) {
        if (message==null) {
            log.info("参数异常，msg为{}", (Object) null);
            return false;
        }
        SseEmitter sseEmitter = emitters.get(userId);

        if (sseEmitter == null) {
            log.info("消息推送失败userId:[{}],没有创建连接，请重试。", userId);
            return false;
        }

        try {
            sseEmitter.send(SseEmitter.event().reconnectTime(60 * 1000L).data(message));
            log.info("用户{},推送成功:{}", userId, message);
            return true;
        } catch (Exception e) {
            emitters.remove(userId);
            log.info("用户{},推送异常:{}", userId, e.getMessage());
            sseEmitter.complete();
            return false;
        }
    }


    /**
     * 断开SSe连接
     *
     * @param userId
     */
    public Boolean closeSse(long userId) {
        if (emitters.containsKey(userId)) {
            SseEmitter sseEmitter = emitters.get(userId);
            sseEmitter.complete();
            emitters.remove(userId);
            log.info("用户{} 连接关闭成功", userId);
            return true;
        } else {

            log.info("用户{} 连接已关闭", userId);
            return false;
        }

    }


    public void completeWithError(long userId, Exception e) {
        SseEmitter sseEmitter = emitters.get(userId);
        sseEmitter.completeWithError(e);

    }


}