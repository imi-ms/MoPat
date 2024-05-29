package de.imi.mopat.model.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class ConditionListDTO {

    private List<ConditionDTO> conditionDTOs = new ArrayList<>();
    private List<BundleDTO> availableBundleDTOs = new ArrayList<>();
    private List<BundleQuestionnaireDTO> availableBundleQuestionnaireDTOs = new ArrayList<>();

    public List<ConditionDTO> getConditionDTOs() {
        return conditionDTOs;
    }

    public void setConditionDTOs(List<ConditionDTO> conditionDTOs) {
        this.conditionDTOs = conditionDTOs;
    }

    /**
     * Returns the availableBundleDTOs which are necessary to show and assign the correct target
     * bundle, which contains the target questionnaire, for all {ConditionDTOs ConditionDTO} of this
     * {ConditionListDTO}.
     *
     * @return The conditionDTOs availableBundleDTOs as list object.
     */
    public List<BundleDTO> getAvailableBundleDTOs() {
        //Sort availableBundleDTOs by name
        Collections.sort(availableBundleDTOs, new Comparator<BundleDTO>() {
            @Override
            public int compare(BundleDTO o1, BundleDTO o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return availableBundleDTOs;
    }

    /**
     * Set the availableBundleDTOs to the {@link ConditionListDTO}.
     *
     * @param availableBundleDTOs The new list of availableBundleDTOs to set.
     */
    public void setAvailableBundleDTOs(final List<BundleDTO> availableBundleDTOs) {
        this.availableBundleDTOs = availableBundleDTOs;
    }

    /**
     * Returns the availableBundleQuestionnaireDTOs which are necessary to show and assign the
     * correct target questionnaire for all {ConditionDTOs ConditionDTO} of this
     * {ConditionListDTO}.
     *
     * @return The {ConditionListDTOs ConditionListDTO} availableBundleQuestionnaireDTOs as list
     * object.
     */
    public List<BundleQuestionnaireDTO> getAvailableBundleQuestionnaireDTOs() {
        return availableBundleQuestionnaireDTOs;
    }

    /**
     * Set the availableBundleQuestionnarieDTOs to the {@link ConditionListDTO}.
     *
     * @param availableBundleQuestionnaireDTOs The new list of availableBundleQuestionnaireDTOs to
     *                                         set.
     */
    public void setAvailableBundleQuestionnaireDTOs(
        final List<BundleQuestionnaireDTO> availableBundleQuestionnaireDTOs) {
        this.availableBundleQuestionnaireDTOs = availableBundleQuestionnaireDTOs;
    }
}
