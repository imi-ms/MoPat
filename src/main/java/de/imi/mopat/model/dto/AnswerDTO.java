package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.validator.DateFormatForString;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public class AnswerDTO {

    private Long id = null;
    @DateFormatForString(value = "yyyy-MM-dd", message = "{dateAnswer.startDate.wrongFormat}")
    private String startDate = null;
    @DateFormatForString(value = "yyyy-MM-dd", message = "{dateAnswer.endDate.wrongFormat}")
    private String endDate = null;
    private Double value = null;
    // Numerical value used for scoring; can be null since scoring is
    // not mandatory
    private String codedValue = null; // Coded vlaue for select questions
    private Double minValue = null;   //Lowest point for slider questions
    private Double maxValue = null;   // Highest point for slider questions
    private String stepsize = null; // Used for slider and numberInput questions
    @JsonIgnore
    private MultipartFile imageFile = null; // Used for image questions
    private String imagePath = null;
    private String imageBase64 = null;

    private SortedMap<String, String> localizedLabel = null;
    private SortedMap<String, String> localizedMaximumText = null;
    private SortedMap<String, String> localizedMinimumText = null;
    private SortedMap<String, String> localizedFreetextLabel = null;

    private Boolean showIcons = null;
    private List<SliderIconDTO> icons = new ArrayList<>();
    private SliderIconConfigDTO sliderIconConfigDTO = null;
    private Boolean vertical = null;
    private Boolean showValueOnButton = null;
    private Boolean isEnabled = Boolean.TRUE;
    private Boolean isOther = Boolean.FALSE;
    private Boolean hasResponse = Boolean.FALSE;
    private Boolean hasExportRule = Boolean.FALSE;
    private Boolean hasConditionsAsTrigger = Boolean.FALSE;
    private Boolean hasConditionsAsTarget = Boolean.FALSE;
    // String that contains the messageCode for the bodyPart
    private String bodyPartMessageCode = null;
    // String that contains the html code of the svg path element
    // to draw the represented body region of the BodyPartAnswer
    private String bodyPartPath = null;
    // Contains the path to the image this answer belongs to
    private String bodyPartImage = null;

    private List<ConditionDTO> conditions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * {
     *
     * @return The earliest date the user can choose. Can be
     * <code>null</code>
     * @see de.imi.mopat.model.DateAnswer#getStartDate()}
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * {
     *
     * @param startDate Sets the earliest date the user can choose. Can be
     *                  <code>null</code>
     * @see de.imi.mopat.model.DateAnswer#setStartDate(java.util.Date startDate)}
     */
    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    /**
     * {
     *
     * @return Gets the latest date the user can choose. Can be
     * <code>null</code>
     * @see de.imi.mopat.model.DateAnswer#getEndDate()}
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * {
     *
     * @param endDate the latest date the user can choose. Can be
     *                <code>null</code>
     * @see de.imi.mopat.model.DateAnswer#setEndDate(java.util.Date endDate)}
     */
    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    /**
     * {
     *
     * @return value Might be <code>null</code>.
     * @see de.imi.mopat.model.SelectAnswer#getValue()}
     */
    public Double getValue() {
        return value;
    }

    /**
     * {
     *
     * @param value Can be <code>null</code>.
     * @see de.imi.mopat.model.SelectAnswer#setValue(Double value)}
     */
    public void setValue(final Double value) {
        this.value = value;
    }

    /**
     * {
     *
     * @return value Might be <code>null</code>.
     * @see de.imi.mopat.model.SelectAnswer#getCodedValue()}
     */
    public String getCodedValue() {
        return codedValue;
    }

    /**
     * {
     *
     * @param codedValue Can be <code>null</code>.
     * @see de.imi.mopat.model.SelectAnswer#setCodedValue(String codedValue)}
     */
    public void setCodedValue(final String codedValue) {
        this.codedValue = codedValue;
    }

    /**
     * {
     *
     * @return The lowest point for this slider question.. Is never <code>null</code>.
     * @see de.imi.mopat.model.NumberInputAnswer#getMinValue()} {
     * @see de.imi.mopat.model.SliderAnswer#getMinValue()}
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * {
     *
     * @param minValue The new lowest point for a slider question. Must not be
     *                 <code>null</code>. Has to be lower than the maximum.
     * @see de.imi.mopat.model.NumberInputAnswer#setMinValue(Double minValue)} {
     * @see de.imi.mopat.model.SliderAnswer#setMinValue(Double minValue)}
     */
    public void setMinValue(final Double minValue) {
        this.minValue = minValue;
    }

    /**
     * {
     *
     * @return The highest point for this slider question. Is never
     * <code>null</code>.
     * @see de.imi.mopat.model.NumberInputAnswer#getMaxValue()} {
     * @see de.imi.mopat.model.SliderAnswer#getMaxValue()}
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * {
     *
     * @param maxValue The new highest point for a slider question. Must not be
     *                 <code>null</code>. Has to be greater than the minimum.
     * @see de.imi.mopat.model.NumberInputAnswer#setMaxValue(Double minValue)} {
     * @see de.imi.mopat.model.SliderAnswer#setMaxValue(Double minValue)}
     */
    public void setMaxValue(final Double maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * {
     *
     * @return Returns the step size of the slider or the step size of the number input. Is never
     * <code>0.0</code>. Is never negative (&lt; 0). Is never <code>null</code>.
     * @see de.imi.mopat.model.NumberInputAnswer#getStepsize()} {
     * @see de.imi.mopat.model.SliderAnswer#getStepsize()}
     */
    public String getStepsize() {
        return stepsize;
    }

    /**
     * {
     *
     * @param stepsize The new step size of the slider / the number input. Must not be
     *                 <code>null</code>. Must not be <code>&lt;= 0</code>.
     * @see de.imi.mopat.model.NumberInputAnswer#setStepsize(Double stepsize)} {
     * @see de.imi.mopat.model.SliderAnswer#setStepsize(Double stepsize)}
     */
    public void setStepsize(final String stepsize) {
        this.stepsize = stepsize;
    }

    /**
     * Get the image file of the {ImageAnswer}.
     *
     * @return Image file of the {ImageAnswer}.
     */
    public MultipartFile getImageFile() {
        return imageFile;
    }

    /**
     * Set the image file of the {ImageAnswer}
     *
     * @param imageFile Image file of the {ImageAnswer}.
     */
    public void setImageFile(final MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    /**
     * {
     *
     * @return Might be <code>null</code>.
     * @see de.imi.mopat.model.ImageAnswer#getImagePath()}
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * {
     *
     * @param imagePath Can be <code>null</code>.
     * @see de.imi.mopat.model.ImageAnswer#setImagePath(String imagePath)}
     */
    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Gets the image of an {ImageAnswer} coded as base64 String.
     *
     * @return Image coded as base64 String.
     */
    public String getImageBase64() {
        return imageBase64;
    }

    /**
     * Sets the image of an {ImageAnswer} coded as base64 String.
     *
     * @param imageBase64 Image coded as base64 String
     */
    public void setImageBase64(final String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    /**
     * {
     *
     * @return A map with localized display texts of the {Answer}. Is never <code>null</code>. Is
     * not empty.
     * @see de.imi.mopat.model.SelectAnswer#getLocalizedLabel()}
     */
    public SortedMap<String, String> getLocalizedLabel() {
        return localizedLabel;
    }

    /**
     * {
     *
     * @param localizedLabel The map with the new localized display texts of the {Answer}. Must not
     *                       be <code>null</code>. Must not be empty.
     * @see de.imi.mopat.model.SelectAnswer#setLocalizedLabel(java.util.Map localizedLabel)}
     */
    public void setLocalizedLabel(final SortedMap<String, String> localizedLabel) {
        this.localizedLabel = localizedLabel;
    }

    /**
     * {
     *
     * @return might be <code>null</code>, might be empty (<code>""</code> after trimming). Will not
     * be trimmed
     * @see de.imi.mopat.model.SliderAnswer#getLocalizedMaximumText()}
     */
    public SortedMap<String, String> getLocalizedMaximumText() {
        return localizedMaximumText;
    }

    /**
     * {
     *
     * @param localizedMaximumText can be <code>null</code>, can be empty.
     * @see de.imi.mopat.model.SliderAnswer#setLocalizedMaximumText(java.util.Map
     * localizedMaximumText)}
     */
    public void setLocalizedMaximumText(final SortedMap<String, String> localizedMaximumText) {
        this.localizedMaximumText = localizedMaximumText;
    }

    /**
     * {
     *
     * @return might be <code>null</code>, might be empty.
     * @see de.imi.mopat.model.SliderAnswer#getLocalizedMinimumText()}
     */
    public SortedMap<String, String> getLocalizedMinimumText() {
        return localizedMinimumText;
    }

    /**
     * {
     *
     * @param localizedMinimumText can be <code>null</code>, can be empty.
     * @see de.imi.mopat.model.SliderAnswer#setLocalizedMinimumText(java.util.Map
     * localizedMinimumText)}
     */
    public void setLocalizedMinimumText(final SortedMap<String, String> localizedMinimumText) {
        this.localizedMinimumText = localizedMinimumText;
    }

    /**
     * {
     *
     * @return Is never <code>null</code>. Is never empty.
     * @see de.imi.mopat.model.SliderFreetextAnswer#getLocalizedFreetextLabel()}
     */
    public SortedMap<String, String> getLocalizedFreetextLabel() {
        return localizedFreetextLabel;
    }

    /**
     * {
     *
     * @param localizedFreetextLabel must not be <code>null</code>. Must not be empty.
     * @see de.imi.mopat.model.SliderFreetextAnswer#setLocalizedFreetextLabel(java.util.Map
     * localizedFreetextLabel)}
     */
    public void setLocalizedFreetextLabel(final SortedMap<String, String> localizedFreetextLabel) {
        this.localizedFreetextLabel = localizedFreetextLabel;
    }

    /**
     * @return might be <code>null</code>, might be empty.
     * @see de.imi.mopat.model.SliderAnswer#getVertical()}
     */
    public Boolean getVertical() {
        return vertical;
    }

    /**
     * @param vertical can be <code>null</code>, can be empty.
     * @see de.imi.mopat.model.SliderAnswer#setVertical(Boolean vertical)}
     */
    public void setVertical(final Boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * {
     *
     * @return might be <code>null</code>
     * @see de.imi.mopat.model.SliderAnswer#getShowValueOnButton()}
     */
    public Boolean getShowValueOnButton() {
        return showValueOnButton;
    }

    /**
     * {
     *
     * @param showValueOnButton can be <code>null</code>
     * @see de.imi.mopat.model.SliderAnswer#setShowValueOnButton(Boolean showValueOnButton)}
     */
    public void setShowValueOnButton(final Boolean showValueOnButton) {
        this.showValueOnButton = showValueOnButton;
    }

    /**
     * {
     *
     * @return The current value of this answer's object property isEnabled.
     * @see de.imi.mopat.model.Answer#getIsEnabled()}
     */
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    /**
     * {
     *
     * @param isEnabled The new value to set.
     * @see de.imi.mopat.model.Answer#setIsEnabled(Boolean isEnabled)}
     */
    public void setIsEnabled(final Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Get Boolean value that gives information about if there is any {Response} attached to this
     * answer.
     *
     * @return True if there is any {Response} attached to this answer, otherwise false.
     */
    public Boolean getHasResponse() {
        return hasResponse;
    }

    /**
     * Set Boolean value that gives information about if there is any {Response} attached to this
     * answer.
     *
     * @param hasResponse True if there is any {Response} attached to this answer, otherwise false.
     */
    public void setHasResponse(final Boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    /**
     * Get Boolean value that gives information about if there is any {ExportRule} attached to this
     * answer.
     *
     * @return True if there is any {ExportRule} attached to this answer, otherwise false.
     */
    public Boolean getHasExportRule() {
        return hasExportRule;
    }

    /**
     * Set Boolean value that gives information about if there is any {ExportRule} attached to this
     * answer.
     *
     * @param hasExportRule True if there is any {ExportRule} attached to this answer, otherwise
     *                      false.
     */
    public void setHasExportRule(final Boolean hasExportRule) {
        this.hasExportRule = hasExportRule;
    }

    /**
     * Get Boolean value that gives information about if this answer triggers any condition.
     *
     * @return True if there are conditions this answer triggers, otherwise false.
     */
    public Boolean getHasConditionsAsTrigger() {
        return hasConditionsAsTrigger;
    }

    /**
     * Set Boolean value that gives information about if this answer triggers any condition.
     *
     * @param hasConditionsAsTrigger is True if there are conditions this answer triggers, otherwise
     *                               false.
     */
    public void setHasConditionsAsTrigger(final Boolean hasConditionsAsTrigger) {
        this.hasConditionsAsTrigger = hasConditionsAsTrigger;
    }

    /**
     * Get Boolean value that gives information about if this answer has conditions that target to
     * it.
     *
     * @return True if there are conditions that are targeting this answer, otherwise false.
     */
    public Boolean getHasConditionsAsTarget() {
        return hasConditionsAsTarget;
    }

    /**
     * Set Boolean value that gives information about if this answer has conditions that target to
     * it.
     *
     * @param hasConditionsAsTarget is true if there are conditions that are targeting this answer,
     *                              otherwise false.
     */
    public void setHasConditionsAsTarget(final Boolean hasConditionsAsTarget) {
        this.hasConditionsAsTarget = hasConditionsAsTarget;
    }

    /**
     * Gets the conditions that are triggered by this answer.
     *
     * @return Conditions this answer triggers.
     */
    public List<ConditionDTO> getConditions() {
        return conditions;
    }

    /**
     * Set the conditions that are triggered by this answer.
     *
     * @param conditions Conditions this answer triggers.
     */
    public void setConditions(final List<ConditionDTO> conditions) {
        this.conditions = conditions;
    }

    /**
     * Returns the messageCode of the BodyPart this answer represents.
     *
     * @return MessageCode of the BodyPart.
     */
    public String getBodyPartMessageCode() {
        return bodyPartMessageCode;
    }

    /**
     * Sets the messageCode fo the BodyPart this answer represents.
     *
     * @param bodyPartMessageCode MessageCode of the BodyPart.
     */
    public void setBodyPartMessageCode(final String bodyPartMessageCode) {
        this.bodyPartMessageCode = bodyPartMessageCode;
    }

    /**
     * Gets the path element represented as html String of the BodyPart this answer represents.
     *
     * @return Path element as html String of the BodyPart.
     */
    public String getBodyPartPath() {
        return bodyPartPath;
    }

    /**
     * Sets the path element represented as html String of the BodyPart this answer represents.
     *
     * @param bodyPartPath Path element as html String of the BodyPart.
     */
    public void setBodyPartPath(final String bodyPartPath) {
        this.bodyPartPath = bodyPartPath;
    }

    /**
     * Gets the path to the image of the BodyPart this answer represents.
     *
     * @return Path to the image of the BodyPart.
     */
    public String getBodyPartImage() {
        return bodyPartImage;
    }

    /**
     * Sets the path to the image of the BodyPart this answer represents.
     *
     * @param bodyPartImage Path to the image of the BodyPart.
     */
    public void setBodyPartImage(final String bodyPartImage) {
        this.bodyPartImage = bodyPartImage;
    }

    public Boolean getIsOther() {
        return isOther;
    }

    public void setIsOther(final Boolean isOther) {
        this.isOther = isOther;
    }

    /**
     * Gets the flag if icons should be shown for the answer.
     *
     * @return Boolean
     */
    public Boolean getShowIcons() {
        return showIcons;
    }

    /**
     * Sets the flag if icons should be shown for the answer.
     *
     * @param showIcons Boolean
     */
    public void setShowIcons(final Boolean showIcons) {
        this.showIcons = showIcons;
    }

    /**
     * Gets the List with all icons for the AnswerDTO.
     *
     * @return List with SliderIconDTOs
     */
    public List<SliderIconDTO> getIcons() {
        return icons;
    }

    /**
     * Sets the List with all icons for the AnswerDTO.
     *
     * @param icons List with SliderIconDTOs
     */
    public void setIcons(final List<SliderIconDTO> icons) {
        this.icons = icons;
    }
    /**
     * Gets the icon config for Answer.
     *
     * @return sliderIconConfigDTO
     */
    public SliderIconConfigDTO getSliderIconConfigDTO() {
        return sliderIconConfigDTO;
    }

    /**
     * set slider icon config dto
     *
     * @param sliderIconConfigDTO slider icon config
     */
    public void setSliderIconConfigDTO(final SliderIconConfigDTO sliderIconConfigDTO) {
        this.sliderIconConfigDTO = sliderIconConfigDTO;
    }
}
