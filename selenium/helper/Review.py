from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils


class ReviewSelectors:
    REVIEW_TABS = (By.ID, "reviewTabs")
    ASSIGNED_REVIEWS_TAB = (By.ID, "assigned-reviews-tab")
    ALL_REVIEWS_TAB = (By.ID, "all-reviews-tab")
    COMPLETED_REVIEWS_LINK = (By.ID, "completed-reviews-tab")
    PENDING_REVIEWS_LINK = (By.ID, "archived-reviews-tab")

    PENDING_REVIEW_TABLE = (By.ID, "pendingReviewTable")
    ALL_REVIEW_TABLE = (By.ID, "allReviewTable")
    TABLE_SEARCH = lambda table_id: (By.CSS_SELECTOR, f"#{table_id}_filter input[type='search']")
    TABLE_ROWS = lambda table_id: (By.CSS_SELECTOR, f"#{table_id} tbody tr")
    CREATE_REVIEW_BUTTON = (By.ID, "createReviewAssigned")
    SUCCESS_NOTIFICATION = (By.ID, "successNotification")

    CREATE_REVIEW_FORM = (By.ID, "createReviewForm")
    REVIEWER_SELECT = (By.ID, "reviewerId")
    QUESTIONNAIRE_SELECT = (By.ID, "questionnaireId")
    LANGUAGE_SELECT = (By.ID, "language")
    DESCRIPTION = (By.ID, "description")
    PERSONAL_MESSAGE = (By.ID, "personalMessage")
    PREVIEW_BUTTON = (By.ID, "previewButton")
    PREVIEW_AREA = (By.ID, "previewArea")
    SEND_REQUEST_BUTTON = (By.CSS_SELECTOR, "#createReviewForm button[type='submit']")

    QUESTIONNAIRE_LINK = (By.CSS_SELECTOR, "#questionnaire a")
    EDITOR_NAME = (By.ID, "editor")
    REVIEWER_NAME = (By.ID, "reviewer")
    CONVERSATION = (By.ID, "conversation")
    APPROVAL_OPTIONS = (By.ID, "approvalOptions")
    APPROVE_OPTION_LABEL = (By.CSS_SELECTOR, "label[for='approveOption']")
    REJECT_OPTION_LABEL = (By.CSS_SELECTOR, "label[for='rejectOption']")
    APPROVE_BUTTON = (By.CSS_SELECTOR, "button[name='action'][value='approve']")
    REJECT_BUTTON = (By.CSS_SELECTOR, "button[name='action'][value='reject']")
    RESUBMIT_BUTTON = (By.CSS_SELECTOR, "button[name='action'][value='review']")


class ReviewHelper:
    def __init__(self, driver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver, navigation_helper)

    def _select_table(self, table_id):
        if table_id == "allReviewTable":
            all_reviews_tab = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ReviewSelectors.ALL_REVIEWS_TAB))
            all_reviews_tab.click()

        return WebDriverWait(self.driver, 15).until(EC.visibility_of_element_located(
            (By.ID, table_id)))

    def _find_review_row(self, questionnaire_name, table_id):
        self._select_table(table_id)
        search_box = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            ReviewSelectors.TABLE_SEARCH(table_id)))
        search_box.clear()
        search_box.send_keys(questionnaire_name)

        def find_row(driver):
            return next((row for row in driver.find_elements(*ReviewSelectors.TABLE_ROWS(table_id))
                         if questionnaire_name in row.text), False)

        return WebDriverWait(self.driver, 15).until(find_row)

    def assert_pending_list(self, is_admin=False):
        tabs = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            ReviewSelectors.REVIEW_TABS))
        assert tabs.is_displayed(), "Review tabs are not displayed."

        table = self._select_table("pendingReviewTable")
        assert table.is_displayed(), "Assigned reviews table is not displayed."
        assert len(table.find_elements(By.CSS_SELECTOR, "thead th")) == 6, (
            "The assigned reviews table is missing expected columns.")

        create_button = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            ReviewSelectors.CREATE_REVIEW_BUTTON))
        assert create_button.is_displayed(), "Request Review button is not displayed."
        assert self.driver.find_element(*ReviewSelectors.COMPLETED_REVIEWS_LINK).is_displayed(), (
            "Completed Reviews link is not displayed.")

        if is_admin:
            assert self.driver.find_element(*ReviewSelectors.ALL_REVIEWS_TAB).is_displayed(), (
                "Admin All Reviews tab is not displayed.")

    def assert_completed_list(self, is_admin=False):
        tabs = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            ReviewSelectors.REVIEW_TABS))
        assert tabs.is_displayed(), "Completed review tabs are not displayed."

        table = self._select_table("pendingReviewTable")
        assert table.is_displayed(), "Assigned completed reviews table is not displayed."
        assert len(table.find_elements(By.CSS_SELECTOR, "thead th")) == 6, (
            "The completed reviews table is missing expected columns.")
        assert self.driver.find_element(*ReviewSelectors.PENDING_REVIEWS_LINK).is_displayed(), (
            "Pending Reviews link is not displayed.")

        if is_admin:
            assert self.driver.find_element(*ReviewSelectors.ALL_REVIEWS_TAB).is_displayed(), (
                "Admin All Reviews tab is not displayed.")

    def assert_create_form(self):
        form = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            ReviewSelectors.CREATE_REVIEW_FORM))
        assert form.is_displayed(), "Create review form is not displayed."

        for selector in (ReviewSelectors.REVIEWER_SELECT, ReviewSelectors.QUESTIONNAIRE_SELECT,
                         ReviewSelectors.LANGUAGE_SELECT, ReviewSelectors.DESCRIPTION,
                         ReviewSelectors.PERSONAL_MESSAGE, ReviewSelectors.PREVIEW_BUTTON,
                         ReviewSelectors.SEND_REQUEST_BUTTON):
            assert self.driver.find_element(*selector).is_displayed(), (
                f"Review request form element {selector} is not displayed.")

    def assert_pending_details(self, is_reviewer=False):
        for selector in (ReviewSelectors.QUESTIONNAIRE_LINK, ReviewSelectors.EDITOR_NAME,
                         ReviewSelectors.REVIEWER_NAME, ReviewSelectors.CONVERSATION,
                         ReviewSelectors.LANGUAGE_SELECT, ReviewSelectors.DESCRIPTION):
            assert WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                selector)).is_displayed(), f"Review details element {selector} is not displayed."

        if is_reviewer:
            assert self.driver.find_element(*ReviewSelectors.APPROVAL_OPTIONS).is_displayed(), (
                "Reviewer approval and rejection options are not displayed.")
            assert self.driver.find_element(*ReviewSelectors.APPROVE_BUTTON).is_displayed(), (
                "Approve action is not displayed.")
            assert self.driver.find_element(*ReviewSelectors.REJECT_OPTION_LABEL).is_displayed(), (
                "Reject action is not displayed.")
        else:
            assert self.driver.find_element(*ReviewSelectors.RESUBMIT_BUTTON).is_displayed(), (
                "Editor resubmit action is not displayed.")

    def assert_completed_details(self, expected_messages):
        for selector in (ReviewSelectors.QUESTIONNAIRE_LINK, ReviewSelectors.EDITOR_NAME,
                         ReviewSelectors.REVIEWER_NAME, ReviewSelectors.CONVERSATION):
            assert WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                selector)).is_displayed(), f"Completed review element {selector} is not displayed."

        conversation_text = self.driver.find_element(*ReviewSelectors.CONVERSATION).text
        for message in expected_messages:
            assert message in conversation_text, (
                f"Review conversation does not contain expected message: {message}")

    def assert_review_list_entry(self, questionnaire_name, list_type="assigned"):
        table_id = "pendingReviewTable" if list_type == "assigned" else "allReviewTable"
        row = self._find_review_row(questionnaire_name, table_id)
        assert row.is_displayed(), f"Review for '{questionnaire_name}' is not displayed in the list."

    def open_review(self, questionnaire_name, list_type="assigned", is_completed=False):
        table_id = "pendingReviewTable" if list_type == "assigned" else "allReviewTable"
        row = self._find_review_row(questionnaire_name, table_id)
        details_link = row.find_element(By.CSS_SELECTOR, "a[href*='details?id=']")
        details_link.click()

        details_path = "/review/completed/details" if is_completed else "/review/pending/details"
        WebDriverWait(self.driver, 15).until(EC.url_contains(details_path))

    def request_review(self, questionnaire_name, reviewer_name, description):
        WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            ReviewSelectors.CREATE_REVIEW_BUTTON)).click()
        self.assert_create_form()

        Select(self.driver.find_element(*ReviewSelectors.REVIEWER_SELECT)).select_by_visible_text(
            reviewer_name)
        Select(self.driver.find_element(*ReviewSelectors.QUESTIONNAIRE_SELECT)).select_by_visible_text(
            questionnaire_name)
        Select(self.driver.find_element(*ReviewSelectors.LANGUAGE_SELECT)).select_by_value("en_GB")
        self.utils.fill_text_field(ReviewSelectors.DESCRIPTION, description)
        self.driver.find_element(*ReviewSelectors.SEND_REQUEST_BUTTON).click()

        WebDriverWait(self.driver, 15).until(EC.url_contains("/review/pending/list"))
        WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            ReviewSelectors.SUCCESS_NOTIFICATION))

    def reject_review(self, reason):
        self.driver.find_element(*ReviewSelectors.REJECT_OPTION_LABEL).click()
        self.utils.fill_text_field(ReviewSelectors.DESCRIPTION, reason)
        WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            ReviewSelectors.REJECT_BUTTON)).click()
        WebDriverWait(self.driver, 15).until(EC.url_contains("/review/pending/list"))

    def resubmit_review(self, description):
        self.utils.fill_text_field(ReviewSelectors.DESCRIPTION, description)
        WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            ReviewSelectors.RESUBMIT_BUTTON)).click()
        WebDriverWait(self.driver, 15).until(EC.url_contains("/review/pending/list"))

    def approve_review(self):
        WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            ReviewSelectors.APPROVE_BUTTON)).click()
        WebDriverWait(self.driver, 15).until(EC.url_contains("/review/pending/list"))


class ReviewAssertHelper(ReviewHelper):
    pass
