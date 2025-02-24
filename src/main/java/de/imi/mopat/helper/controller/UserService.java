package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.model.UserDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private AclEntryDao aclEntryDao;

    @Autowired
    private ClinicDao clinicDao;

    @Autowired
    private RoleHierarchyImpl roleHierarchy;

    @Autowired
    private UserDTOMapper userDTOMapper;

    /**
     * Retrieves all users as UserDTO objects.
     *
     * @return List of UserDTO objects representing all users.
     */
    public List<UserDTO> getAllUser(){
        return userDao.getAllElements().stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all users that are available to be assigned to a specified clinic.
     *
     * @param clinicID The ID of the clinic.
     * @return List of UserDTOs representing users that are not assigned to the clinic.
     */
    public List<UserDTO> getAvailableUserDTOs(final Long clinicID) {
        if (clinicID == null) {
            return getAllUser(); // Return all users if no clinic ID is specified
        }

        Set<Long> assignedUserIds = aclEntryDao.getAllElements().stream()
                .filter(aclEntry -> aclEntry.getAclObjectIdentity().getObjectIdIdentity().equals(clinicID))
                .map(aclEntry -> aclEntry.getUser().getId())
                .collect(Collectors.toSet());

        return userDao.getAllElements().stream()
                .filter(user -> !assignedUserIds.contains(user.getId()))
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all users that are assigned to a specified clinic.
     *
     * @param clinicId The ID of the clinic.
     * @return List of UserDTOs representing users that are assigned to the clinic.
     */
    public List<UserDTO> getAssignedUserDTOs(final Long clinicId) {
        Set<Long> availableUserIds = getAvailableUserDTOs(clinicId).stream()
                .map(UserDTO::getId)
                .collect(Collectors.toSet());

        return userDao.getAllElements().stream()
                .map(userDTOMapper)
                .filter(userDTO -> !availableUserIds.contains(userDTO.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user.
     * @return UserDTO object representing the user.
     */
    public UserDTO getUserDTOById(Long userId) {
        User user = userDao.getElementById(userId);
        return userDTOMapper.apply(user);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return UserDTO object representing the user.
     */
    public UserDTO getUserByUsername(String username) {
        User user = userDao.loadUserByUsername(username);
        return userDTOMapper.apply(user);
    }

    public void replaceUserRoles(User user, UserRole newRole) {
        user.replaceRolesWith(newRole);
        userDao.merge(user);
    }

    /**
     * Updates the clinic rights for a user. Assigns or revokes permissions based on the provided clinic IDs.
     *
     * @param user The user to update.
     * @param clinicIDs The list of clinic IDs to assign rights to.
     */
    public void updateUserClinicRights(User user, List<Long> clinicIDs) {
        // Previously assigned clinics
        Collection<Clinic> previouslyAssignedClinics = clinicDao.getElementsById(
                aclEntryDao.getObjectIdsForClassUserAndRight(Clinic.class, user, PermissionType.READ));

        // Newly assigned clinics based on input
        Collection<Clinic> newlyAssignedClinics = new ArrayList<>();
        if (clinicIDs != null && !clinicIDs.isEmpty()) {
            newlyAssignedClinics = clinicDao.getElementsById(clinicIDs);
        }

        // Determine which clinics to add and remove
        Collection<Clinic> clinicsToRemove = new ArrayList<>(previouslyAssignedClinics);
        clinicsToRemove.removeAll(newlyAssignedClinics);

        Collection<Clinic> clinicsToAdd = new ArrayList<>(newlyAssignedClinics);
        clinicsToAdd.removeAll(previouslyAssignedClinics);

        // Grant rights for newly assigned clinics
        for (Clinic clinic : clinicsToAdd) {
            AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user, PermissionType.READ);
            if (clinicACLEntry == null) {
                clinicDao.grantRight(clinic, user, PermissionType.READ, Boolean.TRUE);
            }
        }

        // Revoke rights for clinics no longer assigned
        for (Clinic clinic : clinicsToRemove) {
            AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user, PermissionType.READ);
            if (clinicACLEntry != null) {
                clinicDao.revokeRight(clinic, user, PermissionType.READ, Boolean.TRUE);
            }
        }
    }

    /**
     * Retrieves the highest role assigned to a user based on the role hierarchy.
     *
     * @param user The user to evaluate.
     * @return The highest UserRole assigned to the user, or null if none are found.
     */
    public UserRole getHighestRole(User user) {
        if (user == null) {
            return null;
        }
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        GrantedAuthority highestAuthority = authorities.stream()
                .max(Comparator.comparingInt(authority ->
                        roleHierarchy.getReachableGrantedAuthorities(List.of(authority)).size()))
                .orElse(null);

        return highestAuthority != null ? UserRole.fromString(highestAuthority.getAuthority()) : null;
    }

    public List<UserDTO> getUsersByRole(UserRole userRole) {
        return userDao.getAllElements().stream()
                .filter(user -> user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(userRole.getTextValue())))
                .map(userDTOMapper)
                .distinct()
                .toList();
    }
}
