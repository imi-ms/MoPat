package de.imi.mopat.dao;

import de.imi.mopat.model.Bundle;
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

}