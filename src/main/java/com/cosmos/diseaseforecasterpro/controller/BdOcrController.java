package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.Bdocr.MedicalReportDetection;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/BdOcr")
@Slf4j
public class BdOcrController {

    @Autowired
    private MedicalReportDetection medicalReportDetection;


    /**
     * 医疗识别
     * @param file 图片
     * @return 结果
     */
    @PostMapping("/MedicalReport")
    public Result medicalReportOcr(@RequestPart(value = "file", required = false) MultipartFile file,
                                   @RequestParam(required = false) String imageUrl) {
        if (file != null && !file.isEmpty()) {
            // 处理上传的文件
            String res = medicalReportDetection.medicalReportDetection(file, null);
            return Result.success("success", res);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // 处理图片链接
            try {
                // 从链接下载图片并处理
                String res = medicalReportDetection.medicalReportDetection(null, imageUrl);
                return Result.success("success", res);
            } catch (Exception e) {
                return Result.error("error ，Failed to download image from URL");
            }
        } else {
            return Result.error("error ，Either file or URL must be provided");
        }
    }


}
