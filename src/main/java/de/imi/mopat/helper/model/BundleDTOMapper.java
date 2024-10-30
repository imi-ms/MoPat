package de.imi.mopat.helper.model;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiFunction;

@Component
public class BundleDTOMapper implements BiFunction<Boolean, Bundle, BundleDTO> {

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    /**
     * Converts this {@link Bundle} object to an {@link BundleDTO} object.
     *
     * @param fullVersion Indicates whether the returned {@link BundleDTO}
     *                    object should include all data from the
     *                    {@link Bundle} object or not.
     * @return An {@link BundleDTO} object based on this {@link Bundle} object.
     */
    @Override
    public BundleDTO apply(Boolean fullVersion, Bundle bundle) {
        BundleDTO bundleDTO = new BundleDTO();
        bundleDTO.setId(bundle.getId());
        bundleDTO.setName(bundle.getName());
        bundleDTO.setAvailableLanguages(bundle.getAvailableLanguages());
        if (fullVersion) {
            bundleDTO.setIsPublished(bundle.getIsPublished());
            bundleDTO.setIsModifiable(bundle.isModifiable());
            bundleDTO.setDescription(bundle.getDescription());
            bundleDTO.setLocalizedWelcomeText(new TreeMap<>(bundle.getLocalizedWelcomeText()));
            bundleDTO.setLocalizedFinalText(new TreeMap<>(bundle.getLocalizedFinalText()));
            bundleDTO.setdeactivateProgressAndNameDuringSurvey(bundle.getDeactivateProgressAndNameDuringSurvey());
            bundleDTO.setShowProgressPerBundle(bundle.getShowProgressPerBundle());
            List<BundleQuestionnaireDTO> bundleQuestionnaireDTOs =
                    new ArrayList<>();
            // this.getBundleQuestionnaires() can never be null because it is
            // initialized with an empty HashMap when a Bundle is created
            if (!bundle.getBundleQuestionnaires()
                    .isEmpty()) {
                for (BundleQuestionnaire bundleQuestionnaire :
                        bundle.getBundleQuestionnaires()) {
                    if (bundleQuestionnaire.getQuestionnaire()
                            .getId()
                            == null) {
                        continue;
                    }
                    BundleQuestionnaireDTO bundleQuestionnaireDTO =
                            bundleQuestionnaire.toBundleQuestionnaireDTO();
                    bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTOMapper.apply(bundleQuestionnaire.getQuestionnaire()));
                    bundleQuestionnaireDTO.setBundleId(bundle.getId());
                    bundleQuestionnaireDTOs.add(bundleQuestionnaireDTO);
                }
            }
            bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaireDTOs);
        }
        return bundleDTO;
    }
}