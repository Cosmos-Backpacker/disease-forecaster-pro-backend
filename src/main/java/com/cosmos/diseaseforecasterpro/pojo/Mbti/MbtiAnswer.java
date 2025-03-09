
package com.cosmos.diseaseforecasterpro.pojo.Mbti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MbtiAnswer {
    private Integer questionId;
    private String selectedOption; // 'A' or 'B'
}
