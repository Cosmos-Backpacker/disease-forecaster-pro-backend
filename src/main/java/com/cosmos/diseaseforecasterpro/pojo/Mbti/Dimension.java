package com.cosmos.diseaseforecasterpro.pojo.Mbti;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 维度分析类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dimension {
    private String dimension;    // 维度名称（如"精力来源"）
    private String selected;     // 选择的倾向（如"外倾-E"）
    private double percentage;   // 倾向百分比
    private String description;  // 维度详细描述

    // getters and setters
}