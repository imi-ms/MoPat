import time
from functools import partial

from selenium import webdriver
from selenium.common import TimeoutException, ElementClickInterceptedException
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.SeleniumUtils import SeleniumUtils


class QuestionnaireSelectors:
    BUTTON_ADD_QUESTIONNAIRE = (By.ID, "addQuestionnaire")
    BUTTON_ADD_QUESTION = (By.ID, "addQuestion")
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_SAVE_AND_EDIT = (By.ID, "saveEditButton")

    INPUT_NAME = (By.ID, "name")
    INPUT_LOCALIZED_DISPLAY_NAME = lambda language_code: (By.ID, f"localizedDisplayName{language_code}")
    INPUT_EDITABLE_DESCRIPTION = (By.CSS_SELECTOR, "div.note-editable")
    INPUT_WELCOME_TEXT_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedWelcomeTextCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_FINAL_TEXT_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedFinalTextCollapsableText_{language_code}"]/div/div[2]/div[2]')

    class QuestionTypes:
        INFO_TEXT = "INFO_TEXT"
        MULTIPLE_CHOICE = "MULTIPLE_CHOICE"
        SLIDER = "SLIDER"
        NUMBER_CHECKBOX = "NUMBER_CHECKBOX"
        NUMBER_CHECKBOX_TEXT = "NUMBER_CHECKBOX_TEXT"
        DROP_DOWN = "DROP_DOWN"
        FREE_TEXT = "FREE_TEXT"
        NUMBER_INPUT = "NUMBER_INPUT"
        DATE = "DATE"
        IMAGE = "IMAGE"
        BODY_PART = "BODY_PART"
        BARCODE = "BARCODE"


class QuestionnaireHelper:

    def __init__(self, driver):
        self.driver = driver
        self.utils = SeleniumUtils(driver)

    def click_add_questionnaire_button(self):
        """Clicks the 'Add Questionnaire' button."""
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTIONNAIRE)

    def fill_questionnaire_details(self, questionnaire_name, description, language_code, localized_display_name,
                                   localized_welcome_text=None, localized_final_text=None):
        """
        :param questionnaire_name: Name of the questionnaire.
        :param description: Description of the questionnaire.
        :param language_code: Language code for localized fields (e.g., 'de_DE').
        :param localized_display_name: Localized display name for the questionnaire.
        :param localized_welcome_text: Localized welcome text (optional).
        :param localized_final_text: Localized final text (optional).
        """
        # Fill in the questionnaire name
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_NAME, questionnaire_name)

        # Fill in the description
        WebDriverWait(self.driver, 30).until(
            EC.visibility_of_element_located(QuestionnaireSelectors.INPUT_EDITABLE_DESCRIPTION)
        )
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_EDITABLE_DESCRIPTION, description)

        # Fill in the localized display name
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_LOCALIZED_DISPLAY_NAME(language_code), localized_display_name)

        # Fill Welcome Text
        if localized_welcome_text:
            welcome_text_div = WebDriverWait(self.driver, 30).until(
                EC.visibility_of_element_located(QuestionnaireSelectors.INPUT_WELCOME_TEXT_EDITABLE_DIV(language_code))
            )
            self.utils.fill_editable_div(welcome_text_div, localized_welcome_text)

        # Fill Final Text
        if localized_final_text:
            final_text_div = WebDriverWait(self.driver, 30).until(
                EC.visibility_of_element_located(QuestionnaireSelectors.INPUT_FINAL_TEXT_EDITABLE_DIV(language_code))
            )
            self.utils.fill_editable_div(final_text_div, localized_final_text)

    def save_questionnaire_edit_question(self):
        """
        :return: The ID of the newly created questionnaire.
        """
        current_url = self.driver.current_url
        self.utils.click_element(QuestionnaireSelectors.BUTTON_SAVE_AND_EDIT)

        # Wait for redirection and extract questionnaire ID
        WebDriverWait(self.driver, 15).until(EC.url_changes(current_url))
        new_url = self.driver.current_url
        try:
            questionnaire_id = new_url.split("id=")[1]
            return questionnaire_id
        except IndexError:
            raise Exception(f"Failed to extract questionnaire ID from URL: {new_url}")

    def click_add_question_button(self):
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTION)

# TODO [] The helper functions for asserting questionnaires should be implemented here
class QuestionnaireAssertHelper(QuestionnaireHelper):
    pass

# TODO [] Implement ConditionHelper
class ConditionHelper(QuestionnaireHelper):
    def add_condition_to_questionnaire(self, condition_name, condition_type, target_question):
        pass

# TODO [] Implement ScoreHelper
class ScoreHelper(QuestionnaireHelper):
    def add_score_to_questionnaire(self, score_name, calculation_formula, target_questions):
        pass