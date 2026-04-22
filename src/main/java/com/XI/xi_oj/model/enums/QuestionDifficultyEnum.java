package com.XI.xi_oj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Question difficulty enum.
 */
public enum QuestionDifficultyEnum {

    EASY("easy", "easy"),
    MEDIUM("medium", "medium"),
    HARD("hard", "hard");

    private final String text;

    private final String value;

    QuestionDifficultyEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(QuestionDifficultyEnum::getValue).collect(Collectors.toList());
    }

    public static QuestionDifficultyEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (QuestionDifficultyEnum difficultyEnum : values()) {
            if (difficultyEnum.value.equals(value)) {
                return difficultyEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}