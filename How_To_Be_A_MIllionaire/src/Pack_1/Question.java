package Pack_1;

import java.util.List;

public class Question {

    // ------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------
    private String id;
    private int tier;
    private String questionText;
    private List<Answer> answerList;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------
    public Question() {
        // Required by Jackson for JSON deserialization
    }

    public Question(String id, int tier, String questionText, List<Answer> answers) {
        this.id = id;
        this.tier = tier;
        this.questionText = questionText;
        this.answerList = answers;
    }

    // ------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------
    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<Answer> getAnswers() {
        return answerList;
    }

    // ------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------
    public void setId(String id) {
        this.id = id;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setAnswers(List<Answer> answers) {
        this.answerList = answers;
    }
}
