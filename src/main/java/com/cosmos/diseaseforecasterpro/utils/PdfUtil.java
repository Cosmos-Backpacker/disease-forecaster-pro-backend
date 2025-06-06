package com.cosmos.diseaseforecasterpro.utils;

import com.cosmos.diseaseforecasterpro.pojo.User;
import com.cosmos.diseaseforecasterpro.pojo.forecastPojo.DiabetesPredictionPojo;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PdfUtil {

    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_RIGHT = 50f;
    private static final float MARGIN_TOP = 50f;
    private static final float MARGIN_BOTTOM = 50f;

    public static byte[] exportPdfToBytes(User user, DiabetesPredictionPojo diabetesPredictionPojo) throws Exception {
        // Create a byte array output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 设置中文字体
        String fontPath = "C:/Windows/Fonts/simfang.ttf";
        BaseFont bfChinese = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontChinese = new Font(bfChinese, 12, Font.NORMAL);
        Font titleFont = new Font(bfChinese, 20, Font.BOLD);
        Font subtitleFont = new Font(bfChinese, 16, Font.BOLD, new BaseColor(0, 0, 0));
        Font sectionFont = new Font(bfChinese, 14, Font.BOLD, new BaseColor(0, 0, 0));
        Font resultFont = new Font(bfChinese, 16, Font.BOLD, new BaseColor(0, 204, 0));
        Font normalFont = new Font(bfChinese, 12, Font.NORMAL);

        // 创建文档
        Document document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // 打开文档
        document.open();

        // 添加标题
        Paragraph title = new Paragraph("糖尿病预测报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        // 添加打印时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Paragraph printTime = new Paragraph("打印时间：" + LocalDateTime.now().format(formatter), normalFont);
        printTime.setAlignment(Element.ALIGN_CENTER);
        printTime.setSpacingAfter(20f);
        document.add(printTime);

        // 添加基本信息
        addSectionTitle(document, "基本信息", sectionFont);

        PdfPTable basicInfoTable = new PdfPTable(2);
        basicInfoTable.setWidthPercentage(100);
        basicInfoTable.setSpacingBefore(10f);
        basicInfoTable.setSpacingAfter(20f);

        addTableRow(basicInfoTable, "姓名", user.getUsername(), fontChinese);
        addTableRow(basicInfoTable, "性别", diabetesPredictionPojo.getSex() == 1 ? "男" : "女", fontChinese);
        addTableRow(basicInfoTable, "年龄", diabetesPredictionPojo.getAge() + "岁", fontChinese);

        document.add(basicInfoTable);

        // 添加检测参数
        addSectionTitle(document, "检测参数", sectionFont);

        PdfPTable testParamsTable = new PdfPTable(3);
        testParamsTable.setWidthPercentage(100);
        testParamsTable.setSpacingBefore(10f);
        testParamsTable.setSpacingAfter(20f);

        // 表头
        addTableHeader(testParamsTable, "参数名称", fontChinese);
        addTableHeader(testParamsTable, "值", fontChinese);
        addTableHeader(testParamsTable, "单位", fontChinese);

        // 表格内容
        addTestParamRow(testParamsTable, "妊娠次数（Pregnancies）", String.valueOf(diabetesPredictionPojo.getPregnancies()), "次", fontChinese);
        addTestParamRow(testParamsTable, "血糖（Glucose）", String.valueOf(diabetesPredictionPojo.getGlucose()), "mg/dL", fontChinese);
        addTestParamRow(testParamsTable, "血压（Blood Pressure）", String.valueOf(diabetesPredictionPojo.getBloodPressure()), "mmHg", fontChinese);
        addTestParamRow(testParamsTable, "皮肤厚度（Skin Thickness）", String.valueOf(diabetesPredictionPojo.getSkinThickness()), "mm", fontChinese);
        addTestParamRow(testParamsTable, "胰岛素（Insulin）", String.valueOf(diabetesPredictionPojo.getInsulin()), "μU/mL", fontChinese);
        addTestParamRow(testParamsTable, "体质指数（BMI）", String.valueOf(diabetesPredictionPojo.getBmi()), "kg/m²", fontChinese);
        addTestParamRow(testParamsTable, "糖尿病家族史（Diabetes Pedigree）", String.valueOf(diabetesPredictionPojo.getDiabetesPedigree()), "-", fontChinese);
        addTestParamRow(testParamsTable, "年龄（Age）", String.valueOf(diabetesPredictionPojo.getAge()), "岁", fontChinese);
        addTestParamRow(testParamsTable, "性别（Sex）", String.valueOf(diabetesPredictionPojo.getSex()), "-", fontChinese);
        addTestParamRow(testParamsTable, "糖化血红蛋白（HbA1c）", String.valueOf(diabetesPredictionPojo.getHba1c()), "%", fontChinese);
        addTestParamRow(testParamsTable, "甘油三酯（Triglycerides）", String.valueOf(diabetesPredictionPojo.getTriglycerides()), "mg/dL", fontChinese);
        addTestParamRow(testParamsTable, "腰臀比（Waist Hip Ratio）", String.valueOf(diabetesPredictionPojo.getWaistHipRatio()), "-", fontChinese);
        addTestParamRow(testParamsTable, "锻炼频率（Exercise Frequency）", diabetesPredictionPojo.getExerciseFrequency(), "-", fontChinese);
        addTestParamRow(testParamsTable, "是否有妊娠糖尿病（Gestational Diabetes）", diabetesPredictionPojo.isHasGestationalDiabetes() ? "是" : "否", "-", fontChinese);
        addTestParamRow(testParamsTable, "症状描述（Symptom Description）", diabetesPredictionPojo.getSymptomDescription(), "-", fontChinese);

        document.add(testParamsTable);

        // 添加预测结果
        addSectionTitle(document, "预测结果", sectionFont);

        Paragraph resultText = new Paragraph("未患病", resultFont);
        resultText.setAlignment(Element.ALIGN_CENTER);
        resultText.setSpacingAfter(20f);
        document.add(resultText);

        // 添加预防建议
        addSectionTitle(document, "预防建议", sectionFont);

        addNumberedTip(document, "保持健康饮食：多吃蔬菜、水果和全谷物，减少高糖、高脂肪食物的摄入。", normalFont);
        addNumberedTip(document, "定期锻炼：每周至少进行150分钟的中等强度有氧运动，如快走、游泳或骑自行车。", normalFont);
        addNumberedTip(document, "控制体重：保持健康的体重，避免肥胖。", normalFont);
        addNumberedTip(document, "定期体检：每年进行一次全面体检，包括血糖、血压和胆固醇检测。", normalFont);
        addNumberedTip(document, "避免吸烟和过量饮酒：吸烟和过量饮酒会增加患糖尿病的风险，应尽量避免。", normalFont);

        document.add(Chunk.NEWLINE);

        // 添加模型相关参数
        addSectionTitle(document, "模型相关参数", sectionFont);

        Paragraph modelAccuracy = new Paragraph("模型预测准确率：93.97%", normalFont);
        modelAccuracy.setSpacingAfter(20f);
        document.add(modelAccuracy);

        // 添加图表
        addImageSection(document, "混淆矩阵", "src/main/resources/assets/Confusion Matrix.png", writer);
        addImageSection(document, "ROC曲线", "src/main/resources/assets/ROC.png", writer);
        addImageSection(document, "AUC值", "0.823925", writer);
        addImageSection(document, "K-S值", "src/main/resources/assets/K-S.png", writer);
        addImageSection(document, "Lift图", "src/main/resources/assets/Lift.png", writer);
        addImageSection(document, "Gain增益图", "src/main/resources/assets/Gain.png", writer);
        addImageSection(document, "Accuracy曲线", "src/main/resources/assets/Accuracy.png", writer);


        // 添加版权信息
        addFooter(writer, bfChinese);

        // 关闭文档
        document.close();

        // 返回字节数组
        return baos.toByteArray();
    }

// All the helper methods (addSectionTitle, addTableRow, addTableHeader, etc.) remain the same


    private static void addSectionTitle(Document document, String title, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(20f);
        paragraph.setSpacingAfter(10f);
        document.add(paragraph);
    }

    private static void addTableRow(PdfPTable table, String label, String value, Font font) {
        table.addCell(new PdfPCell(new Paragraph(label, font)));
        table.addCell(new PdfPCell(new Paragraph(value, font)));
    }

    private static void addTableHeader(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(header, font));
        cell.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(cell);
    }

    private static void addTestParamRow(PdfPTable table, String param, String value, String unit, Font font) {
        table.addCell(new PdfPCell(new Paragraph(param, font)));
        table.addCell(new PdfPCell(new Paragraph(value, font)));
        table.addCell(new PdfPCell(new Paragraph(unit, font)));
    }

    private static void addNumberedTip(Document document, String text, Font font) throws DocumentException {
        List list = new List(List.ORDERED);
        list.setIndentationLeft(20f);
        list.add(new ListItem(text, font));
        document.add(list);
    }

    private static void addImageSection(Document document, String title, String imagePath, PdfWriter writer) throws Exception {
        // 添加标题
        Paragraph imageTitle = new Paragraph(title, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        imageTitle.setSpacingAfter(10f);
        document.add(imageTitle);

        // 添加图片
        try {
            Image image = Image.getInstance(imagePath);
            float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float documentHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();

            // 自动调整图片大小以适应页面宽度
            if (image.getWidth() > documentWidth) {
                image.scaleToFit(documentWidth, documentHeight);
            }

            // 检查当前页面是否有足够空间
            if (writer.getVerticalPosition(false) - image.getScaledHeight() < document.bottomMargin()) {
                document.newPage();
            }

            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            document.add(new Paragraph("无法加载图片: " + imagePath));
        }
    }

    private static void addFooter(PdfWriter writer, BaseFont font) {
        PdfContentByte cb = writer.getDirectContent();

        // 保存状态
        cb.saveState();

        // 设置字体和颜色
        cb.setFontAndSize(font, 10);
        cb.setColorFill(BaseColor.GRAY);

        // 添加页脚文本
        cb.beginText();
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "该报告单仅供参考",
                writer.getPageSize().getWidth() / 2, 30, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "版权仅归本平台所有",
                writer.getPageSize().getWidth() / 2, 15, 0);
        cb.endText();

        // 恢复状态
        cb.restoreState();
    }
}
