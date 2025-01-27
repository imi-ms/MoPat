import time
from functools import partial

from selenium import webdriver
from selenium.common import TimeoutException, ElementClickInterceptedException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils
from enum import Enum

class QuestionSelectors:
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_ADD_ANSWER = (By.ID, "addAnswerButton")

    DROPDOWN_QUESTION_TYPE = (By.ID, "questionTypeDropDown")
    CHECKBOX_IS_REQUIRED = (By.ID, 'isRequired1')
    INPUT_MIN_NUMBER_ANSWERS = (By.ID, "minNumberAnswers")
    INPUT_MAX_NUMBER_ANSWERS = (By.ID, "maxNumberAnswers")

    INPUT_QUESTION_TEXT_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedQuestionTextCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_SLIDER_MIN_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMinimumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_SLIDER_MAX_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMaximumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_NUMERIC_CHECKBOX_MIN_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMinimumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_NUMERIC_CHECKBOX_MAX_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMaximumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_NUMERIC_CHECKBOX_FREETEXT_MIN_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMinimumTextNumberCheckboxCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_NUMERIC_CHECKBOX_FREETEXT_MAX_EDITABLE_DIV = lambda language_code: (By.XPATH, f'//*[@id="localizedMaximumTextNumberCheckboxCollapsableText_{language_code}"]/div/div[2]/div[2]')

    TABLE_QUESTION = (By.ID, "questionTable")
    TABLE_LAST_ROW = (By.XPATH, "//tbody/tr[last()]")
    TABLE_ROWS = (By.XPATH, "//tbody/tr")

    class QuestionTypes():
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


class QuestionHelper:

    # Default values for questions
    DEFAULT_LANGUAGE_CODE = "de_DE"
    DEFAULT_LABELS = ("Low", "High")
    DEFAULT_MIN_VALUE = 0
    DEFAULT_MAX_VALUE = 10
    DEFAULT_OPTIONS = ["Option 1", "Option 2", "Option 3"]
    DEFAULT_QUESTION_TEXT = "Default Question Text"
    DEFAULT_STEP_SIZE = 1

    DEFAULT_MC_OPTIONS =["Option 1", "Option 2", "Option 3"]
    DEFAULT_MC_MIN_VALUE = 0
    DEFAULT_MC_MAX_VALUE = len(DEFAULT_MC_OPTIONS)
    DEFAULT_FREE_TEXT_LABEL = "Freetext Label"
            
    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper
        self.QUESTION_TYPE_MAPPING = {
            QuestionSelectors.QuestionTypes.INFO_TEXT: partial(self.add_question_info_text),
            QuestionSelectors.QuestionTypes.MULTIPLE_CHOICE: partial(self.add_question_multiple_choice),
            QuestionSelectors.QuestionTypes.SLIDER: partial(self.add_question_slider_question),
            QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX: partial(self.add_question_numeric_checkbox),
            QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX_TEXT: partial(self.add_question_numeric_checkbox_freetext),
            QuestionSelectors.QuestionTypes.DATE: partial(self.add_question_date_question),
            QuestionSelectors.QuestionTypes.DROP_DOWN: partial(self.add_question_dropdown),
            QuestionSelectors.QuestionTypes.NUMBER_INPUT: partial(self.add_question_numeric_question),
            QuestionSelectors.QuestionTypes.FREE_TEXT: partial(self.add_question_freetext),
        }
        self.QUESTION_TYPES = [
            QuestionSelectors.QuestionTypes.INFO_TEXT,
            QuestionSelectors.QuestionTypes.MULTIPLE_CHOICE,
            QuestionSelectors.QuestionTypes.SLIDER,
            QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX,
            QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX_TEXT,
            QuestionSelectors.QuestionTypes.DATE,
            QuestionSelectors.QuestionTypes.DROP_DOWN,
            QuestionSelectors.QuestionTypes.NUMBER_INPUT,
            QuestionSelectors.QuestionTypes.FREE_TEXT,
        ]

    def save_question(self):
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        return self.get_last_added_question_id()

    def get_last_added_question_id(self):
        """
        :return: ID of the question as a string.
        """
        # Wait until the table rows are loaded
        WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located(QuestionSelectors.TABLE_LAST_ROW)
        )

        # Find the last row in the table
        last_row = self.driver.find_element(*QuestionSelectors.TABLE_LAST_ROW)

        # Extract the ID from the `id` attribute of the last row
        question_id = last_row.get_attribute("id")

        # Ensure the ID is found
        if not question_id:
            raise Exception("The ID of the last question could not be found.")

        return question_id

    def add_question_info_text(self, language_code=None, question_text=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the info text question.
        :return: A dictionary containing the question type and text for validation.
        """
        question_type = "it"
        if not question_text:
            question_text = f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE

        # Wait for the dropdown to be visible
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))

        # Select the "Info-Text" option by its value
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(QuestionSelectors.QuestionTypes.INFO_TEXT)

        # Wait until the page is fully loaded
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill the editable div for the question text
        info_text_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))

        self.utils.fill_editable_div(info_text_div, question_text)

        # Return the question information for later validation
        return {"type": QuestionSelectors.QuestionTypes.INFO_TEXT, "text": question_text}

    def add_question_multiple_choice(self, language_code=None, is_required=False, question_text=None, options=None, min_answers=None, max_answers=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the multiple-choice question.
        :param is_required: Indicates if the question is required (default: False).
        :param options: A list of answer options for the question.
        :param min_answers: The minimum number of answers required.
        :param max_answers: The maximum number of answers allowed.
        :return: A dictionary containing the question type, text, and options for validation.
        """
        question_type = "mc"
        if not question_text:
            question_text = f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        options = options or self.DEFAULT_OPTIONS
        min_answers = min_answers or self.DEFAULT_MC_MIN_VALUE
        max_answers = max_answers or self.DEFAULT_MC_MAX_VALUE

        # Wait for the dropdown to be visible and select "Mehrfachauswahl"
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value \
            (QuestionSelectors.QuestionTypes.MULTIPLE_CHOICE)

        # Wait until the page is fully loaded
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill the editable div for the question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Set min and max answers
        self.driver.find_element(*QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS).send_keys(str(min_answers))
        self.driver.find_element(*QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS).send_keys(str(max_answers))

        # Add options
        clear_and_send_keys_js = """document.querySelector('textarea[name="{name}"]').value=arguments[0];"""
        clear_and_send_keys_js2 = """document.querySelector('input[name="{name}"]').value=arguments[0];"""

        for i, option in enumerate(options):
            self.driver.execute_script(clear_and_send_keys_js.format(name=f"answers[{i}].localizedLabel[{language_code}]"), option)
            self.driver.execute_script(clear_and_send_keys_js2.format(name=f"answers[{i}].codedValue"), option)

            # Add new answer field except for the last option
            if i < len(options) - 1:
                add_button = self.driver.find_element(By.ID, 'addAnswerButton')
                self.driver.execute_script("arguments[0].click();", add_button)

        # Return the question information for later validation
        return {
            "type": QuestionSelectors.QuestionTypes.MULTIPLE_CHOICE,
            "text": question_text,
            "options": options,
            "min_answers": min_answers,
            "max_answers": max_answers,
        }

    def add_question_slider_question(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None, labels=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the slider question.
        :param is_required: Indicates if the question is required (default: False).
        :param min_value: The minimum value for the slider.
        :param max_value: The maximum value for the slider.
        :param step_size: The step size for the slider.
        :param labels: A tuple containing two labels (min label, max label) for the slider.
        :return: A dictionary containing the question type, text, and slider configuration for validation.
        """
        question_type = "sq"
        if not question_text:
            question_text = f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE
        labels = labels or self.DEFAULT_LABELS

        # Wait for the dropdown to be visible
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))

        # Select the "Slider" option by its value
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(
            QuestionSelectors.QuestionTypes.SLIDER
        )

        # Wait until the page is fully loaded
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill the editable div for the question text
        question_text_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_text_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Set slider configuration
        self.driver.find_element(By.NAME, "answers[0].minValue").send_keys(str(min_value))
        self.driver.find_element(By.NAME, "answers[0].maxValue").send_keys(str(max_value))
        self.driver.find_element(By.NAME, "answers[0].stepsize").send_keys(str(step_size))

        # Set labels for the slider
        slider_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_SLIDER_MIN_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(slider_text_min_div, labels[0])
        slider_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_SLIDER_MAX_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(slider_text_max_div, labels[1])

        # Return the question information for later validation
        return {
            "type": QuestionSelectors.QuestionTypes.SLIDER,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "labels": labels,
        }

    def add_question_numeric_checkbox(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None, labels=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the numeric checkbox question.
        :param is_required: Indicates if the question is required (default: False).
        :param min_value: The minimum value for the numeric checkboxes.
        :param max_value: The maximum value for the numeric checkboxes.
        :param step_size: The step size between numeric checkbox values.
        :param labels: A tuple containing two labels (min label, max label) for the numeric checkboxes.
        :return: A dictionary containing the question type, text, and numeric checkbox configuration for validation.
        """
        question_type = "nc"
        if not question_text:
            question_text = f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value if min_value is not None else self.DEFAULT_MIN_VALUE
        max_value = max_value if max_value is not None else self.DEFAULT_MAX_VALUE
        step_size = step_size if step_size is not None else self.DEFAULT_STEP_SIZE
        labels = labels or self.DEFAULT_LABELS

        # Wait for the dropdown to be visible and select "Nummerierte Checkboxen"
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(
            QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX
        )

        # Wait until the page is fully loaded
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill the editable div for the question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Set numeric values
        self.driver.find_element(By.NAME, "answers[0].minValue").send_keys(str(min_value))
        self.driver.find_element(By.NAME, "answers[0].maxValue").send_keys(str(max_value))
        self.driver.find_element(By.NAME, "answers[0].stepsize").send_keys(str(step_size))

        # Set labels
        nc_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_NUMERIC_CHECKBOX_MIN_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(nc_text_min_div, labels[0])
        nc_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_NUMERIC_CHECKBOX_MAX_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(nc_text_max_div, labels[1])

        # Return the question information for later validation
        return {
            "type": QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "labels": labels,
        }

    def add_question_numeric_checkbox_freetext(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None, freetext_label=None, labels=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the numeric checkbox question.
        :param min_value: The minimum value for the numeric checkboxes.
        :param max_value: The maximum value for the numeric checkboxes.
        :param step_size: The step size between numeric checkbox values.
        :param freetext_label: Label for the freetext field.
        :param labels: A tuple containing two labels (min label, max label) for the numeric checkboxes + freetext.
        :return: A dictionary containing the question type and configuration for validation.
        """
        question_type = "ncf"
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE
        freetext_label = freetext_label or self.DEFAULT_FREE_TEXT_LABEL
        labels = labels or self.DEFAULT_LABELS

        # Select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX_TEXT)

        # Wait for page to load
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Set numeric values
        self.driver.find_element(By.CSS_SELECTOR, "#sliderFreeTextField input[name='answers[0].minValue']").send_keys(str(min_value))
        self.driver.find_element(By.CSS_SELECTOR, "#sliderFreeTextField input[name='answers[0].maxValue']").send_keys(str(max_value))
        self.driver.find_element(By.CSS_SELECTOR, "#sliderFreeTextField input[name='answers[0].stepsize']").send_keys(str(step_size))

        # Set labels
        ncf_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_NUMERIC_CHECKBOX_FREETEXT_MIN_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(ncf_text_min_div, labels[0])
        ncf_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_NUMERIC_CHECKBOX_FREETEXT_MAX_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(ncf_text_max_div, labels[1])

        # Set freetext label
        self.driver.find_element(By.NAME, f"answers[0].localizedFreetextLabel[{language_code}]").send_keys(freetext_label)

        # Return question information
        return {
            "type": QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX_TEXT,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "freetext_label": freetext_label,
        }

    def add_question_date_question(self, language_code=None, is_required=False, question_text=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the date question.
        :return: A dictionary containing the question type and text for validation.
        """
        question_type = "dq"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"

        # Select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(QuestionSelectors.QuestionTypes.DATE)

        # Wait for page to load
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)


        # Return question information
        return {
            "type": QuestionSelectors.QuestionTypes.DATE,
            "text": question_text,
        }

    def add_question_dropdown(self, language_code=None, is_required=False, question_text=None, options=None):
        """
        :param is_required: Indicates if the question is required (default: False).
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the dropdown question.
        :param options: A list of dropdown options.
        :return: A dictionary containing the question type, text, and options for validation.
        """
        question_type = "dd"
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        options = options or self.DEFAULT_OPTIONS

        # Select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(QuestionSelectors.QuestionTypes.DROP_DOWN)

        # Wait for page to load
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Add dropdown options
        clear_and_send_keys_js = """document.querySelector('textarea[name="{name}"]').value=arguments[0];"""
        clear_and_send_keys_js2 = """document.querySelector('input[name="{name}"]').value=arguments[0];"""

        for i, option in enumerate(options):
            self.driver.execute_script(clear_and_send_keys_js.format(name=f"answers[{i}].localizedLabel[{language_code}]"), option)
            self.driver.execute_script(clear_and_send_keys_js2.format(name=f"answers[{i}].codedValue"), option)

            # Add new option field
            if i < len(options) - 1:
                self.driver.find_element(*QuestionSelectors.BUTTON_ADD_ANSWER).click()

        # Return question information
        return {
            "type": QuestionSelectors.QuestionTypes.DROP_DOWN,
            "text": question_text,
            "options": options,
        }

    def add_question_numeric_question(self,  language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None):
        """
        :param is_required: Indicates if the question is required (default: False).
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the numeric question.
        :param min_value: The minimum value for the numeric input.
        :param max_value: The maximum value for the numeric input.
        :param step_size: The step size for the numeric input.
        :return: A dictionary containing the question type and configuration for validation.
        """
        question_type = "nq"
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE

        # Select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(QuestionSelectors.QuestionTypes.NUMBER_INPUT)

        # Wait for page to load
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Set numeric values
        self.driver.find_element(By.CSS_SELECTOR, "#numberInputField input[name='answers[0].minValue']").send_keys(str(min_value))
        self.driver.find_element(By.CSS_SELECTOR, "#numberInputField input[name='answers[0].maxValue']").send_keys(str(max_value))
        self.driver.find_element(By.CSS_SELECTOR, "#numberInputField input[name='answers[0].stepsize']").send_keys(str(step_size))

        # Return question information
        return {
            "type": QuestionSelectors.QuestionTypes.NUMBER_INPUT,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
        }

    def add_question_freetext(self, language_code=None, is_required=False, question_text=None):
        """
        :param is_required: Indicates if the question is required (default: False).
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the freetext question.
        :return: A dictionary containing the question type and text for validation.
        """
        question_type = "ft"
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE

        # Select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(
            QuestionSelectors.QuestionTypes.FREE_TEXT
        )

        # Wait for the page to load
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill question text
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_QUESTION_TEXT_EDITABLE_DIV(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox
        if is_required:
            self.driver.find_element(*QuestionSelectors.CHECKBOX_IS_REQUIRED)
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        # Return question information
        return {
            "type": QuestionSelectors.QuestionTypes.FREE_TEXT,
            "text": question_text,
        }

class QuestionErrorHelper(QuestionHelper):

    # TODO [LJ] refactor
    def add_numeric_checkbox_with_failed_cases(self):
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located((By.ID, "questionTypeDropDown")))
        Select(self.driver.find_element(By.ID, "questionTypeDropDown")).select_by_visible_text("Nummerierte Checkboxen")
        WebDriverWait(self.driver, 30).until(
                lambda driver: driver.execute_script("return document.readyState") == "complete"
        )
        self.driver.find_element(By.XPATH,'/html/body/div/div/div[1]/div/div[2]/div/form/div[1]/div/div[4]/div/div/div[2]/div[2]').send_keys("Diese Frage zeigt einen sog. \'Slider\' vertikal an. Sie haben die Möglichkeit, durch Berühren des Schiebereglers einen Wert festzulegen, den Punkt zu verschieben und sich durch erneutes Antippen des Punkts dazu zu entscheiden, doch keine Antwort zu geben. Wie groß sind Sie (ca.)?")
        self.driver.find_element(By.ID, "isRequired1").click()
        self.driver.find_element(By.NAME, "answers[0].minValue").send_keys("0")
        self.driver.find_element(By.NAME, "answers[0].maxValue").send_keys("100")

        # # Attempt to save with invalid step size
        self.driver.find_element(By.NAME, "answers[0].stepsize").send_keys("30")
        self.driver.find_element(By.NAME, "answers[0].stepsize").send_keys(Keys.TAB)
        WebDriverWait(self.driver, 10).until(EC.presence_of_element_located((By.ID, "sliderErrors")))
        error_message_element = self.driver.execute_script("return document.getElementById('sliderErrors')")
        error_message_text = self.driver.execute_script("return arguments[0].innerText;", error_message_element).strip()
        assert "Der Abstand zwischen Minimum und Maximum ist nicht restlos durch die Schrittweite teilbar" in error_message_text

        # # # Attempt to save with step size greater than range
        self.driver.find_element(By.NAME, "answers[0].stepsize").clear()
        self.driver.find_element(By.NAME, "answers[0].stepsize").send_keys("210")
        self.save_question()
        WebDriverWait(self.driver, 30).until(
                lambda driver: driver.execute_script("return document.readyState") == "complete"
        )
        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located((By.ID, "sliderErrors")))
        time.sleep(2)
        error_message_element1 = self.driver.execute_script("return document.getElementById('sliderErrors')")
        error_message_text1 = self.driver.execute_script("return arguments[0].innerText;", error_message_element1)
        assert "Die Schrittweite der Frage war größer als der Abstand zwischen Minimum und Maximum" in error_message_text1


        # # Attempt to save with invalid step size format

        # # Correct the step size and clear min and max values
        self.driver.execute_script("document.getElementsByName('answers[0].stepsize')[0].value = '';")
        self.driver.execute_script("document.getElementsByName('answers[0].stepsize')[0].value = '1';")
        self.driver.execute_script("document.getElementsByName('answers[0].minValue')[0].value = '';")
        self.driver.execute_script("document.getElementsByName('answers[0].maxValue')[0].value = '';")
        self.save_question()
        WebDriverWait(self.driver, 30).until(
                        lambda driver: driver.execute_script("return document.readyState") == "complete"
        )
        # # Check for errors related to min and max values
        WebDriverWait(self.driver, 10).until(EC.presence_of_element_located((By.ID, "sliderErrors")))
        time.sleep(2)
        error_message_element = self.driver.execute_script("return document.getElementById('sliderErrors')")
        error_message_text = self.driver.execute_script("return arguments[0].innerText;", error_message_element)
        assert "Die Frage benötigt einen Minimalwert" in error_message_text
        assert "Die Frage benötigt einen Maximalwert" in error_message_text

        # # Correct the min and max values
        self.driver.execute_script("document.getElementsByName('answers[0].minValue')[0].value = '1';")
        self.driver.execute_script("document.getElementsByName('answers[0].maxValue')[0].value = '200';")
        self.driver.execute_script("document.getElementsByName('answers[0].stepsize')[0].value = '1';")

        time.sleep(1)

    def verify_number_of_questions(self, expected_count):
        """
        :param expected_count: The expected number of questions.
        :raises AssertionError: If the number of questions does not match the expected count.
        """
        rows = [
            row for row in self.driver.find_element(*QuestionSelectors.TABLE_QUESTION)
            .find_element(By.TAG_NAME, "tbody")
            .find_elements(By.TAG_NAME, "tr") if row.is_displayed()
        ]
        assert len(rows) == expected_count, f"Expected {expected_count} questions, but found {len(rows)}."
