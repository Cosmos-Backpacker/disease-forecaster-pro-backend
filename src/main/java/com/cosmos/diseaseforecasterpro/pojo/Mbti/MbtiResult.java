package com.cosmos.diseaseforecasterpro.pojo.Mbti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MbtiResult {
    private String mbtiType;
    private String typeDescription;
    private List<Dimension> dimensions;
    private String summary;

    // getters and setters
}