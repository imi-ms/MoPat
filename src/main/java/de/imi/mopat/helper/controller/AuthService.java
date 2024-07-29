package de.imi.mopat.helper.controller;

import de.imi.mopat.model.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import de.imi.mopat.model.user.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class AuthService {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final RoleHierarchyImpl roleHierarchy;

    @Autowired
    public AuthService(RoleHierarchyImpl roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    private Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            LOGGER.warn("SecurityContext is null");
            return null;
        }
        return context.getAuthentication();
    }

    public User getAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            LOGGER.warn("No authenticated user found or principal is not an instance of User");
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    public Long getAuthenticatedUserId() {
        User user = getAuthenticatedUser();
        return user != null ? user.getId() : null;
    }

    public boolean hasExactRole(UserRole userRole) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(userRole.getTextValue()));
    }

    public boolean hasRoleOrAbove(UserRole userRole) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(userRole.getTextValue())) ||
                roleHierarchy.getReachableGrantedAuthorities(authorities).stream()
                        .anyMatch(a -> a.getAuthority().equals(userRole.getTextValue()));
    }

    public UserRole getHighestRole() {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return null;
        }
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        GrantedAuthority highestAuthority = authorities.stream()
                .max(Comparator.comparingInt(authority -> roleHierarchy.getReachableGrantedAuthorities(List.of(authority)).size()))
                .orElse(null);

        return highestAuthority != null ? UserRole.fromString(highestAuthority.getAuthority()) : null;
    }
}

