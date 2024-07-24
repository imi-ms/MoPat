package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BundleService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(BundleService.class);

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    private BundleQuestionnaireDao bundleQuestionnaireDao;

    /**
     * Converts this {@link Bundle} object to an {@link BundleDTO} object.
     *
     * @param fullVersion Indicates whether the returned {@link BundleDTO}
     *                    object should include all data from the
     *                    {@link Bundle} object or not.
     * @return An {@link BundleDTO} object based on this {@link Bundle} object.
     */
    public BundleDTO toBundleDTO(final Boolean fullVersion, Bundle bundle) {
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
            // this.getBundleQuestionnaires() can never be null because it is
            // initialized with an empty HashMap when a Bundle is created
            List<BundleQuestionnaireDTO> bundleQuestionnaireDTOs = bundle.getBundleQuestionnaires().stream()
                    .filter(bundleQuestionnaire -> bundleQuestionnaire.getQuestionnaire().getId() != null)
                    .map(bundleQuestionnaire -> {
                        BundleQuestionnaireDTO bundleQuestionnaireDTO = bundleQuestionnaire.toBundleQuestionnaireDTO();
                        bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTOMapper.apply(bundleQuestionnaire.getQuestionnaire()));
                        bundleQuestionnaireDTO.setBundleId(bundle.getId());
                        return bundleQuestionnaireDTO;
                    })
                    .toList();

            bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaireDTOs);
        }
        return bundleDTO;
    }


    public List<BundleQuestionnaire> findByQuestionnaire(Long questionnaireID) {
        return bundleQuestionnaireDao.findByQuestionnaire(questionnaireID);
    }
}
