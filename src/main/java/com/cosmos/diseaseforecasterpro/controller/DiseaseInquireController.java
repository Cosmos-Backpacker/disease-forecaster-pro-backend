package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.otherService.DiseaseInquireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/disease")
@Slf4j
public class DiseaseInquireController {

    @Autowired
    private DiseaseInquireService diseaseInquireService;


    @GetMapping("/inquire")
    public Result Inquire(String name) {

        return diseaseInquireService.diseaseInquire(name);
    }


    @GetMapping("/chat")
    public Result chatBotInquire(String question) {
        return diseaseInquireService.chatbotInquire(question);
    }


}
