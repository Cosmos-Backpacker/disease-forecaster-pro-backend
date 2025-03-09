package com.cosmos.diseaseforecasterpro.service.impl;


import com.cosmos.diseaseforecasterpro.DeepSeekBean.Message;
import com.cosmos.diseaseforecasterpro.Listener.WebSocketListener.XFWebSocketImageUnderstandListener;
import com.cosmos.diseaseforecasterpro.bean.ImageGeneration.JsonParseImageG;
import com.cosmos.diseaseforecasterpro.bean.ImageGeneration.Text;
import com.cosmos.diseaseforecasterpro.bean.ImageUnderstand.XfMessageImgU;
import com.cosmos.diseaseforecasterpro.bean.XFWebClient;
import com.cosmos.diseaseforecasterpro.config.XFConfig;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.service.IXFService;
import com.cosmos.diseaseforecasterpro.utils.OcrUtil;
import com.google.gson.Gson;
import io.netty.channel.Channel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author: ChengLiang
 * @CreateTime: 2023-10-17  15:58
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Data
@Service
public class IXFServiceImpl implements IXFService {

    @Autowired
    private XFConfig xfConfig;

    @Autowired
    private XFWebClient xfWebClient;

    @Autowired
    private OcrUtil ocrUtil;


    public Gson gson = new Gson();

    //图片理解请求中设置历史对话合集
    //可以试着用这个方法改一下之前的大模型
    public static List<XfMessageImgU> historyListImage = new ArrayList<>(); // 对话历史存储集合


    //=========================发起图片理解请求==================================
    public synchronized Result ImageUnderstand(MultipartFile file, String uid, String text) {
        //将text文本内容封装进questions中
        ArrayList<XfMessageImgU> questions = new ArrayList<>();

        boolean ImageAddFlag = false; // 判断是否添加了图片信息


        // 历史问题获取
        if (!historyListImage.isEmpty()) { // 保证首个添加的是图片
            for (XfMessageImgU tempxfMessageImgU : historyListImage) {
                if (tempxfMessageImgU.getContentType().equals("image")) { // 保证首个添加的是图片
                    questions.add(tempxfMessageImgU);
                    ImageAddFlag = true;
                }
            }
        }


        // 最新问题
        XfMessageImgU roleContent = new XfMessageImgU();
        // 添加图片信息
        if (!ImageAddFlag) {
            roleContent.setRole("user");
            try {
                roleContent.setContent(Base64.getEncoder().encodeToString(file.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            roleContent.setContentType("image");
            questions.add(roleContent);
            historyListImage.add(roleContent);
        }


        // 添加对图片提出要求的信息，也就是最新提出的问题
        XfMessageImgU roleContent1 = new XfMessageImgU();
        roleContent1.setRole("user");
        roleContent1.setContent(text);  //text为最新提出的问题
        roleContent1.setContentType("text");
        questions.add(roleContent1);
        historyListImage.add(roleContent1);

        //==========================封装好问题知否发出请求===========================
        //创建一个用于处理响应对象的listener
        XFWebSocketImageUnderstandListener xfwebSocketImageUnderstandListener = new XFWebSocketImageUnderstandListener();

        WebSocket webSocket = xfWebClient.sendImageMessage(uid, questions, xfwebSocketImageUnderstandListener);
        if (webSocket == null) {
            log.error("webSocket连接异常");
            throw new RuntimeException("webSocket连接异常");
        }
        try {
            int count = 0;
            int maxCount = xfConfig.getMaxResponseTime() * 10;
            while (count <= maxCount) {
                Thread.sleep(200);
                if (xfwebSocketImageUnderstandListener.isWsCloseFlag()) {
                    break;
                }
                count++;
            }
            if (count > maxCount) {
                throw new RuntimeException("请求超时");
            }
            //封装成ResultBean对象并返回
            return Result.success("响应成功", xfwebSocketImageUnderstandListener.getAnswer());
        } catch (Exception e) {
            log.error("请求异常：{}", e);
            e.printStackTrace();
        } finally {
            webSocket.close(1000, "");
        }
        return Result.success("");
    }


    public String ImageGeneration(String uid, String content) {

        //发送请求获取字符串类型的答案
        String result = "";
        String resp = xfWebClient.ImageGenerationRequest(uid, content);
        //解析答案
        JsonParseImageG jsonParseImageG = gson.fromJson(resp, JsonParseImageG.class);

        if (jsonParseImageG.getPayload() != null) {
            //这里的Text类是ImageGeneration包里的类
            List<Text> textList = jsonParseImageG.getPayload().getChoices().getText();

            for (Text temp : textList) {
                //将Text列表集合内的图片内容整理在一起
                //因为每次只能返回一张图片，所以这里就直接用content替换result了，没有处理返回过个图片内容
                result = temp.getContent();
            }

        } else {

            // return ResultBean.fail("响应失败");
            return "响应失败";
        }

        return result;
    }


}
