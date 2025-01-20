from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.SeleniumUtils import SeleniumUtils


class NavigationBarSelectors:
    class UserMenu:
        BUTTON = (By.ID, "userDropdownLink")
        MAIL_TO_ALL_LINK = (By.ID, "mailToAllLink")

    class QuestionnaireMenu:
        BUTTON = (By.ID, "questionnaireDropdownLink")
        MANAGE_LINK = (By.ID, "manageQuestionnaireLink")
        IMPORT_LINK = (By.ID, "importQuestionnaireLink")

    class BundleMenu:
        LINK = (By.ID, "bundleLink")

    class ClinicMenu:
        LINK = (By.ID, "clinicLink")

class QuestionTableSelectors:
    EDIT_QUESTION_LINK = lambda question_id: (By.XPATH, f'//a[@href="fill?id={question_id}"]')
    EDIT_QUESTIONS_CONDITIONS_LINK = lambda question_id: (By.XPATH, f'//a[@href="/condition/listQuestionConditions?questionId={question_id}"]')

class QuestionnaireTableSelectors:
    FILTER_INPUT = (By.CSS_SELECTOR, "#questionnaireTable_filter input[type='search']")
    FIRST_RESULT_LINK = (By.CSS_SELECTOR, "#questionnaireTable tbody tr td a")
    EDIT_QUESTIONS_LINK = lambda questionnaire_id: (By.XPATH, f'//a[@href="/question/list?id={questionnaire_id}"]')
    EDIT_SCORES_LINK = lambda questionnaire_id: (By.XPATH, f'//a[@href="/score/list?id={questionnaire_id}"]')

class NavigationHelper:
    def __init__(self, driver):
        self.driver = driver
        self.utils = SeleniumUtils(self.driver)

    def navigate_to_manage_questionnaires(self):
        try:
            self.utils.click_element(NavigationBarSelectors.QuestionnaireMenu.BUTTON)
            self.utils.click_element(NavigationBarSelectors.QuestionnaireMenu.MANAGE_LINK)
        except Exception as e:
            raise Exception(f"Failed to navigate to 'Manage Questionnaires': {e}")

    def navigate_to_questions_of_questionnaire(self, questionnaire_id, questionnaire_name):
        self.navigate_to_manage_questionnaires()
        self.utils.fill_text_field(QuestionnaireTableSelectors.FILTER_INPUT, questionnaire_name)
        link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            QuestionnaireTableSelectors.EDIT_QUESTIONS_LINK(questionnaire_id)))
        link.click()

    def navigate_to_scores_of_questionnaire(self, questionnaire_id, questionnaire_name):
        self.navigate_to_manage_questionnaires()
        self.utils.fill_text_field(QuestionnaireTableSelectors.FILTER_INPUT, questionnaire_name)
        scores_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            QuestionnaireTableSelectors.EDIT_SCORES_LINK(questionnaire_id)))
        scores_link.click()

    def navigate_to_manage_bundles(self):
        try:
            self.utils.click_element(NavigationBarSelectors.BundleMenu.LINK)
        except Exception as e:
            raise Exception(f"Failed to navigate to 'Manage Bundles': {e}")

    def navigate_to_manage_clinics(self):
        try:
            self.utils.click_element(NavigationBarSelectors.ClinicMenu.LINK)
        except Exception as e:
            raise Exception(f"Failed to navigate to 'Manage Clinics': {e}")

    def navigate_to_email_to_all_users(self):
        try:
            self.utils.click_element(NavigationBarSelectors.UserMenu.BUTTON)
            self.utils.click_element(NavigationBarSelectors.UserMenu.MAIL_TO_ALL_LINK)
        except Exception as e:
            raise Exception(f"Failed to navigate to 'E-Mail to All Users': {e}")

    def search_and_open_questionnaire(self, questionnaire_name):
        try:
            self.navigate_to_manage_questionnaires()
            self.utils.fill_text_field(QuestionnaireTableSelectors.FILTER_INPUT, questionnaire_name)

            # Click the first result
            first_result_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                QuestionnaireTableSelectors.FIRST_RESULT_LINK))
            first_result_link.click()
        except TimeoutException:
            raise Exception(f"Failed to search and open questionnaire '{questionnaire_name}'.")


    def open_question(self, question_id):
        question_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            QuestionTableSelectors.EDIT_QUESTION_LINK(question_id)))
        question_link.click()