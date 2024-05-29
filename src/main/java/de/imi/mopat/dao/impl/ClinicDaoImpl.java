package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
public class ClinicDaoImpl extends MoPatDaoImpl<Clinic> implements ClinicDao {

    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private AclEntryDao aclEntryDao;
    @Autowired
    private UserDao userDao;

    @Override
    public boolean isClinicNameUnused(final String name, final Long id) {
        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT c FROM " + "Clinic c " + "WHERE c" + ".name='" + name + "'");
            Clinic clinic = (Clinic) query.getSingleResult();
            // If there is a result, check if it is the same questionniare
            // that should be edit
            return id != null && clinic.getId().longValue() == id.longValue();
        } catch (NoResultException e) {
            return true;
        }
    }

    @Override
    public Collection<Clinic> getClinicsFromAclObjectIdentitys(
        final Collection<AclObjectIdentity> aclClinics) {
        Set<Long> assignedClinicIds = new HashSet<>();
        for (AclObjectIdentity identity : aclClinics) {
            assignedClinicIds.add(identity.getObjectIdIdentity());
        }
        return this.getElementsById(assignedClinicIds);
    }

    @Override
    @Transactional("MoPat_User")
    public void grantInheritedRight(final Clinic clinic, final User user,
        final PermissionType right) {
        for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
            // If the given user has not the right to access the current bundle
            if (aclEntryDao.getEntryForObjectUserAndRight(bundleClinic.getBundle(), user, right)
                == null) {
                // Grant the given right for the current bundle to the user
                bundleDao.grantRight(bundleClinic.getBundle(), user, right, Boolean.FALSE);
            }
        }
    }

    @Override
    @Transactional("MoPat_User")
    public void revokeInheritedRight(final Clinic clinic, final User user,
        final PermissionType right) {
        for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
            AclEntry bundleUserAccess = aclEntryDao.getEntryForObjectUserAndRight(
                bundleClinic.getBundle(), user, right);
            // If the given user has the right to access the current bundle
            // and this right can be deleted
            if (bundleUserAccess != null && isInheritedRightDeletable(clinic,
                bundleClinic.getBundle(), user, right)) {
                // Revoke the given right for the current bundle from the user
                bundleDao.revokeRight(bundleClinic.getBundle(), user, right, Boolean.FALSE);
            }
        }
    }

    @Override
    @Transactional("MoPat_User")
    public void updateUserRights(final Clinic clinic, final List<Bundle> deletedBundles,
        final List<UserDTO> assignedUserDTOs) {
        // Get current user rights for the given clinic
        Map<User, PermissionType> clinicUserRights = aclEntryDao.getUserRightsByObject(clinic);

        // Revoke rights for deleted bundles
        for (Bundle bundle : deletedBundles) {
            for (Map.Entry<User, PermissionType> entry : clinicUserRights.entrySet()) {
                if (isInheritedRightDeletable(clinic, bundle, entry.getKey(), entry.getValue())) {
                    bundleDao.revokeRight(bundle, entry.getKey(), entry.getValue(), Boolean.FALSE);
                }
            }
        }
        // Grant rights for current Bundles
        for (Map.Entry<User, PermissionType> entry : clinicUserRights.entrySet()) {
            grantInheritedRight(clinic, entry.getKey(), entry.getValue());
        }

        // Get outdated users from retrieved user rights
        Set<User> deletedUsers = clinicUserRights.keySet();
        // Get current users from given clinicDTO
        List<User> currentUsers = new ArrayList<>();
        if (assignedUserDTOs != null) {
            for (UserDTO userDTO : assignedUserDTOs) {
                currentUsers.add(userDao.getElementById(userDTO.getId()));
            }
        }
        // Get deleted users
        deletedUsers.removeAll(currentUsers);
        // Revoke rights for deleted users
        for (User deletedUser : deletedUsers) {
            revokeRight(clinic, deletedUser, PermissionType.READ, Boolean.TRUE);
        }
        // Grant rights for current users
        for (User currentUser : currentUsers) {
            grantRight(clinic, currentUser, PermissionType.READ, Boolean.TRUE);
        }
    }

    /**
     * Checks if the given {@link PermissionType right} for the given {@link Bundle Bundle} can be
     * deleted based on the given {@link Clinic Clinic} and {@link User User}.
     *
     * @param clinic The {@link Clinic Clinic} for which the permission should be checked.
     * @param bundle The {@link Bundle Bundle} which permission should be checked.
     * @param user   The {@link User User} whose {@link PermissionType right} should be checked.
     * @param right  The {@link PermissionType right} which should be checked.
     * @return Returns true if the right can be deleted, otherwise false
     */
    public boolean isInheritedRightDeletable(final Clinic clinic, final Bundle bundle,
        final User user, final PermissionType right) {
        // Get all stored clinics and remove the given clinic
        List<Clinic> allClinics = getAllElements();
        allClinics.remove(clinic);
        for (Clinic currentClinic : allClinics) {
            for (BundleClinic bundleClinic : currentClinic.getBundleClinics()) {
                // If the bundle is part of another clinic
                if (bundleClinic.getBundle().equals(bundle)) {
                    // If the user has rights for another clinic, except the
                    // given clinic, the right cannot be removed
                    if (aclEntryDao.getEntryForObjectUserAndRight(currentClinic, user, right)
                        != null) {
                        return false;
                    }
                }
            }
        }
        // If the user owns this right only for the given clinic, this right
        // can be removed
        return true;
    }
}
