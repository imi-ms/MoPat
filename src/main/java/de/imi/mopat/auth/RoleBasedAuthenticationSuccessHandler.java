package de.imi.mopat.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private PinAuthorizationService pinAuthorizationService;

    private Map<String, String> roleUrlMap;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
        final HttpServletResponse response, final Authentication authentication)
        throws IOException {

        pinAuthorizationService.removePinAuthForCurrentUser();

        DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) request.getSession()
            .getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        String requestURL = "";
        //If a page was requested before login, get the appropriate request URL
        if (defaultSavedRequest != null) {
            requestURL = defaultSavedRequest.getRequestURL();
            if (defaultSavedRequest.getQueryString() != null
                && !defaultSavedRequest.getQueryString().isEmpty()) {
                requestURL = requestURL + "?" + defaultSavedRequest.getQueryString();
            }
        }
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String role = userDetails.getAuthorities().isEmpty() ? null
                : userDetails.getAuthorities().toArray()[0].toString();
            // If a page was requested before login
            if (!requestURL.isEmpty() && !requestURL.contains("/mobile/survey/questionnaire")) {
                //Redirect to the request URL
                response.sendRedirect(requestURL);
            } else {
                //Otherwise redirect to the role based welcome page
                response.sendRedirect(request.getContextPath() + roleUrlMap.get(role));
            }
        }

    }

    public void setRoleUrlMap(final Map<String, String> roleUrlMap) {
        this.roleUrlMap = roleUrlMap;
    }
}
