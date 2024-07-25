package de.imi.mopat.helper.controller;

import de.imi.mopat.model.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import de.imi.mopat.model.user.User;

import java.util.Collection;

@Service
public class AuthService {

    private final RoleHierarchyImpl roleHierarchy;

    @Autowired
    public AuthService(RoleHierarchyImpl roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    public boolean hasExactRole(UserRole userRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(userRole.getTextValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoleOrAbove(UserRole userRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(userRole.getTextValue()) ||
                    roleHierarchy.getReachableGrantedAuthorities(authorities).stream().anyMatch(a -> a.getAuthority().equals(userRole.getTextValue()))) {
                return true;
            }
        }
        return false;
    }
}

