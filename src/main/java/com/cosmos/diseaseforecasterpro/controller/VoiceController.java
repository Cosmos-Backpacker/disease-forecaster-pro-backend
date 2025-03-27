package com.cosmos.diseaseforecasterpro.controller;

import com.cosmos.diseaseforecasterpro.service.VoiceToVoiceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;


@Slf4j
@RestController
@RequestMapping("/chatVoice")
public class VoiceController {

    @Autowired
    private VoiceToVoiceService voiceToVoiceService;

    @PostMapping(value = "/voice")
    public ResponseEntity<Resource> processVoice(HttpServletRequest req, @RequestParam(value = "text") String text, @RequestParam(defaultValue = "xiaoyan", required = false) String vcn) {
        try {
            // 调用服务生成音频文件
            File outputFile = voiceToVoiceService.processVoice(req, text,vcn);

            // 检查文件是否存在
            if (!outputFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 创建一个 FileSystemResource 对象
            FileSystemResource fileResource = new FileSystemResource(outputFile);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", outputFile.getName());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // 返回 ResponseEntity
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
