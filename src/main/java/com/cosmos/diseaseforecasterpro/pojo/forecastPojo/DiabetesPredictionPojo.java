package com.cosmos.diseaseforecasterpro.pojo.forecastPojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DiabetesPredictionPojo {
    private int pregnancies; // 怀孕次数
    private double glucose; // 血糖水平
    private double bloodPressure; // 血压
    private double skinThickness; // 皮肤厚度
    private double insulin; // 胰岛素水平
    private double bmi; // 身体质量指数
    private double diabetesPedigree; // 糖尿病家族史评分
    private int age; // 年龄
    private double hba1c; // 糖化血红蛋白
    private double triglycerides; // 甘油三酯
    private double waistHipRatio; // 腰臀比
    private String exerciseFrequency; // 运动频率
    private boolean hasGestationalDiabetes; // 是否有妊娠糖尿病史
    private String symptomDescription; // 症状描述
    private int sex; // 性别（0：女性，1：男性）

}
