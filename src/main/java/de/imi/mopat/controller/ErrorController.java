package de.imi.mopat.controller;

import de.imi.mopat.dao.ConfigurationDao;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @Autowired
    private ConfigurationDao configurationDao;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ErrorController.class);

    /**
     * Controls the HTTP requests for the URL /error/maintenance.
     *
     * @return The website with information about the maintenance downtime.
     */
    // The annotation is used to map URLs onto this handler method
    @GetMapping(value = "/error/maintenance")
    public String maintenancePage() {
        return "error/maintenance";
    }

    /**
     * Controls the HTTP requests for the URL /error/accessdenied.
     *
     * @return The website with information about authorization failure.
     */
    // The annotation is used to map URLs onto this handler method
    @GetMapping(value = "/error/accessdenied")
    public String accessDeniedPage() {
        return "error/accessdenied";
    }

    /**
     * Controls the HTTP requests for the URL /error/pagenotfound. Shows the not found page if the
     * page does not exist within the system.
     *
     * @return The website, which was requested.
     */
    @RequestMapping(value = "/error/pagenotfound")
    public String pageNotFoundPage() {
        return "error/pagenotfound";
    }

    /**
     * Controls the HTTP requests for the URL /error/internalservererror. Shows the internal server
     * error page if an internal server error occurs.
     *
     * @param request the HTTP request to control.
     * @return The website, which was requested.
     */
    @RequestMapping(value = "/error/internalservererror")
    public String internalServerErrorPage(final HttpServletRequest request) {
        Marker fatal = MarkerFactory.getMarker("FATAL");
        Throwable exception = (Throwable) request.getAttribute(
            "jakarta.servlet" + ".error.exception");
        if (exception instanceof HttpSessionRequiredException
            || exception instanceof IllegalStateException) {
            LOGGER.error("The session has expired", exception);
            return "error/sessionTimeout";
        } else {
            LOGGER.error(fatal, "An internal server error on page " + request.getAttribute(
                    "jakarta.servlet.error.request_uri") + " occured. Please check the server log",
                (Throwable) request.getAttribute("jakarta.servlet" + ".error" + ".exception"));
            return "error/internalservererror";
        }
    }


    /**
     * Controls the HTTP requests for the URL /error/clinicNotFound.Error page that shows,
     * survey is started using a user that does not have any clinic assigned
     *
     * @return The website, which was requested.
     */
    @RequestMapping(value = "/error/clinicNotFound")
    public String clinicNotFoundPage(final Model model) {
        model.addAttribute("supportEmail", configurationDao.getSupportEMail());
        return "error/clinicNotFound";
    }
}
