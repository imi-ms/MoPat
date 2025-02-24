package de.imi.mopat.controller;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ReviewService;
import de.imi.mopat.helper.controller.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    public String showCreateReviewForm(@RequestParam(value = "language", required = false) final Locale language,
                                       Model model) {

        Locale currentLanguage = Optional.ofNullable(language).orElse(LocaleContextHolder.getLocale());
        model.addAttribute("language", currentLanguage);

        model.addAttribute("currentUserId", authService.getAuthenticatedUserId());
        model.addAttribute("reviewers", reviewService.getAllReviewers());
        model.addAttribute("questionnaires", reviewService.getUnapprovedQuestionnaires());

        if (!model.containsAttribute("createReviewForm")) {
            model.addAttribute("createReviewForm", new CreateReviewForm(null, null, null, null, null));
        }
        return "review/create";
    }

    @PostMapping("review/create")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String createReview(@Valid @ModelAttribute("createReviewForm") CreateReviewForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               final HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("createReviewForm", form);
            return showCreateReviewForm(null, model);
        }

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult = reviewService.addReview(form, locale, request);

        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/list";
    }

    @RequestMapping(value = "/review/remove")
    @PreAuthorize("@reviewService.canModifyReview(#id)")
    public String removeReview(@RequestParam(value = "id", required = true) final Long id,
                               final Model model,
                               RedirectAttributes redirectAttributes) {

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult = reviewService.deleteReviewById(id, locale);

        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/list";
    }

    /**
     * Generates a localized email preview for a review request.
     * The preview includes the subject and content of the email, localized to the selected language.
     *
     * @param language        The language code (e.g., "en_GB" or "de_DE") in which the email should be generated.
     * @param action          The type of action triggering the email (e.g., "invitation", "approve", "reject").
     * @param questionnaireName The name of the questionnaire related to the review request.
     * @param description     An optional description of the review request.
     * @param personalMessage An optional personal message to be included in the email.
     * @return A `ResponseEntity` containing a map with `subject` and `content` keys for the email preview.
     */
    @GetMapping("/review/preview-email")
    public ResponseEntity<Map<String, String>> getEmailPreview(
            @RequestParam String language,
            @RequestParam String action,
            @RequestParam String questionnaireName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String personalMessage) {

        Locale userLocale = LocaleHelper.getLocaleFromString(language);
        Map<String, String> emailTexts = reviewService.getLocalizedEmailPreviewTexts(action, userLocale, questionnaireName, description, personalMessage);
        return ResponseEntity.ok(emailTexts);
    }

}