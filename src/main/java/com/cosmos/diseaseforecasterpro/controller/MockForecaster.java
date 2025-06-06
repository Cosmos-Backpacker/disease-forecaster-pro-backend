package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.User;
import com.cosmos.diseaseforecasterpro.pojo.forecastPojo.DiabetesPredictionPojo;
import com.cosmos.diseaseforecasterpro.service.impl.UserServiceImpl;
import com.cosmos.diseaseforecasterpro.utils.PdfUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/forecast")
public class MockForecaster {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/Diabetes")
    public ResponseEntity<byte[]> DiabetesForecast(@RequestBody DiabetesPredictionPojo diabetesPredictionPojo, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("表单数据: {}", diabetesPredictionPojo);
        long userId = userService.getUserId(request);
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }


        byte[] pdfBytes = PdfUtil.exportPdfToBytes(user, diabetesPredictionPojo);


//        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "health_report.pdf");
        headers.setContentLength(pdfBytes.length);
        log.info("预测报告生成成功");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

    }


}
