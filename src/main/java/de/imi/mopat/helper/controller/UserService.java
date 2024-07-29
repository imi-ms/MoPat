package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.ClinicDTO;
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

@Service
public class UserService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Question.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private AclEntryDao aclEntryDao;

    @Autowired
    private ClinicDao clinicDao;

    @Autowired
    private RoleHierarchyImpl roleHierarchy;


    public List<UserDTO> getAllUser(){
        return userDao.getAllElements().stream()
                .map(User::toUserDTO)
                .toList();
    }

    /**
     * @param clinicID The Id of the {@link Clinic} object.
     * @return Returns all {@link User Users} that are available to be assigned to the
     * {@link Clinic} object.
     */
    public List<UserDTO> getAvailableUserDTOs(final Long clinicID) {

        List<UserDTO> availableUserDTOs = new ArrayList<>();

        // Add all users that are available
        boolean assigned = false;
        for (User user : userDao.getAllElements()) {
            if (clinicID != null) {
                for (AclEntry aclEntry : aclEntryDao.getAllElements()) {
                    if (aclEntry.getAclObjectIdentity().getObjectIdIdentity().equals(clinicID)
                            && aclEntry.getUser().getId().equals(user.getId())) {
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    availableUserDTOs.add(user.toUserDTO());
                }
                assigned = false;
            } else {
                availableUserDTOs.add(user.toUserDTO());
            }
        }

        return availableUserDTOs;
    }

    public List<UserDTO> getAssignedUserDTOs(final Long clinicId) {
        List<UserDTO> availableUserDTOs = getAvailableUserDTOs(clinicId);
        List<UserDTO> assignedUserDTOs = new ArrayList<>();
        for (User user : userDao.getAllElements()) {
            UserDTO assignedUserDTO = user.toUserDTO();
            assignedUserDTOs.add(assignedUserDTO);
            for (UserDTO userDTO : availableUserDTOs) {
                if (assignedUserDTO.getId().equals(userDTO.getId())) {
                    assignedUserDTOs.remove(assignedUserDTO);
                }
            }
        }
        return assignedUserDTOs;
    }

    private List<UserDTO> getUsersInSameClinicsAsUser(List<ClinicDTO> clinics, UserDTO user) {
        return clinics.stream()
                .filter(clinicDTO -> {
                    List<UserDTO> assignedUserDTOs = getAssignedUserDTOs(clinicDTO.getId());
                    return assignedUserDTOs != null && assignedUserDTOs.stream().anyMatch(u -> u.equals(user));
                })
                .flatMap(clinic -> clinic.getAssignedUserDTOs().stream())
                .distinct() // Entfernt doppelte UserDTOs
                .toList();
    }

    public List<UserDTO> getClinicModeratorsAndAdmins(List<ClinicDTO> clinics, UserDTO user) {
        // Schritt 1: Sammle alle Benutzer, die den gleichen Kliniken wie der gegebene Benutzer zugewiesen sind
        List<UserDTO> usersInSameClinics = getUsersInSameClinicsAsUser(clinics, user);

        // Schritt 2: Filtere Moderatoren und Administratoren
        return usersInSameClinics.stream()
                .map(userDTO -> userDao.loadUserByUsername(userDTO.getUsername()))
                .filter(user1 -> user1.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_MODERATOR") ||
                                authority.getAuthority().equals("ROLE_ADMIN")))
                .map(User::toUserDTO)
                .distinct() // Entfernt doppelte UserDTOs
                .toList();
    }

    public UserDTO getUserByUsername(String username) {
        return userDao.loadUserByUsername(username).toUserDTO();
    }

    public void updateUserRole(User user, UserRole role) {
        user.addRole(role);
        userDao.merge(user);
    }

    public void updateUserClinicRights(User user, List<Long> clinicIDs) {
        Collection<Clinic> assignedClinics = clinicDao.getElementsById(
                aclEntryDao.getObjectIdsForClassUserAndRight(Clinic.class, user, PermissionType.READ));
        Collection<Clinic> currentClinics = new ArrayList<>();
        if (clinicIDs != null && !clinicIDs.isEmpty()) {
            currentClinics = clinicDao.getElementsById(clinicIDs);
        }
        assignedClinics.removeAll(currentClinics);
        currentClinics.removeAll(assignedClinics);
        for (Clinic clinic : currentClinics) {
            AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user, PermissionType.READ);
            if (clinicACLEntry == null) {
                clinicDao.grantRight(clinic, user, PermissionType.READ, Boolean.TRUE);
            }
        }
        for (Clinic clinic : assignedClinics) {
            AclEntry clinicACLEntry = aclEntryDao.getEntryForObjectUserAndRight(clinic, user, PermissionType.READ);
            if (clinicACLEntry != null) {
                clinicDao.revokeRight(clinic, user, PermissionType.READ, Boolean.TRUE);
            }
        }
    }

    public UserRole getHighestRole(User user) {
        if (user == null) {
            return null;
        }
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        GrantedAuthority highestAuthority = authorities.stream()
                .max(Comparator.comparingInt(authority -> roleHierarchy.getReachableGrantedAuthorities(List.of(authority)).size()))
                .orElse(null);

        return highestAuthority != null ? UserRole.fromString(highestAuthority.getAuthority()) : null;
    }
}
