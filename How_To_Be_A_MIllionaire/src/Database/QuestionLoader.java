package Database;
//connection test: Ria's here:3 hi
import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import Pack_1.Question;

public class QuestionLoader {

    public static List<Question> loadQuestions() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("BeMillionaireQuestions.json");

            QuestionList wrapper = mapper.readValue(file, QuestionList.class);
            return wrapper.getQuestions();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static class QuestionList {
        private List<Question> questions;

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }
}
