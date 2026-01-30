package Pack_1;

public class Answer {
    private String text;
    private boolean correct;

    public Answer() {} // Jackson needs a no-arg constructor

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}

