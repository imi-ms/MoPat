package de.imi.mopat.controller;

import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ReviewService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ReviewController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @RequestMapping(value = "/review/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listReview(final Model model) {
        model.addAttribute("allReviews", reviewService.getAllReviews());
        model.addAttribute("assignedReviews", reviewService.getAssignedReviews());
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "review/list";
    }
}