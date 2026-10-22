package de.imi.mopat.service;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EncounterScheduledService {

    @Autowired
    private EncounterScheduledDao encounterScheduledDao;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private AclEntryDao aclEntryDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    private AclClassDao aclClassDao;

    /**
     * Grants read access to the given {@link EncounterScheduled} for all users of the specified
     * {@link Clinic} who have at least the {@code ROLE_ENCOUNTERMANAGER} role.
     *
     * @param clinic             the clinic whose users should be granted rights
     * @param encounterScheduled the encounter scheduled instance
     */
    @Transactional("MoPat_User")
    public void grantUserRightsForEncounterScheduledByClinicUsers(Clinic clinic,
        EncounterScheduled encounterScheduled) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();

        persistNewAclObjectIdentityForEncounterScheduledIfNotExist(encounterScheduled, currentUser);

        updateEncounterScheduledUserRightsForClinic(clinic, encounterScheduled);

    }

    /**
     * Refreshes ACLs for all existing {@link EncounterScheduled} instances. Creates missing
     * {@link AclObjectIdentity} entries and syncs read permissions for clinic users with the
     * {@code ROLE_ENCOUNTERMANAGER} role.
     */
    @Transactional("MoPat_User")
    public void refreshAllUserRightsForEncounterScheduled() {
        List<EncounterScheduled> encounterScheduleds = encounterScheduledDao.getAllElements();

        AclClass aclClass = aclClassDao.getElementByClass(EncounterScheduled.class.getName());
        if (aclClass == null) {
            throw new RuntimeException(
                "AclClass for EncounterScheduled not found. Please register it first.");
        }

        //Function triggerer becomes owner of all newly created ACLs
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();

        for (EncounterScheduled encounterScheduled : encounterScheduleds) {
            AclObjectIdentity objectIdentity =
                aclObjectIdentityDao.getElementByClassAndObjectId(aclClass,
                    encounterScheduled.getId());

            if (objectIdentity == null) {
                //Object Identity does not exist, create it first
                persistNewAclObjectIdentityForEncounterScheduledIfNotExist(encounterScheduled, currentUser);
            }

            Clinic clinic = encounterScheduled.getClinic();

            updateEncounterScheduledUserRightsForClinic(clinic, encounterScheduled);

        }
    }

    /**
     * Deletes all {@link de.imi.mopat.model.user.AclEntry} for an {@link EncounterScheduled}
     * instance. This function is necessary when deleting an old {@link EncounterScheduled} from the
     * app to avoid bloat and memory leaks.
     *
     * @param encounterScheduled to delete ACL entries for
     */
    public void deleteEncounterScheduledAclEntries(EncounterScheduled encounterScheduled) {
        Map<User, PermissionType> encounterScheduledRights = aclEntryDao.getUserRightsByObject(
            encounterScheduled);

        for (User userToRevoke : encounterScheduledRights.keySet()) {
            encounterScheduledDao.revokeRight(encounterScheduled, userToRevoke, PermissionType.READ,
                Boolean.TRUE);
        }

        AclObjectIdentity objectIdentity = aclObjectIdentityDao.getElementByClassAndObjectId(
            aclClassDao.getElementByClass(EncounterScheduled.class.getName()),
            encounterScheduled.getId()
        );
        aclObjectIdentityDao.remove(objectIdentity);
    }

    /**
     * Updates read permissions for all {@link EncounterScheduled} instances belonging to the given {@link Clinic}.
     * Re-evaluates and grants read access to users with at least the {@code ROLE_ENCOUNTERMANAGER} role.
     *
     * @param clinic the clinic whose encounter scheduleds should be updated
     */
    public void updateEncounterScheduledUserRightsForClinic(Clinic clinic) {
        encounterScheduledDao.findByClinicIdIn(List.of(clinic.getId())).forEach(es -> {
            updateEncounterScheduledUserRightsForClinic(clinic, es);
        });
    }

    /**
     * Updates read permissions for the given {@link EncounterScheduled} based on the current user
     * rights of the associated {@link Clinic}. Only users with at least the
     * {@code ROLE_ENCOUNTERMANAGER} role are granted read access.
     *
     * @param clinic             the clinic whose users are checked for roles
     * @param encounterScheduled the encounter scheduled instance to update permissions for
     */
    public void updateEncounterScheduledUserRightsForClinic(Clinic clinic,
        EncounterScheduled encounterScheduled) {
        Map<User, PermissionType> rightsByClinic = aclEntryDao.getUserRightsByObject(
            clinic);

        //Add rights for users in clinic
        for (User user : rightsByClinic.keySet()) {
            if (user.hasAtLeastRole(UserRole.ROLE_ENCOUNTERMANAGER)) {
                encounterScheduledDao.grantRight(encounterScheduled, user, PermissionType.READ,
                    false);
            }
        }

        // Remove Users that are not in clinic but have rights
        aclEntryDao.getUserRightsByObject(encounterScheduled)
            .keySet()
            .stream().filter(user ->
                !rightsByClinic.containsKey(user)
            ).toList()
            .forEach(userToRevoke -> {
                encounterScheduledDao.revokeRight(encounterScheduled, userToRevoke,
                    PermissionType.READ, false);
            });
    }


    /**
     * Grants read access to all {@link EncounterScheduled} instances belonging to the given {@link Clinic}
     * for the specified {@link User}.
     *
     * @param clinic the clinic whose encounter scheduleds are affected
     * @param user the user to grant rights to
     */
    public void addUserRightsForEncounterScheduledsOfClinic(Clinic clinic, User user) {
        if (user.hasAtLeastRole(UserRole.ROLE_ENCOUNTERMANAGER)) {
            encounterScheduledDao.findByClinicIdIn(List.of(clinic.getId())).forEach(es -> {
                persistNewAclObjectIdentityForEncounterScheduledIfNotExist(es, user);
                encounterScheduledDao.grantRight(es, user, PermissionType.READ, false);
            });
        }
    }

    /**
     * Updates ACLs for all {@link EncounterScheduled} instances across all clinics assigned to the user.
     * Grants read access if the user has at least the {@code ROLE_ENCOUNTERMANAGER} role, otherwise revokes it.
     *
     * @param user the user whose rights should be updated
     */
    public void updateUserRightsForEncounterScheduledsOfAllAssignedClinics(User user) {
        aclEntryDao.getClinicsForUser(user).forEach(clinic -> {
                if (user.hasAtLeastRole(UserRole.ROLE_ENCOUNTERMANAGER)) {
                    addUserRightsForEncounterScheduledsOfClinic(clinic, user);
                } else {
                    removeUserRightsForEncounterScheduledsOfClinic(clinic, user);
                }
        });
    }

    /**
     * Revokes read access from all {@link EncounterScheduled} instances belonging to the given {@link Clinic}
     * for the specified {@link User}.
     *
     * @param clinic the clinic whose encounter scheduleds are affected
     * @param user the user to revoke rights from
     */
    public void removeUserRightsForEncounterScheduledsOfClinic(Clinic clinic, User user) {
        encounterScheduledDao.findByClinicIdIn(List.of(clinic.getId())).forEach(es -> {
            encounterScheduledDao.revokeRight(es, user, PermissionType.READ, false);
        });
    }


    /**
     * Persists a new {@link AclObjectIdentity} for the given {@link EncounterScheduled}.
     *
     * @param encounterScheduled the encounter scheduled instance
     * @param currentUser        the user who will be the owner of the new ACL object identity
     */
    private void persistNewAclObjectIdentityForEncounterScheduledIfNotExist(
        EncounterScheduled encounterScheduled,
        User currentUser
    ) {
        AclClass aclClass = aclClassDao.getElementByClass(EncounterScheduled.class.getName());
        AclObjectIdentity existingObjectIdentity =
            aclObjectIdentityDao.getElementByClassAndObjectId(aclClass, encounterScheduled.getId());

        if (existingObjectIdentity == null) {

            AclObjectIdentity encounterScheduledObjectIdentity = new AclObjectIdentity(
            encounterScheduled.getId(),
            Boolean.TRUE,
            aclClass,
            currentUser,
            null);
            aclObjectIdentityDao.persist(encounterScheduledObjectIdentity);
        }
    }
}