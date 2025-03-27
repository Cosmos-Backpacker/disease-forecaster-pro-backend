package com.cosmos.diseaseforecasterpro.service.impl;

import com.cosmos.diseaseforecasterpro.service.IChatService;
import com.cosmos.diseaseforecasterpro.service.VoiceToVoiceService;
import com.cosmos.diseaseforecasterpro.utils.TTS;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class VoiceToVoiceServiceImpl implements VoiceToVoiceService {

    @Autowired
    private IChatService chatService;

    @Autowired
    private TTS tts;

    @Override
    public File processVoice(HttpServletRequest req, String text,String vcn) throws Exception {
        // 获取大模型响应文本
        String responseText = chatService.deepSeekVoiceChatSync(req, text);
        log.info("大模型返回的文本: {}", responseText);

        // 生成输出文件路径
        String projectPath = System.getProperty("user.dir");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String outputFilePath = projectPath + "/src/main/resources/static/audio/output_" + timestamp + ".mp3";
        File outputFile = new File(outputFilePath);

        // 异步转同步：等待合成完成
        try {
            tts.convertTextToSpeech(responseText, outputFile,vcn)
                    .get(60, TimeUnit.SECONDS); // 设置超时防止无限等待

            log.error("返回成功");
        } catch (TimeoutException e) {
            log.error("语音合成超时");
            throw new RuntimeException("语音合成未在60秒内完成");
        } catch (ExecutionException e) {
            log.error("语音合成过程中发生异常: {}", e.getCause().getMessage());
            throw new Exception(e.getCause()); // 抛出实际异常
        }
        log.info("语音处理完成，输出文件: {}", outputFile.getAbsolutePath());
        return outputFile;
    }
//    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
//        File convFile = new File(file.getOriginalFilename());
//        convFile.createNewFile();
//        try (FileOutputStream fos = new FileOutputStream(convFile)) {
//            fos.write(file.getBytes());
//        }
//        return convFile;
//    }
}
