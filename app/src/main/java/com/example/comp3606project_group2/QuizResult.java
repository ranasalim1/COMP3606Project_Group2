package com.example.comp3606project_group2;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class QuizResult implements Serializable {

    private final String phone;
    private final String word;
    private final char userAnswer;
    private final char correctAnswer;
    private final boolean correct;
    private final boolean timedOut;
    private final long timestamp;

    public QuizResult(String phone,
                      String word,
                      char userAnswer,
                      char correctAnswer,
                      boolean correct,
                      boolean timedOut,
                      long timestamp) {
        this.phone = phone;
        this.word = word;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.timedOut = timedOut;
        this.timestamp = timestamp;
    }

    public String toDisplayString() {
        String time = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ").append(time).append("\n");
        sb.append("Phone: ").append(phone).append("\n");
        sb.append("Word: ").append(word).append("\n");
        sb.append("Answer: ").append(userAnswer).append("\n");
        sb.append("Correct: ").append(correctAnswer).append("\n");
        if (timedOut) {
            sb.append("Result: TIME OUT");
        } else if (correct) {
            sb.append("Result: CORRECT");
        } else {
            sb.append("Result: INCORRECT");
        }
        return sb.toString();
    }
}
