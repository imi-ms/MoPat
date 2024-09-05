package de.imi.mopat.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.GitRepositoryMetadataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * AdminController controller.
 */
@Controller
public class AdminController {

    /**
     * Configuration Data access object.
     */
    @Autowired
    private ConfigurationDao configurationDao;


    @Autowired
    private GitRepositoryMetadataHandler gitRepositoryMetadataHandler;

    /**
     * @param model The model, which holds the information for the view.
     * @return The <i>/admin/index</i> website.
     */
    // The annotation is used to map URLs onto this handler method
    @GetMapping(value = "/admin/index")
    @PreAuthorize("hasRole('ROLE_ENCOUNTERMANAGER')")
    public String showAdmin(final Model model) {
        //Get the default language of the application from the configuration
        model.addAttribute("defaultLanguage", configurationDao.getDefaultLocale());
        model.addAttribute("gitRepositoryMetadata", gitRepositoryMetadataHandler.getGitRepositoryMetadata());
        return "admin/index";
    }
}
