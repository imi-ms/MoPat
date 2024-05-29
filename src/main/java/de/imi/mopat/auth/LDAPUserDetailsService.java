package de.imi.mopat.auth;

import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class LDAPUserDetailsService implements UserDetailsContextMapper {

    @Autowired
    private UserDao moPatUserDao;

    @Override
    public UserDetails mapUserFromContext(final DirContextOperations dco, final String username,
        final Collection<? extends GrantedAuthority> clctn) {
        if (dco == null) {
            throw new BadCredentialsException("LDAP Authentication " + "deactivated");
        }
        // Get User from UserDao
        User userEntity = moPatUserDao.loadUserByUsername(username);
        // Check if user is empty and throw exception
        if (userEntity == null || !userEntity.isLdap()) {
            throw new InsufficientAuthenticationException(
                "User not found in " + "locale " + "database");
        } else if (!userEntity.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
        return userEntity;
    }

    @Override
    public void mapUserToContext(final UserDetails ud, final DirContextAdapter dca) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
