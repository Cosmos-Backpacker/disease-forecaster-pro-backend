package com.cosmos.diseaseforecasterpro.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class ConvertTextToPdf {

    public byte[] generatePdf(String content) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            // 初始化 PdfWriter
            PdfWriter.getInstance(document, baos);
            document.open();

            // 加载系统自带的仿宋字体
            String fontPath = "C:/Windows/Fonts/simfang.ttf"; // 替换为实际字体路径
            BaseFont bfChinese = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontChinese = new Font(bfChinese, 12);

            // 添加内容到 PDF
            document.add(new Paragraph(content, fontChinese));
        } catch (DocumentException | java.io.IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }
}
