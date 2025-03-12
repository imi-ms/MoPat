from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.Question import QuestionType
from helper.SeleniumUtils import SeleniumUtils, DropdownMethod
import random


class SurveySelectors:
    BUTTON_SHOW_BUNDLES = (By.ID, "showBundles")
    BUTTON_START_SURVEY = (By.ID, "startSurveyButton")
    BUTTON_NEXT_QUESTION = (By.ID, "buttonNext")
    BUTTON_CHECK_CASE = (By.ID, "checkButton")
    BUTTON_ADDITIONAL_INFORMATION = (By.ID, "additionalInformationButton")
    BUTTON_CHECK_CASE_NUMBER = (By.ID, "checkButton")

    DROPDOWN_CLINIC_SELECTION = (By.ID, "clinic-selection")
    DROPDOWN_BUNDLE_SELECTION = (By.ID, "bundle-selection")
    DROPDOWN_LANGUAGE_SELECTION = (By.ID, "bundle-language-selection")
    DROPDOWN_GENERIC = (By.ID, "dropDown")
    DROPDOWN_LANGUAGE_SELECTOR = (By.ID, "localeChanger")

    INPUT_CASE_NUMBER = (By.ID, "caseNumber")
    INPUT_NUMBER = (By.ID, "numberInput")
    INPUT_DATE = (By.ID, "dateInput")
    INPUT_TEXTAREA = (By.ID, "textarea")

    RADIO_REGISTER = (By.ID, "register")
    RADIO_SEARCH_HIS = (By.ID, "isHIS")
    RADIO_PSEUDONYMIZATION = (By.ID, "pseudonym")

    SLIDER = (By.ID, "range")

    TEXT_QUESTION_CONTENT = (By.ID, "questionContent")
    TEXT_QUESTION_TITLE = (By.ID, "questionTitle")

    LABEL_FOR_CHECKBOX = lambda selected_value: (By.CSS_SELECTOR, f"label[for='numberedCheckbox_{selected_value}']")
    LABEL_BY_OPTION_TEXT = lambda option_text: (By.XPATH, f"//div[@class='right' and text()='{option_text}']/..")
    
    TAB_PATIENT_REGISTRATION = (By.CSS_SELECTOR, "#radioSelectHIS > div > label:nth-child(2)")
    TAB_PATIENT_DATA_AUTOMATION = (By.CSS_SELECTOR, "#radioSelectHIS > div > label:nth-child(4)")
    TAB_PATIENT_PSEUDONYMIZATION = (By.CSS_SELECTOR, "#radioSelectHIS > div > label:nth-child(6)")
    

class SurveyHelper:

    # Default values for survey interactions
    DEFAULT_LANGUAGE_CODE = "de_DE"
    DEFAULT_FREETEXT = "Default answer for freetext questions"
    DEFAULT_SLIDER_POSITION = 0.5
    DEFAULT_DATE = "2025-12-12"

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper

    def click_next_button(self):
        self.utils.click_element(SurveySelectors.BUTTON_NEXT_QUESTION)

    def start_survey(self,clinic_name, configuration=None, case_number=None):
        """
        Starts a survey as a user by interacting with the clinic selection,
        configuration options, and case number input.

        :param configuration: The configuration to select (e.g., "searchHIS" or "pseudonym").
        :param clinic_name: The name of the clinic to select.
        :param case_number: The case number to input.
        """
        try:
            # Select clinic
            clinic_dropdown = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(SurveySelectors.DROPDOWN_CLINIC_SELECTION)
            )

            # Check if the dropdown is disabled
            is_disabled = clinic_dropdown.get_attribute("disabled")
            if is_disabled:
                # Validate that the preselected clinic matches the expected clinic
                selected_option = clinic_dropdown.find_element(By.CSS_SELECTOR, "option[selected]")
                selected_clinic = selected_option.text
                assert selected_clinic == clinic_name, f"Expected clinic '{clinic_name}', but got '{selected_clinic}'"
            else:
                # Select the clinic from the dropdown
                self.utils.select_dropdown(SurveySelectors.DROPDOWN_CLINIC_SELECTION, clinic_name, DropdownMethod.VISIBLE_TEXT)

            # Select configuration
            if configuration is not None:
                config_selector = configuration['config_selector']
                self.utils.click_element(config_selector)

            # Generate a random 5-digit case number if not provided
            if case_number is None:
                case_number = str(random.randint(10000, 99999))
            # Input case number
            self.utils.fill_text_field(SurveySelectors.INPUT_CASE_NUMBER, case_number)

            # Click check button
            self.utils.click_element(SurveySelectors.BUTTON_CHECK_CASE)

        except TimeoutException:
            raise Exception("Timeout while starting the survey process.")
        except Exception as e:
            raise Exception(f"Error while starting survey: {e}")

    def complete_survey(self):
        self.click_next_button()

        # Wait for the survey completion message to appear
        WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            SurveySelectors.TEXT_QUESTION_CONTENT))

        # Validate that the survey completion page is displayed
        completion_content = self.driver.find_element(*SurveySelectors.TEXT_QUESTION_CONTENT).text

        # Optional: Return the completion content for further validation or logging
        return completion_content

    def proceed_to_bundle_selection(self, bundle_name, language_code=None):
        """
        :param bundle_name: The name of the bundle to select.
        :param language_code: The code of the language to select (e.g., "de_DE") (optional).
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        try:
            # Click "Show Bundles" button to proceed
            self.utils.click_element(SurveySelectors.BUTTON_SHOW_BUNDLES)

            # Select bundle
            self.utils.select_dropdown(SurveySelectors.DROPDOWN_BUNDLE_SELECTION, bundle_name, DropdownMethod.VISIBLE_TEXT)

            # Select language if provided
            if language_code:
                self.utils.select_dropdown(SurveySelectors.DROPDOWN_LANGUAGE_SELECTION, language_code, DropdownMethod.VALUE)

            # Start the survey
            self.utils.click_element(SurveySelectors.BUTTON_START_SURVEY)

        except TimeoutException:
            raise Exception("Timeout while proceeding to bundle selection.")
        except Exception as e:
            raise Exception(f"Error while proceeding to bundle selection: {e}")

    def answer_multiple_choice_question(self, question):
        """
        :param question: A dictionary containing the question details.
        """
        # Ensure options is always a valid list
        options = question.get("options", []) if isinstance(question.get("options"), list) else ["Option 1"]
        min_answers = question.get("min_answers",0)
        max_answers = question.get("max_answers",1)

        # Validate min_answers and max_answers
        if min_answers < 0 or max_answers > len(options) or min_answers > max_answers:
            raise ValueError("Invalid min_answers or max_answers values.")

        # Select at least min_answers options, but not more than max_answers
        for i, option_text in enumerate(options[:max_answers]):
            label = self.driver.find_element(*SurveySelectors.LABEL_BY_OPTION_TEXT(option_text))
            label.click()

    def answer_slider_question(self, question, slider_position=None):
        """
        :param question: The question dictionary containing slider configuration.
        :param slider_position: A float value between 0 and 1 that determines the slider position.
        """
        slider_position = slider_position or self.DEFAULT_SLIDER_POSITION

        # Ensure slider_position is between 0 and 1
        if not (0 <= slider_position <= 1):
            raise ValueError("slider_position must be a value between 0 and 1.")

        # Extract slider properties from the question
        min_value = question.get("min_value",0)
        max_value = question.get("max_value",10)

        # Calculate the target value for the slider
        target_value = min_value + (slider_position * (max_value - min_value))

        # Set the slider value in the DOM
        self.driver.execute_script("document.getElementById('range').value=arguments[0];", target_value)

        # Trigger events to simulate user interaction with the slider
        slider_element = self.driver.find_element(*SurveySelectors.SLIDER)
        self.driver.execute_script("arguments[0].dispatchEvent(new Event('mousedown'));", slider_element)
        self.driver.execute_script("arguments[0].dispatchEvent(new Event('mouseup'));", slider_element)

    def answer_number_checkbox_question(self, question, selected_value=None):
        """
        :param question: The question dictionary containing numeric checkbox configuration.
        :param selected_value: A numeric value to select, between min_value and max_value.
        """
        # Extract the min, max, and step values from the question
        min_value = question.get("min_value",0)
        max_value = question.get("max_value",10)
        step_size = question.get("step_size",1)
        selected_value = selected_value or min_value

        if not (min_value <= selected_value <= max_value):
            raise ValueError(f"Value {selected_value} is not within the allowed range {min_value} to {max_value}.")
        if (selected_value - min_value) % step_size != 0:
            raise ValueError(f"Value {selected_value} does not match the step size {step_size}.")

        # Find and select the checkbox
        label_element = self.driver.find_element(*SurveySelectors.LABEL_FOR_CHECKBOX(selected_value))
        label_element.click()

    def answer_text_question(self, text=None):
        """
        :param text: The text to input. Defaults to `DEFAULT_FREETEXT`.
        """
        text = text or self.DEFAULT_FREETEXT
        self.driver.find_element(*SurveySelectors.INPUT_TEXTAREA).send_keys(text)

    def answer_number_checkbox_text_question(self, question, selected_value=None, text=None):
        """
        :param question: A dictionary containing numeric checkbox configuration (min/max/step values).
        :param selected_value: The numeric value to select. Defaults to the minimum value.
        :param text: The text to input in the associated text field. Defaults to `DEFAULT_FREETEXT`.
        """
        self.answer_number_checkbox_question(question, selected_value)
        self.answer_text_question(text)

    def answer_date_question(self, date=None):
        """
        :param question: A dictionary containing question details. The function uses a default date if none is specified.
        :param date: A string representing the date to set (e.g., "2024-12-20").
        """
        date = date or self.DEFAULT_DATE
        self.driver.execute_script("document.getElementById('dateInput').valueAsDate = new Date(arguments[0]);", date)
        self.driver.execute_script("document.getElementById('dateInput').dispatchEvent(new Event('blur'));")

    def select_dropdown_option(self, question, option_text=None):
        """
        :param question: A dictionary containing the dropdown question details, including available options.
        :param option_text: The visible text of the dropdown option to select. If not provided,
            the first option from the "options" list in the question will be selected.
        """
        options = question.get("options", []) if isinstance(question.get("options"), list) else ["Option 1"]
        option_text = option_text or options[0]

        Select(self.driver.find_element(*SurveySelectors.DROPDOWN_GENERIC)).select_by_visible_text(option_text)

    def answer_numbered_input_question(self, question, value=None):
        """
        :param question: The question dictionary containing the numbered input configuration.
        :param value: The numeric value to input. Defaults to the minimum value from the question configuration.
        """
        # Extract constraints from the question
        min_value = question.get("min_value", 0)
        max_value = question.get("max_value", 10)
        step_size = question.get("step_size", 1)

        # Use default value if not explicitly provided
        if value is None:
            value = min_value


        # Locate the input element and set the value
        input_element = self.driver.find_element(*SurveySelectors.INPUT_NUMBER)
        input_element.clear()
        input_element.send_keys(str(value))

        # Trigger the `onchange` event if necessary
        self.driver.execute_script("arguments[0].dispatchEvent(new Event('change'));", input_element)

    def end_survey(self):
        # Get the current URL before clicking the "Next" button
        current_url = self.driver.current_url

        # Click the "Next" button
        self.click_next_button()

        # Wait until the URL changes
        WebDriverWait(self.driver, 15).until(
            lambda driver: driver.current_url != current_url,
            message="URL did not change after clicking 'Next' button."
        )

class SurveyAssertHelper(SurveyHelper):

    def assertion_for_welcome_text(self, text):
        WebDriverWait(self.driver, 10).until(
            EC.text_to_be_present_in_element(
                SurveySelectors.TEXT_QUESTION_CONTENT, text))
        welcome_text = self.driver.find_element(*SurveySelectors.TEXT_QUESTION_CONTENT).text
        assert text in welcome_text, "Welcome text not found!"

    def assertion_for_question_title(self, question):
        if question["type"] == QuestionType.INFO_TEXT:
            WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                SurveySelectors.TEXT_QUESTION_CONTENT))
            question_text = self.driver.find_element(*SurveySelectors.TEXT_QUESTION_CONTENT).text
            assert question['text'] in question_text, "Question text not found!"
        else:
            WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                SurveySelectors.TEXT_QUESTION_TITLE))
            question_text = self.driver.find_element(*SurveySelectors.TEXT_QUESTION_TITLE).text
            assert question['text'] in question_text, "Question title not found!"