package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.PointOnImageDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import de.imi.mopat.model.enumeration.QuestionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for table <i>response</i>. Represents the answer a patient has choosen
 * within a questionnaire/an encounter. If the question was of type {@link QuestionType#SLIDER}, the
 * value the patient has chosen is accessible via {@link Response#getValue()}. If the Question was
 * of type {@link QuestionType#FREE_TEXT} or {@link QuestionType#NUMBER_CHECKBOX_TEXT}, the text
 * entered by the patient is accessible via {@link Response#getCustomtext()}. If the Question was of
 * type {@link de.imi.mopat.model.enumeration.QuestionType#DATE} the date entered by the patient is
 * accessible via {@link Response#getDate()}.
 */
@Entity
@Table(name = "response")
public class Response implements Serializable {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @Column(name = "customtext", columnDefinition = "TEXT")
    private String customtext = null;
    @Column(name = "value")
    private Double value = null;
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date = null;
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointOnImage> pointsOnImage = new ArrayList<>();
    @NotNull(message = "{response.answer.notNull}")
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "answer_id", referencedColumnName = "id")
    private Answer answer;
    @JsonIgnore
    @NotNull(message = "{response.encounter.notNull}")
    @ManyToOne
    @JoinColumn(name = "encounter_id", referencedColumnName = "id")
    private Encounter encounter;

    protected Response() { // default constructor (in protected state),
        // should not be accessible to
        // anything else but the JPA implementation (here: Hibernate) and the
        // JUnit tests
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param answer    The new {@link Answer Answer} object associated with this response. Must not
     *                  be <code>null</code>.
     * @param encounter The new {@link Encounter Encounter} object associated with this response.
     *                  Must not be <code>null</code>.
     */
    public Response(final Answer answer, final Encounter encounter) {
        setAnswer(answer);
        setEncounter(encounter);
    }

    /**
     * @return Might be <code>null</code> for newly created objects. Is never
     * <code>&lt;= 0</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * If a questionnaire gives an encounter the possibility to write a comment or an answer to a
     * question, you need the field custom text.
     *
     * @return The custom text entered by the user. Might be <code>null</code>, in case the response
     * does not refer to an answer with free text or the patient did not answer the question. Might
     * be empty ("").
     */
    public String getCustomtext() {
        return customtext;
    }

    /**
     * Sets a new custom text entered by the user.See {@link Response#getCustomtext()} for a
     * description.
     *
     * @param customtext The new custom text. Must not be <code>null</code>. Can be empty ("").
     */
    public void setCustomtext(final String customtext) {
        assert customtext != null : "The given custom text was null";
        this.customtext = customtext;
    }

    /**
     * A slider is an answer type for question, where you have to make your mark between a range.
     * The value represents this mark.
     *
     * @return The value of the slider selection. Might be <code>null</code>, in case the response
     * does not refer to an answer with numeric values or the patient did not answer the question.
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value determined by the slider.Does only make sense if this response corresponds to
     * a {@link SliderAnswer}. See {@link Response#getValue()} for a description
     *
     * @param value The value determined by the slider. Can be <code>null</code> in case the patient
     *              deletes his/her decision. Must not be &lt; the referenced
     *              {@link SliderAnswer#getMinValue()} value. Must not be &gt; the referenced
     *              {@link SliderAnswer#getMaxValue()} value.
     */
    public void setValue(final Double value) {
        assert answer instanceof SliderAnswer :
            "Trying to set a value to a " + "response that doesn't correspond to a slider answer "
                + "doesn't make sense";
        if (value != null) {
            assert
                value >= ((SliderAnswer) answer).getMinValue() :
                "The value was" + " < than the answer's min value";
            assert
                value <= ((SliderAnswer) answer).getMaxValue() :
                "The value was" + " > than the answer's max value";
        }
        this.value = value;
    }

    /**
     * A date is an answer type for question, where you have to select a date, maybe between a
     * range.
     *
     * @return The date selected. Might be <code>null</code>, in case the response does not refer to
     * an answer with a date or the patient did not answer the question.
     */
    public Date getDate() {
        return date;
    }

    /**
     * If a questionnaire gives an encounter the possibility to select a date, you need the field
     * date.
     *
     * @param date The date selected in the questionnaire. Can be
     *             <code>null</code> in case the patient deletes his/her
     *             decision. Must not be &lt; the referenced {@link DateAnswer#getStartDate()}
     *             value. Must not be &gt; the referenced {@link DateAnswer#getEndDate()} value.
     */
    public void setDate(final Date date) {
        assert answer instanceof DateAnswer : "Trying to set a date to a "
            + "response that doesn't correspond to a date answer doesn't " + "make sense";
        if (date != null) {
            if (((DateAnswer) answer).getStartDate() != null) {
                assert date.getTime() >= ((DateAnswer) answer).getStartDate().getTime() :
                    "The value was <" + " than the answer's min value";
            }
            if (((DateAnswer) answer).getEndDate() != null) {
                assert date.getTime() <= ((DateAnswer) answer).getEndDate().getTime() :
                    "The value was >" + " than the answer's max value";
            }
        }
        this.date = date;
    }

    /**
     * Returns the answer associated with this response.
     *
     * @return The {@link Answer Answer} object associated with this response. Is not
     * <code>null</code>.
     */
    public Answer getAnswer() {
        return answer;
    }

    /**
     * Sets a new answer associated with this response.Takes care that the {@link Answer} refers to
     * this one, too.
     *
     * @param answer The new {@link Answer Answer} object associated with this response. Must not be
     *               <code>null</code>.
     */
    public void setAnswer(final Answer answer) {
        assert answer != null : "The given Answer was null";
        this.answer = answer;
        if (!answer.getResponses().contains(this)) {
            answer.addResponse(this);
        }
    }

    /**
     * Returns the encounter associated with this response. See {@link Encounter} for a description
     *
     * @return The {@link Encounter Encounter} object associated with this response. Is not
     * <code>null</code>.
     */
    public Encounter getEncounter() {
        return encounter;
    }

    /**
     * Sets a new Encounter associated with this response.Takes care that the {@link Encounter}
     * refers to this response, too.
     *
     * @param encounter The new {@link Encounter Encounter} object associated with this response.
     *                  Must not be <code>null</code>.
     */
    public void setEncounter(final Encounter encounter) {
        assert encounter != null : "The given Encounter was null";
        this.encounter = encounter;
        if (!encounter.getResponses().contains(this)) {
            encounter.addResponse(this);
        }
    }

    /**
     * Returns the list of {@link PointOnImage PointsOnImage} associated to this response.
     *
     * @return The list of {@link PointOnImage PointsOnImage} associated to this response. Is not
     * <code>null</code>.
     */
    public List<PointOnImage> getPointsOnImage() {
        Collections.sort(pointsOnImage,
            (PointOnImage o1, PointOnImage o2) -> o1.getPosition() - o2.getPosition());
        return pointsOnImage;
    }

    /**
     * Sets the list of {@link PointOnImage PointsOnImage}.Does only make sense if this response
     * corresponds to a {@link ImageAnswer}.
     *
     * @param pointsOnImage The list of {@link PointOnImage PointsOnImage} of this response.
     */
    public void setPointsOnImage(final List<PointOnImage> pointsOnImage) {
        assert answer instanceof ImageAnswer :
            "Trying to set a list of " + "pointsOnImage to a response that doesn't correspond to a "
                + "image answer doesn't make sense";
        this.pointsOnImage = pointsOnImage;
    }

    /**
     * Takes care that the {@link Encounter} does no longer refer to this object. Also removes this
     * objects reference to the {@link Encounter}.
     */
    public void removeEncounter() {
        if (encounter != null) {
            Encounter encounterTemp = encounter;
            encounter = null;
            if (encounterTemp.getResponses().contains(this)) {
                encounterTemp.removeResponse(this);
            }
        }
    }

    private String getUUID() {
        return this.uuid;
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Response)) {
            return false;
        }
        Response other = (Response) obj;
        return getUUID().equals(other.getUUID());
    }

    public ResponseDTO toResponseDTO() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setAnswerId(this.getAnswer().getId());
        responseDTO.setCustomtext(this.getCustomtext());
        responseDTO.setValue(this.getValue());
        responseDTO.setDate(this.getDate());
        responseDTO.setEnabled(true);

        List<PointOnImageDTO> pointOnImageDTOs = new ArrayList<>();
        for (PointOnImage currentPoint : this.pointsOnImage) {
            pointOnImageDTOs.add(currentPoint.toPointOnImageDTO());
        }
        responseDTO.setPointsOnImage(pointOnImageDTOs);

        return responseDTO;
    }
}
