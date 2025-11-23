package com.example.comp3606project_group2;

import java.io.Serializable;

public class SpellingQuestion implements Serializable {

    private final String word;
    private final String hint;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final char correctOption;

    public SpellingQuestion(String word,
                            String hint,
                            String optionA,
                            String optionB,
                            String optionC,
                            String optionD,
                            char correctOption) {
        this.word = word;
        this.hint = hint;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = Character.toUpperCase(correctOption);
    }

    public String getWord() { return word; }
    public String getHint() { return hint; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public char getCorrectOption() { return correctOption; }
}
