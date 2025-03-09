package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IUserService;
import com.cosmos.diseaseforecasterpro.service.IXFService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/xfModel")
public class XFMessageController {

    @Autowired
    private IXFService xFService;


    @Autowired
    private IUserService userService;

    @PostMapping("/imageU")
    public Result XFImageU(@RequestParam("file") MultipartFile file, HttpServletRequest request, String question) {

        long userId = userService.getUserId(request);
        log.error("question:{}", question);

        if (StringUtils.isBlank(question)) {
            log.error("uid或text不能为空");
            return Result.error("uid或text不能为空");
        }
        return xFService.ImageUnderstand(file, String.valueOf(userId), question);
    }


    @PostMapping("/imageG")
    public Result XFImageG(HttpServletRequest request, String content) {
        long userId = userService.getUserId(request);

        if (StringUtils.isBlank(content)) {
            return Result.error("uid或text不能为空");
        }

        return Result.success(xFService.ImageGeneration(String.valueOf(userId), content));

    }


}
