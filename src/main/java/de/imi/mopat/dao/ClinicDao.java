package de.imi.mopat.dao;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.UserDTO;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface ClinicDao extends MoPatDao<Clinic> {

    /**
     * Check whether the given name is free for usage.
     *
     * @param name The name which will be checked
     * @param id   The id of the clinic which should be saved
     * @return True if the name is not in use false otherwise
     */
    boolean isClinicNameUnused(String name, Long id);

    /**
     * Returns a collection of {@link Clinic} objects from a given collection of
     * {@link AclObjectIdentity} objects.
     *
     * @param aclClinics A collection of {@link AclObjectIdentity} objects.
     * @return Returns a collection of {@link Clinic} objects.
     */
    Collection<Clinic> getClinicsFromAclObjectIdentitys(Collection<AclObjectIdentity> aclClinics);

    /**
     * Updates the {@link de.imi.mopat.model.enumeration.PermissionType rights} of all
     * {@link de.imi.mopat.model.user.User Users} for the given {@link Clinic}.
     *
     * @param clinic           The clinic on which the
     *                         {@link de.imi.mopat.model.enumeration.PermissionType rights} should
     *                         be updated.
     * @param deletedBundles   The just deleted bundles for which the rights should be revoked for
     *                         all users.
     * @param assignedUserDTOs The assigned users for the given clinic.
     */
    void updateUserRights(Clinic clinic, List<Bundle> deletedBundles,
        List<UserDTO> assignedUserDTOs);
}
