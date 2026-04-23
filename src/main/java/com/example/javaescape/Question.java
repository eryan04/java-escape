package com.example.javaescape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    public String category;
    public String question;
    public String correct_answer;
    public List<String> incorrect_answers;
    public List<String> allAnswers;

    public void shuffleAnswers() {
        allAnswers = new ArrayList<>(incorrect_answers);
        allAnswers.add(correct_answer);
        Collections.shuffle(allAnswers);
    }

    private String decodeHtmlEntities(String text) {
        return text
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ");
    }

    public String getDecodedQuestion() {
        return decodeHtmlEntities(question);
    }

    public List<String> getDecodedAnswers() {
        List<String> decoded = new ArrayList<>();
        for (String answer : allAnswers) {
            decoded.add(decodeHtmlEntities(answer));
        }
        return decoded;
    }

    public String getDecodedCorrectAnswer() {
        return decodeHtmlEntities(correct_answer);
    }
}
