package de.imi.mopat.auth;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.user.PinAuthorizationDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PinAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinAuthorizationFilter.class);

    @Autowired
    PinAuthorizationDao pinAuthorizationDao;

    @Autowired
    UserDao userDao;

    @Autowired
    ConfigurationDao configurationDao;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip filter for login page and other specific conditions
        return path.startsWith("/error") ||
            path.startsWith("/public") ||
            path.startsWith("/css") ||
            path.startsWith("/js") ||
            path.startsWith("/images") ||
            path.startsWith("/mobile/user/pinlogout") ||
            path.startsWith("/mobile/user/pinlogin") ||
            path.startsWith("/mobile/survey/scores")
            ;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (configurationDao.isGlobalPinAuthEnabled()) {
            // Retrieve the security context from the SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
                // User is authenticated
                //Check if user has Pin Auth activated, then interject this request and redirect to Pin Auth
                User user = userDao.loadUserByUsername(authentication.getName());
                if (user.getUsePin()) {
                    if (pinAuthorizationDao.isPinAuthActivatedForUser(user)) {
                        //Handle Redirect
                        redirectToPinView(response);
                    }
                }
            }
        }
        // User is not authenticated, then do nothing
        // If no redirect interjected, the request can be processed as it normally would
        filterChain.doFilter(request, response);

    }

    private void redirectToPinView(HttpServletResponse response) throws IOException {
        response.sendRedirect("/mobile/user/pinlogin");
    }
}
