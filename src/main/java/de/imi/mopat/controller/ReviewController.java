package de.imi.mopat.controller;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.controller.forms.ReviewDecisionForm;
import de.imi.mopat.helper.controller.AuthService;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ReviewService;
import de.imi.mopat.helper.controller.UserService;
import de.imi.mopat.helper.controller.ValidationResult;
import de.imi.mopat.model.dto.ReviewDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    AuthService authService;

    @GetMapping("/pending/list")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listReview(final Model model) {
        model.addAttribute("allReviews", reviewService.getAllPendingReviews());
        model.addAttribute("assignedReviews", reviewService.getAssignedPendingReviews());
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "review/pending-list";
    }

    @GetMapping("/completed/list")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String listArchivedReviews(final Model model) {
        model.addAttribute("allReviews", reviewService.getAllCompletedReviews());
        model.addAttribute("assignedReviews", reviewService.getAssignedCompletedReviews());
        model.addAttribute("availableLocales", LocaleHelper.getAvailableLocales());
        return "review/completed-list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String showCreateReviewForm(@RequestParam(value = "language", required = false) final Locale language,
                                       @RequestParam(value = "questionnaireId", required = false) final Long questionnaireId,
                                       Model model) {

        Locale currentLanguage = Optional.ofNullable(language).orElse(LocaleContextHolder.getLocale());
        model.addAttribute("language", currentLanguage);

        model.addAttribute("currentUserId", authService.getAuthenticatedUserId());
        model.addAttribute("reviewers", userService.getAllReviewers());
        model.addAttribute("questionnaires", reviewService.getUnapprovedQuestionnaires());

        if (!model.containsAttribute("createReviewForm")) {
            model.addAttribute("createReviewForm", new CreateReviewForm(questionnaireId, null, null, null, null));
        }
        return "review/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String createReview(@Valid @ModelAttribute("createReviewForm") CreateReviewForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               final HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("createReviewForm", form);
            return showCreateReviewForm(null, null, model);
        }

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult = reviewService.addReview(form, locale, request);

        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/pending/list";
    }

    @GetMapping("/pending/details")
    @PreAuthorize("@reviewService.canModifyReview(#id)")
    public String getReviewById(@RequestParam(value = "language", required = false) final Locale language,
                                @RequestParam Long id, Model model) {

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validatedReview = reviewService.validateReview(id, locale);

        if (validatedReview.hasErrors()) {
            model.addAttribute("messageFail", validatedReview.getLocalizedMessage());
            return "review/list";
        }

        ReviewDTO reviewDTO = reviewService.getReviewById(id);
        Locale currentLanguage = Optional.ofNullable(language).orElse(LocaleContextHolder.getLocale());

        model.addAttribute("language", currentLanguage);
        model.addAttribute("isReviewer", reviewService.isUserReviewer());
        model.addAttribute("reviewers", userService.getAllReviewers());
        model.addAttribute("review", reviewDTO);
        model.addAttribute("currentUserId", authService.getAuthenticatedUserId());

        if (!model.containsAttribute("reviewDecisionForm")) {
            model.addAttribute("reviewDecisionForm", new ReviewDecisionForm(id, null, null, null, false, null, null));
        }
        return "review/pending-details";
    }

    @GetMapping("/completed/details")
    @PreAuthorize("@reviewService.canModifyReview(#id)")
    public String getCompletedReviewById(@RequestParam(value = "language", required = false) final Locale language,
                                @RequestParam Long id, Model model) {

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validatedReview = reviewService.validateReview(id, locale);

        if (validatedReview.hasErrors()) {
            model.addAttribute("messageFail", validatedReview.getLocalizedMessage());
            return "review/list";
        }

        ReviewDTO reviewDTO = reviewService.getReviewById(id);
        Locale currentLanguage = Optional.ofNullable(language).orElse(LocaleContextHolder.getLocale());

        model.addAttribute("language", currentLanguage);
        model.addAttribute("review", reviewDTO);
        model.addAttribute("currentUserId", authService.getAuthenticatedUserId());

        return "review/completed-details";
    }

    @PostMapping("/details")
    @PreAuthorize("@reviewService.canModifyReview(#form.reviewId)")
    public String handleReviewAction(@Valid @ModelAttribute("reviewDecisionForm") ReviewDecisionForm form,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reviewDecisionForm", form);
            return getReviewById(null, form.reviewId(), model);
        }

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult;
        switch (form.action()) {
            case "approve":
                validationResult = reviewService.approveReview(form, locale, request);
                break;
            case "reject":
                validationResult = reviewService.rejectReview(form, locale, request);
                break;
            case "review":
                validationResult = reviewService.resubmitReview(form, locale, request);
                break;
            case "assignReviewer":
                validationResult = reviewService.assignReviewer(form, locale, request);
                break;
            default:
                redirectAttributes.addFlashAttribute("messageFail", "Invalid action: "+form.action());
                return "redirect:/review/pending/list";
        }
        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/pending/list";
    }

    @RequestMapping(value = "/remove")
    @PreAuthorize("@reviewService.canModifyReview(#id)")
    public String removeReview(@RequestParam(value = "id", required = true) final Long id,
                               final Model model,
                               RedirectAttributes redirectAttributes) {

        Locale locale = LocaleContextHolder.getLocale();
        ValidationResult validationResult = reviewService.deleteReviewById(id, locale);
        redirectAttributes.addFlashAttribute(
                validationResult.hasNoErrors() ? "messageSuccess" : "messageFail", validationResult.getLocalizedMessage()
        );
        return "redirect:/review/pending/list";
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
    @GetMapping("/preview-email")
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