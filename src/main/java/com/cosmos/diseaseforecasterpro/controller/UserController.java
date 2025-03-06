package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.SSE.SseClient;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-02-27
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SseClient sseClient;


    @Autowired
    private IUserService userService;

    /**
     * 获取SSE连接
     *
     * @param request 请求
     * @return SseEmitter
     */
    @GetMapping("/SseLink")
    public SseEmitter SseLink(HttpServletRequest request) {

        long userId = userService.getUserId(request);


        return sseClient.createSse(userId);
    }


    @GetMapping("/closeSseLink")
    public Result closeSseLink(HttpServletRequest request) {
        long userId = userService.getUserId(request);

        Boolean result = sseClient.closeSse(userId);
        if (result) {
            return Result.success("关闭成功");
        }

        return Result.error("关闭失败");


    }


    @PostMapping("/register")
    public Result register(String userAccount, String password, String checkPassword) {

        return userService.userRegister(userAccount, password, checkPassword);

    }


    @PostMapping("/login")
    public Result login(String userAccount, String userPassword, HttpServletRequest request) {
        return userService.userLogin(userAccount, userPassword, request);

    }


    /**
     * 用户注销
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/userLayout")
    public Result userLayout(HttpServletRequest request) {
        return userService.userLayout(request);
    }


}
