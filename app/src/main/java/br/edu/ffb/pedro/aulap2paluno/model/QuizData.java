package br.edu.ffb.pedro.aulap2paluno.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonIgnore;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import br.edu.ffb.pedro.bullyelectionp2p.payload.Payload;

@SuppressWarnings("WeakerAccess")
@JsonObject
public class QuizData extends Payload {

    @JsonIgnore
    public static final String TYPE = "QuizData";

    @JsonIgnore
    public static final String LOAD_QUIZ = "loadQuiz";
    @JsonIgnore
    public static final String RESPONSE_QUIZ = "responseQuiz";

    @JsonField
    public String message;
    @JsonField
    public Questionnaire questionnaire;

    public QuizData() {
        super(TYPE);
    }
}
