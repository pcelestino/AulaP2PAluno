package br.edu.ffb.pedro.aulap2paluno.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonIgnore;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Arrays;
import java.util.List;

import br.edu.ffb.pedro.aulap2paluno.converter.StringListConverter;

@SuppressWarnings("WeakerAccess")
@JsonObject
@Entity
public class Question {

    @JsonIgnore
    @Transient
    public static final String SINGLE_CHOICE = "single";

    @JsonIgnore
    @Transient
    public static final String MULTIPLE_CHOICE = "multiple";

    @JsonField
    @Id(autoincrement = true)
    public Long id;

    @JsonField
    @Index
    public long questionnaireId;

    @JsonField
    public float value;

    @JsonField
    public float score;

    @JsonField
    public float errorPenalty;

    @JsonField
    public String title;

    @JsonField
    public String type;

    @JsonField
    @Convert(converter = StringListConverter.class, columnType = String.class)
    public List<String> choices;

    @JsonField
    @Convert(converter = StringListConverter.class, columnType = String.class)
    public List<String> rightChoices;

    @JsonField
    @Convert(converter = StringListConverter.class, columnType = String.class)
    public List<String> selectedChoices;

    @Generated(hash = 1575940576)
    public Question(Long id, long questionnaireId, float value, float score,
                    float errorPenalty, String title, String type, List<String> choices,
                    List<String> rightChoices, List<String> selectedChoices) {
        this.id = id;
        this.questionnaireId = questionnaireId;
        this.value = value;
        this.score = score;
        this.errorPenalty = errorPenalty;
        this.title = title;
        this.type = type;
        this.choices = choices;
        this.rightChoices = rightChoices;
        this.selectedChoices = selectedChoices;
    }

    @Generated(hash = 1868476517)
    public Question() {
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + getId() +
                ", questionnaireId=" + getQuestionnaireId() +
                ", value=" + getValue() +
                ", score=" + getScore() +
                ", errorPenalty=" + getErrorPenalty() +
                ", title='" + getTitle() + '\'' +
                ", type='" + getType() + '\'' +
                ", choices=" + Arrays.toString(getChoices().toArray()) +
                ", rightChoices=" + Arrays.toString(getRightChoices().toArray()) +
                ", selectedChoices=" + Arrays.toString(getSelectedChoices().toArray()) +
                '}';
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getQuestionnaireId() {
        return this.questionnaireId;
    }

    public void setQuestionnaireId(long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getErrorPenalty() {
        return this.errorPenalty;
    }

    public void setErrorPenalty(float errorPenalty) {
        this.errorPenalty = errorPenalty;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getChoices() {
        return this.choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public List<String> getRightChoices() {
        return this.rightChoices;
    }

    public void setRightChoices(List<String> rightChoices) {
        this.rightChoices = rightChoices;
    }

    public List<String> getSelectedChoices() {
        return this.selectedChoices;
    }

    public void setSelectedChoices(List<String> selectedChoices) {
        this.selectedChoices = selectedChoices;
    }

}
