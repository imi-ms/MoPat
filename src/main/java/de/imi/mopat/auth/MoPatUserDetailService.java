package de.imi.mopat.auth;

import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
public class MoPatUserDetailService implements UserDetailsService {

    @Qualifier("userDaoImpl")
    @Autowired
    private UserDao moPatUserDao;

    @Override
    @Transactional("MoPat_User")
    public UserDetails loadUserByUsername(final String username)
        throws UsernameNotFoundException, DataAccessException {
        // Get User from UserDao
        User userEntity = moPatUserDao.loadUserByUsername(username);
        // Check if user is empty and throw exception
        if (userEntity == null || userEntity.getPassword() == null || userEntity.getPassword()
            .isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return userEntity;
    }
}
