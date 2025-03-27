package com.cosmos.diseaseforecasterpro.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface VoiceToVoiceService {
    /**
     * 处理语音文件
     *
     * @param req
     * @return
     * @throws Exception
     */
    File processVoice(HttpServletRequest req, String text, String vcn) throws Exception;
}
