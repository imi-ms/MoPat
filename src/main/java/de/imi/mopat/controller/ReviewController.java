package de.imi.mopat.controller;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ReviewService;
import de.imi.mopat.helper.controller.ValidationResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class ReviewController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/review/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listReview(final Model model) {
        model.addAttribute("allReviews", reviewService.getAllReviews());
        model.addAttribute("assignedReviews", reviewService.getAssignedReviews());
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "review/list";
    }

    @GetMapping("review/create")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showCreateReviewForm(Model model) {

        model.addAttribute("currentUserId", authService.getAuthenticatedUserId());
        model.addAttribute("reviewers", reviewService.getAllReviewers());
        model.addAttribute("questionnaires", reviewService.getUnapprovedQuestionnaires());

        if (!model.containsAttribute("createReviewForm")) {
            model.addAttribute("createReviewForm", new CreateReviewForm(null, null, null));
        }
        return "review/create";
    }

    @PostMapping("review/create")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String createReview(@Valid @ModelAttribute("createReviewForm") CreateReviewForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("createReviewForm", form);
            return showCreateReviewForm(model);
        }

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult = reviewService.addReview(form, locale);

        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/list";
    }
}