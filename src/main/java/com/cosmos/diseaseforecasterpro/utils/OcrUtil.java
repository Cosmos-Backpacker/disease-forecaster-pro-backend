package com.cosmos.diseaseforecasterpro.utils;

import com.cosmos.diseaseforecasterpro.bean.ImageUnderstand.XfMessageImgU;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.cosmos.diseaseforecasterpro.service.impl.IXFServiceImpl.historyListImage;


@Component

public class OcrUtil {

    public String readAllBytes(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len = 0;
        while ((len = is.read(b)) != -1) {
            sb.append(new String(b, 0, len, "utf-8"));
        }
        return sb.toString();
    }


    //====辅助函数read()
    public static byte[] read(String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        byte[] data = inputStream2ByteArray(in);
        in.close();
        return data;
    }


    //======辅助函数=======
    public static byte[] inputStream2ByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }


    //========================这个是讯飞图片理解调用的辅助函数，用于判断对话历史总数是否大于1.2w===============
    public static boolean canAddHistory() {  // 由于历史记录最大上线1.2W左右，需要判断是能能加入历史
        int history_length = 0;
        for (XfMessageImgU temp : historyListImage) {
            history_length = history_length + temp.getContent().length();
        }
        // System.out.println(history_length);
        if (history_length > 1200000000) { // 这里限制了总上下文携带，图片理解注意放大 ！！！
            historyListImage.remove(0);
            return false;
        } else {
            return true;
        }
    }


}
