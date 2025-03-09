package com.cosmos.diseaseforecasterpro.pojo.Mbti;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-03-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mbti_question")
public class MbtiQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String content;

    private String dimension;

    private String optionA;

    private String optionB;

    private Integer optionAScore;

    private Integer optionBScore;

    private Integer kind;
}
