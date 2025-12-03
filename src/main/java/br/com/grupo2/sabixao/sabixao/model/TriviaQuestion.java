package br.com.grupo2.sabixao.sabixao.model;

import java.util.List;

/**
 * Classe para representar uma pergunta da Open Trivia Database
 */
public class TriviaQuestion {
    private String category;
    private String type; // "multiple" ou "boolean"
    private String difficulty; // "easy", "medium", "hard"
    private String question;
    private String correct_answer;
    private List<String> incorrect_answers;

    public TriviaQuestion() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correct_answer;
    }

    public void setCorrectAnswer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrect_answers;
    }

    public void setIncorrectAnswers(List<String> incorrect_answers) {
        this.incorrect_answers = incorrect_answers;
    }

    @Override
    public String toString() {
        return "TriviaQuestion{" +
                "category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", question='" + question + '\'' +
                '}';
    }
}
