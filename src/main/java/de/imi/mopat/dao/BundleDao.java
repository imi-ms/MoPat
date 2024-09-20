package de.imi.mopat.dao;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Questionnaire;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface BundleDao extends MoPatDao<Bundle> {

    /**
     * Check if the given name is free for usage.
     *
     * @param name The name which will be checked
     * @param id   The id of the bundle which should be saved
     * @return true if the name is free for usage and false otherwise
     */
    boolean isBundleNameUnused(String name, Long id);

    /**
     * Method to fetch available {@link Questionnaire} instances for a {@link Bundle} ID.
     * Available means all questionnaires that are not already assigned to the bundle.
     * @param bundleId to find questionnaires for
     * @return {@link List} of {@link Questionnaire} objects
     */
    List<Questionnaire> getAvailableQuestionnairesForBundle(Long bundleId);

}