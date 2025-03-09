package com.cosmos.diseaseforecasterpro.bean.ImageGeneration;

import lombok.Data;

import java.util.List;


@Data
public class Choices {
    private int status;
    private int seq;
    private List<Text> text;
}
