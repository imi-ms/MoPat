import datetime

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionType
from helper.SeleniumUtils import SeleniumUtils


class QuestionnaireSelectors:
    BUTTON_ADD_QUESTIONNAIRE = (By.ID, "addQuestionnaire")
    BUTTON_ADD_QUESTION = (By.ID, "addQuestion")
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_SAVE_AND_EDIT = (By.ID, "saveEditButton")
    BUTTON_DELETE_LANGUAGE = lambda language_code: (By.ID, f"{language_code}_Delete")
    BUTTON_DELETE_LANGUAGE_MODAL = (By.XPATH, "//button[contains(@class, 'btn-danger') and text()='Entfernen']")

    DROPDOWN_ADD_LANGUAGE = (By.CSS_SELECTOR, "div#languageDropdown.dropdown")
    DROPDOWN_LANGUAGE_ITEM = lambda language_id: (By.CSS_SELECTOR, f"a.dropdown-item#{language_id}")

    DELETE_LANGUAGE_MODAL = (By.ID, "deleteLanguageModal")
    DELETE_LAST_LANGUAGE_MODAL = (By.ID, "deleteLastLanguageModal")
    DELETE_LAST_LANGUAGE_OK_BUTTON = (By.CSS_SELECTOR, "#deleteLastLanguageModal .btn-secondary")

    INPUT_NAME = (By.ID, "name")
    INPUT_LOGO = (By.ID, "file")
    INPUT_LOCALIZED_DISPLAY_NAME = lambda language_code: (By.ID, f"localizedDisplayName{language_code}")
    INPUT_EDITABLE_DESCRIPTION = (By.CSS_SELECTOR, "div.note-editable")
    INPUT_WELCOME_TEXT_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedWelcomeTextCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_FINAL_TEXT_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedFinalTextCollapsableText_{language_code}"]/div/div[2]/div[2]')

    QUESTIONNAIRE_TABLE = (By.ID, "questionnaireTable")
    TABLE_ROWS = (By.CSS_SELECTOR, "table#questionnaireTable tbody tr")
    TABLE_FIRST_ROW = (By.XPATH, "//tbody/tr[not(@id='emptyRow')][1]")
    FLAG_ICONS = (By.CSS_SELECTOR, "table#questionnaireTable img[title]")
    PAGINATION = (By.ID, "questionnaireTable_paginate")
    SEARCH_BOX = (By.CSS_SELECTOR, "#questionnaireTable_filter input[type='search']")
    ACTION_BUTTONS = (By.CSS_SELECTOR, "td.actionColumn > div.d-none.d-xl-block > a.link")
    LANGUAGE_FLAG_ICONS = (By.CSS_SELECTOR, ".languageLabel img")

class QuestionnaireHelper:

    DEFAULT_DESCRIPTION = "This description of the questionnaire is a dummy text."
    DEFAULT_LANGUAGE_CODE = "de_DE"
    DEFAULT_LANGUAGE_CODE_EN = "en"
    DEFAULT_LOCALIZED_WELCOME_TEXT = 'A welcome text for the questionnaire. Nothing special to see.'
    DEFAULT_LOCALIZED_FINAL_TEXT = 'This text is shown at the end of the questionnaire.'

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver, navigation_helper)
        self.navigation_helper = navigation_helper
        self.question_helper = QuestionHelper(driver, navigation_helper)

    def click_add_questionnaire_button(self):
        """Clicks the 'Add Questionnaire' button."""
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTIONNAIRE)

    def fill_questionnaire_details(self, questionnaire_name=None, description=None, language_code=None, localized_display_name=None,
                                   localized_welcome_text=None, localized_final_text=None, question_types=None):
        """
        :param questionnaire_name: Name of the questionnaire (optional).
        :param description: Description of the questionnaire (optional).
        :param language_code: Language code for localized fields (e.g., 'de_DE') (optional).
        :param localized_display_name: Localized display name for the questionnaire (optional).
        :param localized_welcome_text: Localized welcome text (optional).
        :param localized_final_text: Localized final text (optional).
        """
        timestamp: str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        fragetypen_text = ''
        if question_types:
            delimiter = "+"
            fragetypen_text = f" (Fragetypen: {delimiter.join(str(question_type.value) for question_type in question_types)})"

        questionnaire_name = questionnaire_name or f"Fragebogen {timestamp}{fragetypen_text}"
        description = description or self.DEFAULT_DESCRIPTION
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        localized_display_name = localized_display_name or questionnaire_name
        localized_welcome_text = localized_welcome_text or self.DEFAULT_LOCALIZED_WELCOME_TEXT
        localized_final_text = localized_final_text or self.DEFAULT_LOCALIZED_FINAL_TEXT

        # Fill in the questionnaire name
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_NAME, questionnaire_name)

        # Fill in the description
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
            QuestionnaireSelectors.INPUT_EDITABLE_DESCRIPTION))
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_EDITABLE_DESCRIPTION, description)

        # Fill in the localized display name
        self.utils.fill_text_field(QuestionnaireSelectors.INPUT_LOCALIZED_DISPLAY_NAME(language_code), localized_display_name)

        # Fill Welcome Text
        if localized_welcome_text:
            welcome_text_div = WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
                QuestionnaireSelectors.INPUT_WELCOME_TEXT_EDITABLE_DIV(language_code)))
            self.utils.fill_editable_div(welcome_text_div, localized_welcome_text)

        # Fill Final Text
        if localized_final_text:
            final_text_div = WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
                QuestionnaireSelectors.INPUT_FINAL_TEXT_EDITABLE_DIV(language_code)))
            self.utils.fill_editable_div(final_text_div, localized_final_text)

        return questionnaire_name

    def add_language(self, language_code):
        """
        :param language_code: The Code of the language to add (e.g., 'en' for English).
        """
        try:
            # Click on the "Add Language" button
            add_language_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                QuestionnaireSelectors.DROPDOWN_ADD_LANGUAGE))
            add_language_button.click()

            # Select the desired language from the dropdown
            language_option = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                QuestionnaireSelectors.DROPDOWN_LANGUAGE_ITEM(language_code)))
            language_option.click()

        except TimeoutException:
            raise AssertionError(f"Timed out while trying to add language '{language_code}'.")


    def click_add_question_button(self):
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTION)

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


    def save_questionnaire(self):
        """
        :return: The ID of the newly created questionnaire.
        """
        current_url = self.driver.current_url
        self.utils.click_element(QuestionnaireSelectors.BUTTON_SAVE)

        # Wait for redirection and extract questionnaire ID
        WebDriverWait(self.driver, 15).until(EC.url_changes(
            current_url))

        WebDriverWait(self.driver, 30).until(EC.presence_of_element_located(
            QuestionnaireSelectors.TABLE_FIRST_ROW))

        # Find the last row in the table
        first_row = self.driver.find_element(*QuestionnaireSelectors.TABLE_FIRST_ROW)

        questionnaire_id = first_row.get_attribute("id")

        if not questionnaire_id:
            raise Exception("The ID of the last added questionnaire could not be found.")

        return questionnaire_id

    def create_questionnaire_with_questions(self, questionnaire_name=None, questionnaire_description=None,
                                            questionnaire_language_code=None, questionnaire_display_name=None,
                                            questionnaire_welcome_text=None, questionnaire_final_text=None, question_types=None):
        """
        :param questionnaire_name: Name of the questionnaire.
        :param questionnaire_description: Description of the questionnaire.
        :param questionnaire_language_code: Language code (e.g., 'de_DE').
        :param questionnaire_display_name: Localized display name of the questionnaire.
        :param questionnaire_welcome_text: Welcome text for the questionnaire.
        :param questionnaire_final_text: Final text for the questionnaire.
        :param question_types: A list of question types.
        :return: A dictionary containing the questionnaire ID and a list of added questions.
        """
        excluded_question_types = {QuestionType.IMAGE, QuestionType.BODY_PART, QuestionType.BARCODE}
        question_types = question_types or [question_type for question_type in QuestionType if question_type not in excluded_question_types]

        # Navigate to "Manage Questionnaires"
        self.navigation_helper.navigate_to_manage_questionnaires()

        # Create the questionnaire
        self.click_add_questionnaire_button()
        questionnaire_name = self.fill_questionnaire_details(questionnaire_name, questionnaire_description,
                                                  questionnaire_language_code, questionnaire_display_name,
                                                  questionnaire_welcome_text, questionnaire_final_text, question_types)
        questionnaire_id = self.save_questionnaire_edit_question()

        # Add questions to the questionnaire
        added_questions = []
        for question_type in question_types:
            self.click_add_question_button()
            question_info = self.question_helper.add_question_by_type_default_value(question_type)
            question_info["id"] = self.question_helper.save_question()  # Save the question and retrieve ID
            added_questions.append(question_info)

        return {"id": questionnaire_id, "name": questionnaire_name, "questions": added_questions}


class QuestionnaireAssertHelper(QuestionnaireHelper):

    def assert_questionnaire_list(self):
        try:
            # Validate table with questionnaires
            questionnaire_table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.QUESTIONNAIRE_TABLE))
            assert questionnaire_table.is_displayed(), "The table with available questionnaires is not displayed."

            # Validate flag icons
            flag_icons = self.driver.find_elements(*QuestionnaireSelectors.FLAG_ICONS)
            assert len(flag_icons) > 0, "No flag icons are displayed for the questionnaires."

            # Validate pagination
            pagination = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.PAGINATION))
            assert pagination.is_displayed(), "Pagination is not displayed."

            # Validate search box
            search_box = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.SEARCH_BOX))
            assert search_box.is_displayed(), "Search box is not displayed."

            # Validate buttons in the action column
            rows = self.driver.find_elements(*QuestionnaireSelectors.TABLE_ROWS)
            assert len(rows) > 0, "No rows found in the questionnaire table."

            for index, row in enumerate(rows, start=1):
                action_buttons = row.find_elements(*QuestionnaireSelectors.ACTION_BUTTONS)
                assert len(action_buttons) == 5, f"Row {index} does not have exactly 5 action buttons. Found: {len(action_buttons)}"

            # Validate the button to create a new questionnaire
            create_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.BUTTON_ADD_QUESTIONNAIRE))
            assert create_button.is_displayed(), "Button to create a new questionnaire is not displayed."

        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the questionnaire list view.")
        except AssertionError as e:
            raise e

    def assert_questionnaire_fill_page(self, add_language_code = None):
        add_language_code = add_language_code or self.DEFAULT_LANGUAGE_CODE_EN
        try:
            # Validate dropdown to add language
            dropdown = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.DROPDOWN_ADD_LANGUAGE))
            assert dropdown.is_displayed(), "Language dropdown for adding a language is not displayed."

            # Validate input for questionnaire name
            questionnaire_name_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_NAME))
            assert questionnaire_name_input.is_displayed(), "Input for questionnaire name is not displayed."

            # Validate input for logo
            logo_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_LOGO))
            logo_display = self.driver.execute_script("return window.getComputedStyle(arguments[0]).display;",logo_input)
            assert logo_display != "none", "Logo input display is set to 'none'."

            # Validate WYSIWYG editor for description
            wysiwyg_description = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_EDITABLE_DESCRIPTION))
            assert wysiwyg_description.is_displayed(), "WYSIWYG editor for description is not displayed."

            # Validate input for display name
            display_name_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_LOCALIZED_DISPLAY_NAME(self.DEFAULT_LANGUAGE_CODE)))
            assert display_name_input.is_displayed(), "Input for display name is not displayed."

            # Validate WYSIWYG editor for welcome text
            wysiwyg_welcome_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_WELCOME_TEXT_EDITABLE_DIV(self.DEFAULT_LANGUAGE_CODE)))
            assert wysiwyg_welcome_text.is_displayed(), "WYSIWYG editor for welcome text is not displayed."

            # Validate WYSIWYG editor for end text
            wysiwyg_end_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionnaireSelectors.INPUT_FINAL_TEXT_EDITABLE_DIV(self.DEFAULT_LANGUAGE_CODE)))
            assert wysiwyg_end_text.is_displayed(), "WYSIWYG editor for end text is not displayed."

            # Validate flag icons
            flag_icons = self.driver.find_elements(
                *QuestionnaireSelectors.LANGUAGE_FLAG_ICONS)
            assert len(flag_icons) > 0, "No flag icons are displayed for inputs."

            # Add a language
            self.add_language(add_language_code)

            # Validate duplicated inputs with flag icons
            new_flag_icons = self.driver.find_elements(
                *QuestionnaireSelectors.LANGUAGE_FLAG_ICONS)
            assert len(new_flag_icons) == len(flag_icons) * 2, "Adding a language did not duplicate inputs with flag icons."

            # Delete a language
            delete_added_language_button = self.driver.find_elements(
                *QuestionnaireSelectors.BUTTON_DELETE_LANGUAGE(add_language_code))
            assert len(delete_added_language_button) > 0, "No delete buttons for languages are displayed."
            delete_added_language_button[0].click()

            # Close the modal
            delete_language_modal = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                QuestionnaireSelectors.DELETE_LANGUAGE_MODAL))
            remove_button = delete_language_modal.find_element(
                *QuestionnaireSelectors.BUTTON_DELETE_LANGUAGE_MODAL)
            remove_button.click()

            # Validate deletion removes duplicated inputs
            updated_flag_icons = self.driver.find_elements(
                *QuestionnaireSelectors.LANGUAGE_FLAG_ICONS)
            assert len(updated_flag_icons) == len(flag_icons), "Deleting a language did not remove duplicated inputs."

            # Validate deleting the last language is not possible
            delete_default_language_button = self.driver.find_elements(
                *QuestionnaireSelectors.BUTTON_DELETE_LANGUAGE(self.DEFAULT_LANGUAGE_CODE))
            if len(delete_default_language_button) == 1:
                delete_default_language_button[0].click()

                # Wait until the modal for the last language becomes visible
                last_language_modal = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                    QuestionnaireSelectors.DELETE_LAST_LANGUAGE_MODAL))
                assert last_language_modal.is_displayed(), "Modal for deleting the last language is not displayed."

                # Click the 'Ok' button in the modal
                ok_button = last_language_modal.find_element(
                    *QuestionnaireSelectors.DELETE_LAST_LANGUAGE_OK_BUTTON)
                ok_button.click()

            questionnaire_name = self.fill_questionnaire_details()

            self.utils.scroll_to_element(QuestionnaireSelectors.BUTTON_SAVE)

            # Wait until the save button is visible
            WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                QuestionnaireSelectors.BUTTON_SAVE))

            # Validate save button creates a questionnaire and redirects
            questionnaire_id = self.save_questionnaire()

            WebDriverWait(self.driver, 10).until(EC.url_contains("/questionnaire/list"))

            return {
                "id": questionnaire_id,
                "name": questionnaire_name
            }
        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the questionnaire fill page.")
        except AssertionError as e:
            raise e