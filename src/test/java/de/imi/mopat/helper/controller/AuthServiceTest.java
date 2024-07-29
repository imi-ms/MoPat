package de.imi.mopat.helper.controller;

import de.imi.mopat.model.user.Authority;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.model.user.UserTest;
import de.imi.mopat.utils.Helper;
import de.imi.mopat.utils.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class AuthServiceTest {

    @Mock
    private RoleHierarchyImpl roleHierarchy;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetAuthenticatedUser_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        User result = authService.getAuthenticatedUser();

        // Assert
        assertNull("Authenticated user should be null when there is no authentication", result);
    }

    @Test
    public void testGetAuthenticatedUserId_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        Long result = authService.getAuthenticatedUserId();

        // Assert
        assertNull("Authenticated user ID should be null when there is no authentication", result);
    }

    @Test
    public void testHasExactRole_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = authService.hasExactRole(UserRole.ROLE_ADMIN);

        // Assert
        assertFalse("hasExactRole should return false when there is no authentication", result);
    }

    @Test
    public void testHasRoleOrAbove_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = authService.hasRoleOrAbove(UserRole.ROLE_ADMIN);

        // Assert
        assertFalse("hasRoleOrAbove should return false when there is no authentication", result);
    }

    @Test
    public void testGetAuthenticatedUser() {
        // Arrange
        User user = spy(UserTest.getNewValidUser());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        User authenticatedUser = authService.getAuthenticatedUser();

        // Assert
        assertEquals("Authenticated user should match the expected user", user, authenticatedUser);
    }

    @Test
    public void testGetAuthenticatedUserId() {
        // Arrange
        User user = spy(UserTest.getNewValidUser());
        Long userId = Helper.generatePositiveNonZeroLong();
        doReturn(userId).when(user).getId();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        Long authenticatedUserId = authService.getAuthenticatedUserId();

        // Assert
        assertEquals("Authenticated user ID should match the expected ID", userId, authenticatedUserId);
    }

    @Test
    public void testHasExactRole() {
        // Arrange
        UserRole userRole = UserRole.ROLE_ADMIN;
        GrantedAuthority authority = userRole::getTextValue;
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        boolean result = authService.hasExactRole(userRole);

        // Assert
        assertTrue("hasExactRole should return true for the matching role", result);
    }

    @Test
    public void testHasExactRole_NotFound() {
        // Arrange
        UserRole userRole = UserRole.ROLE_ADMIN;
        Collection<GrantedAuthority> authorities = Collections.emptyList();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        boolean result = authService.hasExactRole(userRole);

        // Assert
        assertFalse("hasExactRole should return false when the role is not found", result);
    }

    @Test
    public void testHasRoleOrAbove() {
        // Arrange
        UserRole userRole = UserRole.ROLE_USER;
        GrantedAuthority authority = UserRole.ROLE_ADMIN::getTextValue;
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);
        Collection<GrantedAuthority> reachableAuthorities = Collections.singletonList(userRole::getTextValue);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertTrue("hasRoleOrAbove should return true for roles above or equal to the given role", result);
    }

    @Test
    public void testHasRoleOrAbove_LowerRole() {
        // Arrange
        UserRole userRole = UserRole.ROLE_MODERATOR; // The role we are checking against
        GrantedAuthority authority = UserRole.ROLE_USER::getTextValue; // The user has only ROLE_USER
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);
        Collection<GrantedAuthority> reachableAuthorities = Collections.singletonList(authority);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertFalse("hasRoleOrAbove should return false for roles below the given role", result);
    }

    @Test
    public void testHasRoleOrAbove_NotFound() {
        // Arrange
        UserRole userRole = UserRole.ROLE_ADMIN;
        Collection<GrantedAuthority> authorities = Collections.emptyList();
        Collection<GrantedAuthority> reachableAuthorities = Collections.emptyList();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertFalse("hasRoleOrAbove should return false when the role or above roles are not found", result);
    }

    @Test
    public void testHasRoleOrAbove_MultipleRoles() {
        // Arrange
        UserRole userRole = UserRole.ROLE_MODERATOR;
        GrantedAuthority userAuthority = UserRole.ROLE_USER::getTextValue;
        GrantedAuthority adminAuthority = UserRole.ROLE_ADMIN::getTextValue;
        Collection<GrantedAuthority> authorities = List.of(userAuthority, adminAuthority);
        Collection<GrantedAuthority> reachableAuthorities = List.of(userAuthority, userRole::getTextValue);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertTrue("hasRoleOrAbove should return true if the user has a role higher than the given role", result);
    }

    @Test
    public void testHasRoleOrAbove_MultipleLowerRoles() {
        // Arrange
        UserRole userRole = UserRole.ROLE_MODERATOR;
        GrantedAuthority userAuthority = UserRole.ROLE_USER::getTextValue;
        GrantedAuthority editorAuthority = UserRole.ROLE_EDITOR::getTextValue;
        Collection<GrantedAuthority> authorities = List.of(userAuthority, editorAuthority);
        Collection<GrantedAuthority> reachableAuthorities = List.of(userAuthority, editorAuthority);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertFalse("hasRoleOrAbove should return false when the role or above roles are not found", result);
    }

    @Test
    public void testHasRoleOrAbove_ExactRole() {
        // Arrange
        UserRole userRole = UserRole.ROLE_MODERATOR;
        GrantedAuthority authority = UserRole.ROLE_MODERATOR::getTextValue;
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);
        Collection<GrantedAuthority> reachableAuthorities = Collections.singletonList(authority);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenReturn(reachableAuthorities);

        // Act
        boolean result = authService.hasRoleOrAbove(userRole);

        // Assert
        assertTrue("hasRoleOrAbove should return true for the exact role", result);
    }

//    Neue Tests

    @Test
    public void testGetHighestRole_SingleRole() {
        // Arrange
        GrantedAuthority authority = UserRole.ROLE_USER::getTextValue;
        Collection<GrantedAuthority> authorities = List.of(authority);
        User user = mock(User.class);
        when(user.getAuthority()).thenReturn(Set.of(new Authority(user, UserRole.ROLE_USER)));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        UserRole highestRole = authService.getHighestRole();

        // Assert
        assertEquals("The highest role should be ROLE_USER", UserRole.ROLE_USER, highestRole);
    }

    @Test
    public void testGetHighestRole_MultipleRoles() {
        // Arrange
        GrantedAuthority userAuthority = UserRole.ROLE_USER::getTextValue;
        GrantedAuthority moderatorAuthority = UserRole.ROLE_MODERATOR::getTextValue;
        Collection<GrantedAuthority> authorities = List.of(userAuthority, moderatorAuthority);
        User user = mock(User.class);
        when(user.getAuthority()).thenReturn(Set.of(new Authority(user, UserRole.ROLE_USER), new Authority(user, UserRole.ROLE_MODERATOR)));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(roleHierarchy.getReachableGrantedAuthorities(anyList())).thenAnswer(invocation -> {
            Collection<GrantedAuthority> auths = invocation.getArgument(0);
            return Set.copyOf(auths);
        });

        // Act
        UserRole highestRole = authService.getHighestRole();

        // Assert
        assertEquals("The highest role should be ROLE_MODERATOR", UserRole.ROLE_MODERATOR, highestRole);
    }

    @Test
    public void testGetHighestRole_NoRoles() {
        // Arrange
        User user = mock(User.class);
        when(user.getAuthority()).thenReturn(Collections.emptySet());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        // Act
        UserRole highestRole = authService.getHighestRole();

        // Assert
        assertNull("There should be no highest role for a user with no roles", highestRole);
    }

    @Test
    public void testGetHighestRole_RoleHierarchy() {
        // Arrange
        GrantedAuthority userAuthority = UserRole.ROLE_USER::getTextValue;
        GrantedAuthority adminAuthority = UserRole.ROLE_ADMIN::getTextValue;
        GrantedAuthority editorAuthority = UserRole.ROLE_EDITOR::getTextValue;
        Collection<GrantedAuthority> authorities = List.of(userAuthority, adminAuthority, editorAuthority);
        User user = mock(User.class);
        when(user.getAuthorities()).thenReturn((Collection) authorities);
        when(user.getAuthority()).thenReturn(Set.of(new Authority(user, UserRole.ROLE_USER), new Authority(user, UserRole.ROLE_ADMIN), new Authority(user, UserRole.ROLE_EDITOR)));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(roleHierarchy.getReachableGrantedAuthorities(anyList())).thenAnswer(invocation -> {
            Collection<GrantedAuthority> auths = invocation.getArgument(0);
            if (auths.contains(editorAuthority)) {
                return Set.of(editorAuthority, adminAuthority, userAuthority);
            }
            return auths;
        });

        // Act
        UserRole highestRole = authService.getHighestRole();

        System.out.println(highestRole.getTextValue());

        // Assert
        assertEquals("The highest role should be ROLE_EDITOR", UserRole.ROLE_EDITOR, highestRole);
    }

}