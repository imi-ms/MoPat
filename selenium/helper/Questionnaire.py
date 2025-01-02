import datetime

from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper
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

class ConditionSelectors:
    BUTTON_ADD_CONDITION = (By.ID, 'addCondition')
    BUTTON_ADD_TARGET = (By.ID, 'addTargetButton')
    BUTTON_SAVE_CONDITION = (By.ID, 'saveCondition')
    BUTTON_BACK_TO_QUESTIONNAIRE = (By.ID, 'backToQuestionnaire')

    CONDITION_LINK = lambda question_id: (By.XPATH, f'//a[contains(@href, "/condition/listQuestionConditions?questionId={question_id}")]')

    DROPDOWN_TRIGGER = (By.ID, "triggerId")
    DROPDOWN_QUESTION = (By.ID, 'questionDropDown0')

    INPUT_THRESHOLD = (By.ID, 'conditionDTOs0.thresholdValue')

    THRESHOLD_TYPE = (By.ID, "conditionDTOs0.thresholdType")
    class ThresholdType:
        SMALLER_THAN = "SMALLER_THAN"
        SMALLER_THAN_EQUALS = "SMALLER_THAN_EQUALS"
        EQUALS = "EQUALS"
        BIGGER_THAN_EQUALS = "BIGGER_THAN_EQUALS"
        BIGGER_THAN = "BIGGER_THAN"
        NOT_EQUALS = "NOT_EQUALS"

    DROPDOWN_TARGET_CLASS = (By.ID, "targetClass0")
    class TargetType:
        QUESTION = "de.imi.mopat.model.Question"
        ANSWER = "de.imi.mopat.model.SelectAnswer"

    DROPDOWN_TARGET_QUESTION = (By.ID, 'targetAnswerQuestionDropDown0')
    DROPDOWN_TARGET_ANSWER = (By.ID, 'questionDropDown0')
    DROPDOWN_ANSWER_TARGET = (By.ID, 'answerDropDown0')

    DROPDOWN_ACTION = (By.ID, 'action0')
    class ActionType:
        DISABLE = "DISABLE"
        ENABLE = "ENABLE"

class QuestionnaireHelper:

    DEFAULT_DESCRIPTION = "Dieser Fragebogen enthält alle bisher implementierten Fragetypen"
    DEFAULT_LANGUAGE_CODE = "de_DE"
    DEFAULT_LOCALIZED_WELCOME_TEXT = 'Ein Willkommenstext für den Fragebogen. Nichts besonderes zu sehen.'
    DEFAULT_LOCALIZED_FINAL_TEXT = 'Dieser Text wird am Ende des Fragebogens gezeigt.'

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper
        self.question_helper = QuestionHelper(driver, navigation_helper)

    def click_add_questionnaire_button(self):
        """Clicks the 'Add Questionnaire' button."""
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTIONNAIRE)

    def fill_questionnaire_details(self, questionnaire_name=None, description=None, language_code=None, localized_display_name=None,
                                   localized_welcome_text=None, localized_final_text=None):
        """
        :param questionnaire_name: Name of the questionnaire (optional).
        :param description: Description of the questionnaire (optional).
        :param language_code: Language code for localized fields (e.g., 'de_DE') (optional).
        :param localized_display_name: Localized display name for the questionnaire (optional).
        :param localized_welcome_text: Localized welcome text (optional).
        :param localized_final_text: Localized final text (optional).
        """
        timestamp: str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        questionnaire_name = questionnaire_name or f"Fragebogen alle Typen {timestamp}"
        description = description or self.DEFAULT_DESCRIPTION
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        localized_display_name = localized_display_name or questionnaire_name
        localized_welcome_text = localized_welcome_text or self.DEFAULT_LOCALIZED_WELCOME_TEXT
        localized_final_text = localized_final_text or self.DEFAULT_LOCALIZED_FINAL_TEXT

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

        return questionnaire_name

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

    def create_questionnaire_with_questions(self, questionnaire_name=None, questionnaire_description=None,
                                            questionnaire_language_code=None, questionnaire_display_name=None,
                                            questionnaire_welcome_text=None, questionnaire_final_text=None, question_types=None):
        """
        Creates a questionnaire and adds all specified question types.

        :param questionnaire_name: Name of the questionnaire.
        :param questionnaire_description: Description of the questionnaire.
        :param questionnaire_language_code: Language code (e.g., 'de_DE').
        :param questionnaire_display_name: Localized display name of the questionnaire.
        :param questionnaire_welcome_text: Welcome text for the questionnaire.
        :param questionnaire_final_text: Final text for the questionnaire.
        :param question_types: A list of methods to add question types.
        :return: A dictionary containing the questionnaire ID and a list of added questions.
        """
        question_types = question_types or self.question_helper.QUESTION_TYPES

        # Navigate to "Manage Questionnaires"
        self.navigation_helper.navigate_to_manage_questionnaires()

        # Create the questionnaire
        self.click_add_questionnaire_button()
        questionnaire_name = self.fill_questionnaire_details(questionnaire_name, questionnaire_description,
                                                  questionnaire_language_code, questionnaire_display_name,
                                                  questionnaire_welcome_text, questionnaire_final_text)
        questionnaire_id = self.save_questionnaire_edit_question()

        # Add questions to the questionnaire
        added_questions = []
        for add_question_method in question_types:
            self.click_add_question_button()
            question_info = add_question_method()  # Add question
            question_info["id"] = self.question_helper.save_question()  # Save the question and retrieve ID
            added_questions.append(question_info)

        return {"id": questionnaire_id, "name": questionnaire_name, "questions": added_questions}


# TODO [] The helper functions for asserting questionnaires should be implemented here
class QuestionnaireAssertHelper(QuestionnaireHelper):
    pass