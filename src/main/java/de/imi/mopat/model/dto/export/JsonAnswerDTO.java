package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.ServletContextInfo;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.enumeration.BodyPart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class represents the data transfer obejct of model {@link Answer} to convert a model to json
 * for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("answer")
@JsonIgnoreProperties(value = {"showTooltip"})
public class JsonAnswerDTO {

    private Long id = null;
    private JsonQuestionDTO jsonQuestionDTO = null;
    private String startDate = null;
    private String endDate = null;
    private Double value = null;
    // Numerical value used for scoring; can be null since scoring is
    // not mandatory
    private String codedValue = null;
    private Double minValue = null;   //Lowest point for slider questions
    private Double maxValue = null;   // Highest point for slider questions
    private String stepsize = null; // Used for slider and numberInput questions
    private String imageBase64 = null;
    //The image is assigned to the created answer after merging, because the
    // path depends on the answer's question id.
    //So the imagePath property is used to identify the corresponding
    // jsonAnswerDTO to assign it to the merged answer because there is no
    // other property to relate the two objects
    private String imagePath = null;
    private Map<String, String> localizedLabel = new HashMap<>();
    private Map<String, String> localizedMaximumText = new HashMap<>();
    private Map<String, String> localizedMinimumText = new HashMap<>();
    private Map<String, String> localizedFreetextLabel = new HashMap<>();

    private Boolean vertical = null;
    private Boolean showValueOnButton = null;
    private Boolean isEnabled = Boolean.TRUE;
    private Boolean isOther = null;

    private BodyPart bodyPart;

    private Boolean showIcons = null;

    private Set<SliderIcon> icons = new HashSet<>();
    private List<JsonConditionDTO> conditions = new ArrayList<>();

    public JsonAnswerDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JsonQuestionDTO getJsonQuestionDTO() {
        return jsonQuestionDTO;
    }

    public void setJsonQuestionDTO(final JsonQuestionDTO jsonQuestionDTO) {
        this.jsonQuestionDTO = jsonQuestionDTO;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    public String getCodedValue() {
        return codedValue;
    }

    public void setCodedValue(final String codedValue) {
        this.codedValue = codedValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(final Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getStepsize() {
        return stepsize;
    }

    public void setStepsize(final String stepsize) {
        this.stepsize = stepsize;
    }

    public Map<String, String> getLocalizedLabel() {
        return localizedLabel;
    }

    public void setLocalizedLabel(final Map<String, String> localizedLabel) {
        this.localizedLabel = localizedLabel;
    }

    public Map<String, String> getLocalizedMaximumText() {
        return localizedMaximumText;
    }

    public void setLocalizedMaximumText(final Map<String, String> localizedMaximumText) {
        this.localizedMaximumText = localizedMaximumText;
    }

    public Map<String, String> getLocalizedMinimumText() {
        return localizedMinimumText;
    }

    public void setLocalizedMinimumText(final Map<String, String> localizedMinimumText) {
        this.localizedMinimumText = localizedMinimumText;
    }

    public Map<String, String> getLocalizedFreetextLabel() {
        return localizedFreetextLabel;
    }

    public void setLocalizedFreetextLabel(final Map<String, String> localizedFreetextLabel) {
        this.localizedFreetextLabel = localizedFreetextLabel;
    }

    public Boolean getVertical() {
        return vertical;
    }

    public void setVertical(final Boolean vertical) {
        this.vertical = vertical;
    }

    public Boolean getShowValueOnButton() {
        return showValueOnButton;
    }

    public void setShowValueOnButton(final Boolean showValueOnButton) {
        this.showValueOnButton = showValueOnButton;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Boolean getIsOther() {
        return isOther;
    }

    public void setIsOther(final Boolean isOther) {
        this.isOther = isOther;
    }

    public List<JsonConditionDTO> getConditions() {
        return conditions;
    }

    public void setConditions(final List<JsonConditionDTO> conditions) {
        this.conditions = conditions;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(final String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(final BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }

    public void setShowIcons(final Boolean showIcons) {
        this.showIcons = showIcons;
    }

    public Boolean getShowIcons() {
        return showIcons;
    }

    public Set<SliderIcon> getIcons() {
        return icons;
    }

    public void addCondition(JsonConditionDTO jsonConditionDTO){
        this.conditions.add(jsonConditionDTO);
    }

    public void setIcons(final Set<SliderIcon> icons) {
        this.icons = icons;
    }

    /**
     * Convert instance of this class to {@link Answer} object.
     *
     * @param question {@link Question} object the answer belongs to.
     * @return Object of model {@link Answer}.
     */
    public Answer convertToAnswer(final Question question) {
        Answer answer = null;
        switch (this.getJsonQuestionDTO().getQuestionType()) {
            case MULTIPLE_CHOICE:
            case DROP_DOWN:
                // Differenciate between select and freetext answer (only
                // select answers have a localized label)
                if (this.getLocalizedLabel() != null && !this.getLocalizedLabel().isEmpty()) {
                    SelectAnswer selectAnswer = new SelectAnswer(question, this.getIsEnabled(),
                        this.getLocalizedLabel(), this.getIsOther());
                    selectAnswer.setValue(this.getValue());
                    selectAnswer.setCodedValue(this.getCodedValue());
                    answer = selectAnswer;
                } else {
                    answer = new FreetextAnswer(question, this.getIsEnabled());
                }
                break;
            case DATE:
                try {
                    if (this.getStartDate() != null && this.getEndDate() == null) {
                        answer = new DateAnswer(question, this.getIsEnabled(),
                            Constants.DATE_FORMAT.parse(this.getStartDate()), null);
                    } else if (this.getStartDate() == null && this.getEndDate() != null) {
                        answer = new DateAnswer(question, this.getIsEnabled(), null,
                            Constants.DATE_FORMAT.parse(this.getStartDate()));
                    } else if (this.getStartDate() != null && this.getEndDate() != null) {
                        answer = new DateAnswer(question, this.getIsEnabled(),
                            Constants.DATE_FORMAT.parse(this.getStartDate()),
                            Constants.DATE_FORMAT.parse(this.getEndDate()));
                    } else {
                        answer = new DateAnswer(question, this.getIsEnabled(), null, null);
                    }
                } catch (ParseException e) {
                    answer = new DateAnswer(question, this.getIsEnabled(), null, null);
                }
                break;
            case FREE_TEXT:
            case BARCODE:
                answer = new FreetextAnswer(question, this.getIsEnabled());
                break;
            case IMAGE:
                answer = new ImageAnswer(question, this.getIsEnabled(), this.getImagePath());
                byte[] imageByteArray = Base64.decodeBase64(this.getImageBase64());
                File image = new File(this.getImagePath());
                try {
                    if (!image.exists()) {
                        image.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(image);
                    fos.write(imageByteArray);
                } catch (IOException e) {
                }
                break;
            case BODY_PART:
                answer = new BodyPartAnswer(this.getBodyPart(), question, this.getIsEnabled());
                break;
            case NUMBER_INPUT:
                Double stepsize = null;
                if (this.getStepsize() != null) {
                    Double.parseDouble(this.getStepsize());
                }
                answer = new NumberInputAnswer(question, this.getIsEnabled(), this.getMinValue(),
                    this.getMaxValue(), stepsize);
                break;
            case SLIDER:
            case NUMBER_CHECKBOX:
                SliderAnswer sliderAnswer = new SliderAnswer(question, this.getIsEnabled(),
                    this.getMinValue(), this.getMaxValue(), Double.parseDouble(this.getStepsize()),
                    this.getVertical());
                sliderAnswer.setLocalizedMinimumText(this.getLocalizedMinimumText());
                sliderAnswer.setLocalizedMaximumText(this.getLocalizedMaximumText());
                sliderAnswer.setShowValueOnButton(this.getShowValueOnButton());
                sliderAnswer.setShowIcons(this.getShowIcons());
                Set<SliderIcon> iconSet = new HashSet<>();
                for (SliderIcon icon : this.getIcons()) {
                    SliderIcon newIcon = new SliderIcon(icon.getPosition(), icon.getIcon(),
                        sliderAnswer);
                    iconSet.add(newIcon);
                }
                sliderAnswer.setIcons(iconSet);
                answer = sliderAnswer;
                break;
            case NUMBER_CHECKBOX_TEXT:
                SliderFreetextAnswer sliderFreetextAnswer = new SliderFreetextAnswer(question,
                    this.getIsEnabled(), this.getMinValue(), this.getMaxValue(),
                    Double.parseDouble(this.getStepsize()), this.getLocalizedFreetextLabel(),
                    this.getVertical());
                sliderFreetextAnswer.setLocalizedMinimumText(this.getLocalizedMinimumText());
                sliderFreetextAnswer.setLocalizedMaximumText(this.getLocalizedMaximumText());
                answer = sliderFreetextAnswer;
                break;
            case INFO_TEXT:
                break;
            default:
                break;
        }

        return answer;
    }
}
