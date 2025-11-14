import os
from datetime import datetime, timedelta
from enum import Enum

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils, DropdownMethod


class QuestionType(Enum):
    BODY_PART = "BODY_PART"
    BARCODE = "BARCODE"
    NUMBER_INPUT = "NUMBER_INPUT"
    INFO_TEXT = "INFO_TEXT"
    MULTIPLE_CHOICE = "MULTIPLE_CHOICE"
    SLIDER = "SLIDER"
    NUMBER_CHECKBOX = "NUMBER_CHECKBOX"
    NUMBER_CHECKBOX_TEXT = "NUMBER_CHECKBOX_TEXT"
    DROP_DOWN = "DROP_DOWN"
    FREE_TEXT = "FREE_TEXT"
    DATE = "DATE"
    IMAGE = "IMAGE"


class BodyPartSelectors:
    # SVG selectors for body parts
    BODY_PART_SVG_FRONT = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} #svg-FRONT")
    BODY_PART_SVG_BACK = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} #svg-BACK")
    # Individual body parts (front_or_back = 'front' or 'back') (left_or_right)
    HEAD = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-head")
    THROAT = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-throat")
    CHEST = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-chest")
    STOMACH = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-stomach")
    HIPS = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-hips")
    GENITALS = lambda id_selector, front_or_back: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-genitals")
    SHOULDER = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}Shoulder")
    ELBOW_JOINT = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}ElbowJoint")
    LOWER_ARM = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}LowerArm")
    HAND = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}Hand")
    THIGH = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}Thigh")
    KNEE = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}Knee")
    LOWER_LEG = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}LowerLeg")
    FOOT = lambda id_selector, front_or_back, left_or_right: (By.CSS_SELECTOR, f"#{id_selector} #bodyPart-{front_or_back}-{left_or_right}Foot")

class QuestionSelectors:
    NUMBER_INPUT_SECTION = (By.ID, "numberInput")
    MULTIPLE_CHOICE_AND_DROP_DOWN_SECTION = (By.ID, "multipleChoice")
    SLIDER_AND_NUMBER_CHECKBOX_SECTION = (By.ID, "slider")
    NUMBER_CHECKBOX_TEXT_SECTION = (By.ID, "sliderFreeText")
    DATE_SECTION = (By.ID, "date")
    IMAGE_SECTION = (By.ID, "image")
    BODY_PART_SECTION = (By.ID, "bodyPart")

    BUTTON_ADD_QUESTION = (By.ID, "addQuestion")
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_CANCEL = (By.ID, "cancelButton")
    BUTTON_ADD_ANSWER = (By.ID, "addAnswerButton")
    BUTTON_SELECT_ALL_BODY_PARTS = lambda FRONT_or_BACK: (By.ID, f"selectAllBodyPartsButton-{FRONT_or_BACK}") # "FRONT" or "BACK"
    BUTTON_DESELECT_ALL_BODY_PARTS = lambda FRONT_or_BACK: (By.ID, f"deleteAllBodyPartsButton-{FRONT_or_BACK}") # "FRONT" or "BACK"

    CHECKBOX_IS_REQUIRED = (By.ID, 'isRequired1')
    CHECKBOX_QUESTION_INITIAL_ACTIVATION = (By.ID, "isEnabled1")
    CHECKBOX_SYMBOL_ABOVE_SLIDER = (By.ID, "showIcons")
    CHECKBOX_DISPLAY_VALUE_ON_SLIDER = (By.ID, "showValueOnButton")
    CHECKBOX_VERTICAL = lambda index : (By.NAME, f"answers[{index}].vertical")
    CHECKBOX_FREE_TEXT = lambda id_selector, index: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[{index}].isOther']")
    CHECKBOX_ANSWER_INITIAL_ACTIVATION = lambda id_selector, index: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[{index}].isEnabled']")

    DROPDOWN_ADD_LANGUAGE = (By.CSS_SELECTOR, "div#languageDropdown.dropdown")
    DROPDOWN_TYPE_OF_IDENTIFICATION = (By.ID, "codedValueType")
    DROPDOWN_QUESTION_TYPE = (By.ID, "questionTypeDropDown")
    DROPDOWN_IMAGE_TYPE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} select[name='imageType']")

    ERROR_SLIDER_CONTAINER = (By.ID, "sliderErrors")
    ERROR_QUESTION_TEXT = lambda language_code: (By.XPATH, f"//textarea[@id='localizedQuestionText{language_code}']/following-sibling::div[@style='color: red']")
    ERROR_MIN_ANSWERS = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='minNumberAnswers']/following-sibling::div[@style='color: red']")
    ERROR_MAX_ANSWERS = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='maxNumberAnswers']/following-sibling::div[@style='color: red']")
    ERROR_IDENT_CODE = lambda index: (By.XPATH, f"//input[@id='answers{index}.codedValue']/following-sibling::div[@style='color: red']")
    ERROR_MIN_VALUE = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='answers0.minValue']/following-sibling::div[@style='color: red']")
    ERROR_MAX_VALUE = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='answers0.maxValue']/following-sibling::div[@style='color: red']")
    ERROR_STEP_SIZE = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='answers0.stepsize']/following-sibling::div[@style='color: red']")
    ERROR_START_DATE = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='answers0.startDate']/following-sibling::div[@style='color: red']")
    ERROR_END_DATE = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//input[@id='answers0.endDate']/following-sibling::div[@style='color: red']")
    ERROR_FILE_PATH = lambda id_selector: (By.XPATH, f"//div[@id='{id_selector}']//div[@style='color: red']")
    ERROR_TEXTAREA_ANSWER = lambda id_selector, index, language_code: (By.XPATH, f"//div[@id='{id_selector}']//textarea[@name='answers[{index}].localizedLabel[{language_code}]']/following-sibling::div[@style='color: red']")
    ERROR_SLIDER_FIELD = (By.ID, "errorSlider")

    INPUT_MIN_NUMBER_ANSWERS = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[id='minNumberAnswers']")
    INPUT_MAX_NUMBER_ANSWERS = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[id='maxNumberAnswers']")
    INPUT_MIN_VALUE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[0].minValue']")
    INPUT_MAX_VALUE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[0].maxValue']")
    INPUT_STEP_SIZE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[0].stepsize']")
    INPUT_START_DATE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[0].startDate']")
    INPUT_END_DATE = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[0].endDate']")
    INPUT_IMAGE_UPLOAD = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} input[type='file'][name='answers[0].imageFile']")
    INPUT_IDENTIFICATION = lambda id_selector, index: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[{index}].codedValue']")
    INPUT_SCORE = lambda id_selector, index: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[{index}].value']")
    INPUT_FREETEXT_LABEL = lambda id_selector, index, language_code: (By.CSS_SELECTOR, f"#{id_selector} input[name='answers[{index}].localizedFreetextLabel[{language_code}]']")

    INPUT_WYSIWYG_QUESTION_TEXT = lambda language_code: (By.XPATH, f'//*[@id="localizedQuestionTextCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_WYSIWYG_SLIDER_MIN = lambda language_code: (By.XPATH, f'//*[@id="localizedMinimumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_WYSIWYG_SLIDER_MAX = lambda language_code: (By.XPATH, f'//*[@id="localizedMaximumTextSliderCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MIN = lambda language_code: (By.XPATH, f'//*[@id="localizedMinimumTextNumberCheckboxCollapsableText_{language_code}"]/div/div[2]/div[2]')
    INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MAX = lambda language_code: (By.XPATH, f'//*[@id="localizedMaximumTextNumberCheckboxCollapsableText_{language_code}"]/div/div[2]/div[2]')

    TEXTAREA_ANSWER_TEXT = lambda id_selector, index, language_code: (By.CSS_SELECTOR, f"#{id_selector} textarea[name='answers[{index}].localizedLabel[{language_code}]']")

    IMAGE_FILE_CONTAINER = lambda id_selector: (By.CSS_SELECTOR, f"#{id_selector} .form-group.imageFile")
    MULTIPLE_CHOICE_ANSWER_PANELS = (By.CLASS_NAME, "multipleChoiceAnswerPanel")
    DELETE_BUTTON_WITHIN_PANEL = (By.XPATH, ".//button[@id='deleteAnswerButton']")
    TABLE_LAST_ROW = (By.XPATH, "//tbody/tr[last()]")
    TABLE_ROWS = (By.CSS_SELECTOR, "tbody > tr:not(#emptyRow)")
    TABLE_QUESTIONS = (By.ID, "questionTable")
    ACTION_BUTTONS = (By.CSS_SELECTOR, "td.actionColumn > div.d-none.d-xl-block > div.btn-group > a.link")
    GRIP_SELECTOR = lambda item_id: (By.ID, f"grip-{item_id}")

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
    DEFAULT_IMAGE_UPLOAD_PATH = "test_images"
    DEFAULT_IMAGE_FILE_NAME = "test_upload_image.png"
    DEFAULT_IMAGE_TYPE = 'FRONT'

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper

    def save_question(self):
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        return self.get_last_added_question_id()

    def cancel_question_editing(self):
        self.utils.click_element(QuestionSelectors.BUTTON_CANCEL)

    def get_question_type(self, question_id):
        """
        Retrieves the type of a question by opening its edit page and checking the selected option
        in the question type dropdown.

        :param question_id: The ID of the question to retrieve the type for.
        :return: The QuestionType of the question.
        """
        try:
            # Navigate to the question's edit page
            self.navigation_helper.open_question(question_id)

            # Wait for the question type dropdown to be visible
            question_type_dropdown = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE)
            )

            # Retrieve the selected option
            selected_option = question_type_dropdown.find_element(By.CSS_SELECTOR, "option[selected]")
            value = selected_option.get_attribute('value')

            self.cancel_question_editing()

            # Convert the value to a QuestionType
            return QuestionType(value)
        except TimeoutException:
            raise Exception(f"Timeout while retrieving question type for question ID {question_id}.")
        except Exception as e:
            raise Exception(f"Error while retrieving question type for question ID {question_id}: {e}")

    def add_question_by_type_default_value(self, question_type: QuestionType):
        if question_type == QuestionType.INFO_TEXT:
            return self.add_question_info_text()
        elif question_type == QuestionType.MULTIPLE_CHOICE:
            return self.add_question_multiple_choice()
        elif question_type == QuestionType.SLIDER:
            return self.add_question_slider_question()
        elif question_type == QuestionType.NUMBER_CHECKBOX:
            return self.add_question_number_checkbox()
        elif question_type == QuestionType.NUMBER_CHECKBOX_TEXT:
            return self.add_question_number_checkbox_text()
        elif question_type == QuestionType.DATE:
            return self.add_question_date_question()
        elif question_type == QuestionType.DROP_DOWN:
            return self.add_question_dropdown()
        elif question_type == QuestionType.NUMBER_INPUT:
            return self.add_question_number_question()
        elif question_type == QuestionType.FREE_TEXT:
            return self.add_question_freetext()
        elif question_type == QuestionType.IMAGE:
            return self.add_question_image()
        elif question_type == QuestionType.BODY_PART:
            return self.add_question_body_part()
        elif question_type == QuestionType.BARCODE:
            return self.add_question_barcode()
        else:
            raise ValueError(f"Unknown QuestionType: {question_type}")

    def add_question_info_text(self, language_code=None, is_required=False, question_text=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the info text question.
        :return: A dictionary containing the question type and text for validation.
        """
        question_text = self.initialize_question(QuestionType.INFO_TEXT, language_code, is_required, question_text)

        return {"type": QuestionType.INFO_TEXT, "text": question_text}

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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        options = options or self.DEFAULT_OPTIONS
        min_answers = min_answers or self.DEFAULT_MC_MIN_VALUE
        max_answers = max_answers or self.DEFAULT_MC_MAX_VALUE
        id_selector = self.get_selector_for(QuestionType.MULTIPLE_CHOICE)

        question_text = self.initialize_question(QuestionType.MULTIPLE_CHOICE, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.MULTIPLE_CHOICE_AND_DROP_DOWN_SECTION)

        # Set min and max answers
        self.set_min_max_answers(id_selector, min_answers, max_answers)
        # Add options
        for i, option in enumerate(options):
            textarea_element = self.driver.find_element(*QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, i, language_code))
            self.driver.execute_script("arguments[0].value=arguments[1];", textarea_element, option)
            input_ident_element = self.driver.find_element(*QuestionSelectors.INPUT_IDENTIFICATION(id_selector, i))
            self.driver.execute_script("arguments[0].value=arguments[1];", input_ident_element, option)

            # Add new answer field except for the last option
            if i < len(options) - 1:
                add_button = self.driver.find_element(*QuestionSelectors.BUTTON_ADD_ANSWER)
                self.driver.execute_script("arguments[0].click();", add_button)

        return {
            "type": QuestionType.MULTIPLE_CHOICE,
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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE
        labels = labels or self.DEFAULT_LABELS

        question_text = self.initialize_question(QuestionType.SLIDER, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.SLIDER_AND_NUMBER_CHECKBOX_SECTION)

        # Set slider configuration
        self.set_min_max_step_inputs(min_value, max_value, step_size, QuestionType.SLIDER)
        # Set labels for the slider
        slider_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_SLIDER_MIN(language_code))
        self.utils.fill_editable_div(slider_text_min_div, labels[0])
        slider_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_SLIDER_MAX(language_code))
        self.utils.fill_editable_div(slider_text_max_div, labels[1])

        return {
            "type": QuestionType.SLIDER,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "labels": labels,
        }

    def add_question_number_checkbox(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None, labels=None):
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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value if min_value is not None else self.DEFAULT_MIN_VALUE
        max_value = max_value if max_value is not None else self.DEFAULT_MAX_VALUE
        step_size = step_size if step_size is not None else self.DEFAULT_STEP_SIZE
        labels = labels or self.DEFAULT_LABELS

        question_text = self.initialize_question(QuestionType.NUMBER_CHECKBOX, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.NUMBER_CHECKBOX_TEXT_SECTION)

        # Set numeric values
        self.set_min_max_step_inputs(min_value, max_value, step_size, QuestionType.NUMBER_CHECKBOX)
        # Set labels
        nc_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_SLIDER_MIN(language_code))
        self.utils.fill_editable_div(nc_text_min_div, labels[0])
        nc_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_SLIDER_MAX(language_code))
        self.utils.fill_editable_div(nc_text_max_div, labels[1])

        return {
            "type": QuestionType.NUMBER_CHECKBOX,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "labels": labels,
        }

    def add_question_number_checkbox_text(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None, freetext_label=None, labels=None):
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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE
        freetext_label = freetext_label or self.DEFAULT_FREE_TEXT_LABEL
        labels = labels or self.DEFAULT_LABELS
        id_selector = self.get_selector_for(QuestionType.NUMBER_CHECKBOX_TEXT)

        question_text = self.initialize_question(QuestionType.NUMBER_CHECKBOX_TEXT, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.NUMBER_CHECKBOX_TEXT_SECTION)

        # Set numeric values
        self.set_min_max_step_inputs(min_value, max_value, step_size, QuestionType.NUMBER_CHECKBOX_TEXT)
        # Set labels
        ncf_text_min_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MIN(language_code))
        self.utils.fill_editable_div(ncf_text_min_div, labels[0])
        ncf_text_max_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MAX(language_code))
        self.utils.fill_editable_div(ncf_text_max_div, labels[1])
        # Set freetext label
        self.driver.find_element(*QuestionSelectors.INPUT_FREETEXT_LABEL(id_selector, 0, language_code)).send_keys(freetext_label)

        return {
            "type": QuestionType.NUMBER_CHECKBOX_TEXT,
            "text": question_text,
            "min_value": min_value,
            "max_value": max_value,
            "step_size": step_size,
            "freetext_label": freetext_label,
        }

    def add_question_date_question(self, language_code=None, is_required=False, question_text=None, start_date=None, end_date=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the date question.
        :param start_date: The start date for the date range (default: current date).
        :param end_date: The end date for the date range (default: 7 days from the current date).
        :return: A dictionary containing the question type and text for validation.
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        start_date = start_date or datetime.now().strftime("%m/%d/%Y")
        end_date = end_date or ((datetime.now() + timedelta(days=7)).strftime("%m/%d/%Y"))
        id_selector = self.get_selector_for(QuestionType.DATE)

        question_text = self.initialize_question(QuestionType.DATE, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.DATE_SECTION)

        # Find start and end date inputs
        start_date_input = self.driver.find_element(*QuestionSelectors.INPUT_START_DATE(id_selector))
        end_date_input = self.driver.find_element(*QuestionSelectors.INPUT_END_DATE(id_selector))
        # Clear and input start/end date
        start_date_input.clear()
        start_date_input.send_keys(start_date)
        end_date_input.clear()
        end_date_input.send_keys(end_date)

        return {
            "type": QuestionType.DATE,
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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        options = options or self.DEFAULT_OPTIONS
        id_selector = self.get_selector_for(QuestionType.DROP_DOWN)

        question_text = self.initialize_question(QuestionType.DROP_DOWN, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.MULTIPLE_CHOICE_AND_DROP_DOWN_SECTION)

        # Add options
        for i, option in enumerate(options):
            textarea_element = self.driver.find_element(*QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, i, language_code))
            self.driver.execute_script("arguments[0].value=arguments[1];", textarea_element, option)
            input_ident_element = self.driver.find_element(*QuestionSelectors.INPUT_IDENTIFICATION(id_selector, i))
            self.driver.execute_script("arguments[0].value=arguments[1];", input_ident_element, option)

            # Add new option field
            if i < len(options) - 1:
                add_button = self.driver.find_element(*QuestionSelectors.BUTTON_ADD_ANSWER)
                self.driver.execute_script("arguments[0].click();", add_button)

        return {
            "type": QuestionType.DROP_DOWN,
            "text": question_text,
            "options": options,
        }

    def add_question_number_question(self, language_code=None, is_required=False, question_text=None, min_value=None, max_value=None, step_size=None):
        """
        :param is_required: Indicates if the question is required (default: False).
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param question_text: The text content for the numeric question.
        :param min_value: The minimum value for the numeric input.
        :param max_value: The maximum value for the numeric input.
        :param step_size: The step size for the numeric input.
        :return: A dictionary containing the question type and configuration for validation.
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        min_value = min_value or self.DEFAULT_MIN_VALUE
        max_value = max_value or self.DEFAULT_MAX_VALUE
        step_size = step_size or self.DEFAULT_STEP_SIZE

        question_text = self.initialize_question(QuestionType.NUMBER_INPUT, language_code, is_required, question_text)

        self.ensure_section_visible(QuestionSelectors.NUMBER_INPUT_SECTION)

        # Set numeric values
        self.set_min_max_step_inputs(min_value, max_value, step_size, QuestionType.NUMBER_INPUT)

        return {
            "type": QuestionType.NUMBER_INPUT,
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
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE

        question_text = self.initialize_question(QuestionType.FREE_TEXT, language_code, is_required, question_text)

        return {
            "type": QuestionType.FREE_TEXT,
            "text": question_text,
        }

    def add_question_image(self, language_code=None, is_required=False, question_text=None, upload_path=None, file_name=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the image question.
        :param upload_path: The relative path to the image directory (default: self.DEFAULT_IMAGE_UPLOAD_PATH).
        :param file_name: The name of the image file to upload (default: self.DEFAULT_IMAGE_FILE_NAME).
        :return: A dictionary containing the question type and text for validation.
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        upload_path = upload_path or self.DEFAULT_IMAGE_UPLOAD_PATH
        file_name = file_name or self.DEFAULT_IMAGE_FILE_NAME
        id_selector = self.get_selector_for(QuestionType.IMAGE)

        question_text = self.initialize_question(QuestionType.IMAGE, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.IMAGE_SECTION)

        image_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.INPUT_IMAGE_UPLOAD(id_selector)))

        image_path = os.path.join(os.path.dirname(__file__), upload_path, file_name)
        assert os.path.exists(image_path), f"Test image not found at path: {image_path}"
        image_input.send_keys(image_path)

        return {
            "type": QuestionType.IMAGE,
            "text": question_text,
        }

    def add_question_body_part(self, language_code=None, is_required=False, question_text=None, image_type=None, body_part_selectors=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the body part question.
        :param image_type: The type of image to display (default: self.DEFAULT_IMAGE_TYPE).
        :param body_part_selectors: A set of selectors for the body parts to be selected (default: HEAD(id_selector, "front")).
        :return: A dictionary containing the question type, text, and selected body parts for validation.
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE
        image_type = image_type or self.DEFAULT_IMAGE_TYPE
        id_selector = self.get_selector_for(QuestionType.BODY_PART)
        body_part_selectors = body_part_selectors or {BodyPartSelectors.HEAD(id_selector, "front")}

        question_text = self.initialize_question(QuestionType.BODY_PART, language_code, is_required, question_text)

        # Ensure the number input section is expanded
        self.ensure_section_visible(QuestionSelectors.BODY_PART_SECTION)

        self.utils.scroll_to_bottom()
        self.utils.select_dropdown(QuestionSelectors.DROPDOWN_IMAGE_TYPE(id_selector), image_type, DropdownMethod.VALUE)
        self.set_min_max_answers(id_selector, 0, 1)
        selected_body_parts = self.select_body_parts(body_part_selectors, deselect_first=True)

        return {
            "type": QuestionType.BODY_PART,
            "text": question_text,
            "selected_body_parts": selected_body_parts,
        }

    def add_question_barcode(self, language_code=None, is_required=False, question_text=None):
        """
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the body part question.
        :return: A dictionary containing the question type, text.
        """
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE

        question_text = self.initialize_question(QuestionType.BARCODE, language_code, is_required, question_text)

        return {
            "type": QuestionType.BARCODE,
            "text": question_text
        }

    def initialize_question(self, question_type, language_code=None, is_required=False, question_text=None):
        """
        :param question_type: The type of the question (e.g., QuestionType.MULTIPLE_CHOICE).
        :param language_code: The language code for localized labels (default: self.DEFAULT_LANGUAGE_CODE).
        :param is_required: Indicates if the question is required (default: False).
        :param question_text: The text content for the question.
        :return: A dictionary containing common question details.
        """
        question_text = question_text or f"{self.DEFAULT_QUESTION_TEXT} - {question_type.value}"
        language_code = language_code or self.DEFAULT_LANGUAGE_CODE

        # Wait for the dropdown to be visible and select the question type
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
            QuestionSelectors.DROPDOWN_QUESTION_TYPE))


        self.utils.select_dropdown(QuestionSelectors.DROPDOWN_QUESTION_TYPE, question_type.value, DropdownMethod.VALUE)

        # Wait until the page is fully loaded
        WebDriverWait(self.driver, 30).until(lambda d: d.execute_script("return document.readyState") == "complete")

        # Fill the editable div for the question text (info text uses the same text field)
        question_div = self.driver.find_element(*QuestionSelectors.INPUT_WYSIWYG_QUESTION_TEXT(language_code))
        self.utils.fill_editable_div(question_div, question_text)

        # Set required checkbox if needed
        if is_required:
            self.utils.toggle_checkbox(QuestionSelectors.CHECKBOX_IS_REQUIRED)

        return question_text

    def ensure_section_visible(self, section_selector):
        """
        :param section_selector: The selector for the section container.
        """
        # Wait for the page to fully load
        WebDriverWait(self.driver, 30).until(lambda driver: driver.execute_script("return document.readyState") == "complete")

        # Locate the section container
        section = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            section_selector))

        # Check if the section is hidden
        is_hidden = self.driver.execute_script(
            "return arguments[0].querySelector('.form-group').style.display === 'none';", section
        )

        if is_hidden:
            # Click the legend to expand the section
            legend = section.find_element(By.TAG_NAME, "legend")
            legend.click()

            # Wait until the section becomes visible
            WebDriverWait(self.driver, 10).until(
                lambda d: self.driver.execute_script(
                    "return arguments[0].querySelector('.form-group').style.display !== 'none';", section
                )
            )

    @staticmethod
    def get_selector_for(question_type: QuestionType):
        if question_type == QuestionType.BODY_PART:
            return QuestionSelectors.BODY_PART_SECTION[1]
        elif question_type == QuestionType.DATE:
            return QuestionSelectors.DATE_SECTION[1]
        elif question_type == QuestionType.IMAGE:
            return QuestionSelectors.IMAGE_SECTION[1]
        elif question_type == QuestionType.MULTIPLE_CHOICE or question_type == QuestionType.DROP_DOWN:
            return QuestionSelectors.MULTIPLE_CHOICE_AND_DROP_DOWN_SECTION[1]
        elif question_type == QuestionType.NUMBER_INPUT:
            return QuestionSelectors.NUMBER_INPUT_SECTION[1]
        elif question_type == QuestionType.SLIDER or question_type == QuestionType.NUMBER_CHECKBOX:
            return QuestionSelectors.SLIDER_AND_NUMBER_CHECKBOX_SECTION[1]
        elif question_type == QuestionType.NUMBER_CHECKBOX_TEXT:
            return QuestionSelectors.NUMBER_CHECKBOX_TEXT_SECTION[1]

    def get_last_added_question_id(self):
        """
        :return: ID of the question as a string.
        """
        # Wait until the table rows are loaded
        WebDriverWait(self.driver, 30).until(EC.presence_of_element_located(
            QuestionSelectors.TABLE_LAST_ROW))

        # Find the last row in the table
        last_row = self.driver.find_element(*QuestionSelectors.TABLE_LAST_ROW)

        # Extract the ID from the `id` attribute of the last row
        question_id = last_row.get_attribute("id")

        # Ensure the ID is found
        if not question_id:
            raise Exception("The ID of the last question could not be found.")

        return question_id

    def clear_min_max_step_inputs(self, question_type : QuestionType):
        id_selector = self.get_selector_for(question_type)

        min_value = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MIN_VALUE(id_selector)))

        max_value = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MAX_VALUE(id_selector)))

        step_size = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_STEP_SIZE(id_selector)))

        min_value.clear()
        max_value.clear()
        step_size.clear()

    def set_min_max_step_inputs(self, min_value, max_value, step_size, question_type: QuestionType):
        id_selector = self.get_selector_for(question_type)

        self.clear_min_max_step_inputs(question_type)

        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MIN_VALUE(id_selector))).send_keys(min_value)

        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MAX_VALUE(id_selector))).send_keys(max_value)

        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_STEP_SIZE(id_selector))).send_keys(step_size)

    def set_min_max_answers(self, id_selector, min_value, max_value):
        input_min_answers = WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS(id_selector)))

        input_max_answers = WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
            QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS(id_selector)))

        input_min_answers.clear()
        input_min_answers.send_keys(min_value)
        input_max_answers.clear()
        input_max_answers.send_keys(max_value)

    def select_body_parts(self, body_part_selectors, deselect_first=False):
        """
        :param body_part_selectors: A list of selectors for the body parts to be selected.
        :param deselect_first: Indicates if all body parts should be deselected before selection (default: False).
        :return: A list of successfully selected body part selectors.
        """
        if deselect_first:
            self.deselect_all_body_parts()

        successfully_selected = []

        for body_part_selector in body_part_selectors:
            try:
                # Wait for the page to fully load
                WebDriverWait(self.driver, 30).until(
                    lambda driver: driver.execute_script("return document.readyState") == "complete")

                WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                    body_part_selector))

                # Click the element to select
                self.utils.click_element(body_part_selector)

                # Verify if the body part is selected
                body_part_element = self.driver.find_element(*body_part_selector)
                if "shape-selected" in body_part_element.get_attribute("class"):
                    successfully_selected.append(body_part_selector)
            except Exception as e:
                print(f"Failed to select body part with selector {body_part_selector}: {e}")

        return successfully_selected

    def deselect_all_body_parts(self):
        id_selector = self.get_selector_for(QuestionType.BODY_PART)

        image_type_dropdown = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.DROPDOWN_IMAGE_TYPE(id_selector)))

        selected_image_type = Select(image_type_dropdown).first_selected_option.get_attribute("value")

        # Perform deselection based on the selected image type
        if selected_image_type == "FRONT":
            parts_deselect = QuestionSelectors.BUTTON_DESELECT_ALL_BODY_PARTS("FRONT")
            self.utils.click_element(parts_deselect)
        elif selected_image_type == "BACK":
            parts_deselect = QuestionSelectors.BUTTON_DESELECT_ALL_BODY_PARTS("BACK")
            self.utils.click_element(parts_deselect)
        elif selected_image_type == "FRONT_BACK":
            parts_deselect_front = QuestionSelectors.BUTTON_DESELECT_ALL_BODY_PARTS("FRONT")
            parts_deselect_back = QuestionSelectors.BUTTON_DESELECT_ALL_BODY_PARTS("BACK")
            self.utils.click_element(parts_deselect_front)
            self.utils.click_element(parts_deselect_back)
        else:
            raise ValueError(f"Unexpected image type: {selected_image_type}")


class QuestionAssertHelper(QuestionHelper):

    def assert_question_fill_page(self):
        language_code = self.DEFAULT_LANGUAGE_CODE
        id_selector = self.get_selector_for(QuestionType.MULTIPLE_CHOICE)
        try:
            # Select one of the question types
            WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
                QuestionSelectors.DROPDOWN_QUESTION_TYPE))
            self.utils.select_dropdown(QuestionSelectors.DROPDOWN_QUESTION_TYPE, QuestionType.MULTIPLE_CHOICE.value, DropdownMethod.VALUE)

            # Validate dropdown to add language
            language_dropdown = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.DROPDOWN_ADD_LANGUAGE))
            assert language_dropdown.is_displayed(), "Dropdown to add language is not displayed."

            # Validate select for question type
            question_type_select = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.DROPDOWN_QUESTION_TYPE))
            assert question_type_select.is_displayed(), "Dropdown for question type is not displayed."

            # Validate WYSIWYG editor for the question text
            wysiwyg_question_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.INPUT_WYSIWYG_QUESTION_TEXT(language_code)))
            assert wysiwyg_question_text.is_displayed(), "WYSIWYG editor for question text is not displayed."

            # Validate checkboxes for mandatory and initial activation
            mandatory_checkbox = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.CHECKBOX_IS_REQUIRED))
            assert mandatory_checkbox.is_displayed(), "Checkbox to make question mandatory is not displayed."

            initial_activation_checkbox = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.CHECKBOX_ANSWER_INITIAL_ACTIVATION(id_selector, 0)))
            assert initial_activation_checkbox.is_displayed(), "Checkbox to activate question initially is not displayed."

            self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
            # Error: Localized question text missing
            error_message_element = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                QuestionSelectors.ERROR_QUESTION_TEXT(self.DEFAULT_LANGUAGE_CODE)))
            error_message = error_message_element.text.strip()

            expected_error = "Die Frage benötigt einen lokalisierten Fragetext"
            assert error_message == expected_error, f"Unexpected error message: '{error_message}'"

            self.cancel_question_editing()
        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the question fill page.")
        except AssertionError as e:
            raise e

    def assert_question_by_type(self, question_type: QuestionType):
        WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(QuestionSelectors.DROPDOWN_QUESTION_TYPE))
        Select(self.driver.find_element(*QuestionSelectors.DROPDOWN_QUESTION_TYPE)).select_by_value(question_type.value)
        # Wait for the page to fully load
        WebDriverWait(self.driver, 30).until(lambda driver: driver.execute_script("return document.readyState") == "complete")

        if question_type == QuestionType.INFO_TEXT:
            self.assert_fields_create_info_text_question()
        elif question_type == QuestionType.MULTIPLE_CHOICE:
            self.assert_fields_create_multiple_choice_question()
            self.assert_validation_errors_multiple_choice()
        elif question_type == QuestionType.SLIDER:
            self.assert_fields_create_slider_question()
            self.assert_validation_errors_sq_nc(question_type = QuestionType.SLIDER)
        elif question_type == QuestionType.NUMBER_CHECKBOX:
            self.assert_fields_numeric_checkbox_question()
            self.assert_validation_errors_sq_nc(question_type = QuestionType.NUMBER_CHECKBOX)
        elif question_type == QuestionType.NUMBER_CHECKBOX_TEXT:
            self.assert_fields_numeric_checkbox_freetext_question()
            self.assert_validation_errors_ncf()
        elif question_type == QuestionType.DATE:
            self.assert_fields_create_date_question()
            self.assert_validation_errors_dq()
        elif question_type == QuestionType.DROP_DOWN:
            self.assert_fields_create_dropdown_question()
            self.assert_validation_errors_dd()
        elif question_type == QuestionType.NUMBER_INPUT:
            self.assert_fields_create_number_question()
            self.assert_nq_validation_errors_de()
        elif question_type == QuestionType.FREE_TEXT:
            self.assert_freetext_question()
        elif question_type == QuestionType.IMAGE:
            self.assert_fields_create_image_question()
            self.assert_validation_errors_iq()
        elif question_type == QuestionType.BODY_PART:
            self.assert_fields_create_body_part_question()
            self.assert_validation_errors_bp()
        elif question_type == QuestionType.BARCODE:
            self.assert_fields_create_barcode_question()
        else:
            raise ValueError(f"Unknown QuestionType: {question_type}")

        question_info = self.add_question_by_type_default_value(question_type)
        question_id = self.save_question()
        question_info['id'] = question_id
        return question_info

    def assert_fields_create_multiple_choice_question(self):
        self.assert_mc_dd_common_validations(QuestionType.MULTIPLE_CHOICE)

    def assert_fields_create_slider_question(self):
        self.assert_sq_nc_common_validations(QuestionType.SLIDER)

        checkbox_vertical = self.driver.find_element(*QuestionSelectors.CHECKBOX_VERTICAL(0))
        assert not checkbox_vertical.is_enabled(), f"Checkbox for 'vertical' should be disabled but is enabled."

        symbols_checkbox = self.driver.find_element(*QuestionSelectors.CHECKBOX_SYMBOL_ABOVE_SLIDER)
        value_checkbox = self.driver.find_element(*QuestionSelectors.CHECKBOX_DISPLAY_VALUE_ON_SLIDER)
        assert symbols_checkbox.is_displayed(), "Checkbox for enable/disable symbol above slider is not displayed."
        assert value_checkbox.is_displayed(), "Checkbox for enable/disable value on slider is not displayed."

    def assert_freetext_question(self):
        try:
            allowed_ids = {
                QuestionSelectors.DROPDOWN_QUESTION_TYPE[1],
                QuestionSelectors.CHECKBOX_IS_REQUIRED[1],
                QuestionSelectors.CHECKBOX_QUESTION_INITIAL_ACTIVATION[1],
                QuestionSelectors.BUTTON_SAVE[1],
            }
            self.assert_question_inputs(allowed_ids)
        except AssertionError as e:
            raise e

    def assert_fields_create_info_text_question(self):
        try:
            allowed_ids = {
                QuestionSelectors.DROPDOWN_QUESTION_TYPE[1],
                QuestionSelectors.CHECKBOX_IS_REQUIRED[1],
                QuestionSelectors.CHECKBOX_QUESTION_INITIAL_ACTIVATION[1],
                QuestionSelectors.BUTTON_SAVE[1],
            }
            self.assert_question_inputs(allowed_ids)
        except AssertionError as e:
            raise e

    def assert_fields_create_barcode_question(self):
        try:
            allowed_ids = {
                QuestionSelectors.DROPDOWN_QUESTION_TYPE[1],
                QuestionSelectors.CHECKBOX_IS_REQUIRED[1],
                QuestionSelectors.CHECKBOX_QUESTION_INITIAL_ACTIVATION[1],
                QuestionSelectors.BUTTON_SAVE[1],
            }
            self.assert_question_inputs(allowed_ids)
        except AssertionError as e:
            raise e

    def assert_fields_numeric_checkbox_question(self):
        self.assert_sq_nc_common_validations(QuestionType.NUMBER_CHECKBOX)
        checkbox_vertical = self.driver.find_element(*QuestionSelectors.CHECKBOX_VERTICAL(0))
        assert checkbox_vertical.is_enabled(), f"Checkbox for 'vertical' should be enabled but is disabled."

    def assert_fields_numeric_checkbox_freetext_question(self):
        try:
            language_code = self.DEFAULT_LANGUAGE_CODE
            id_selector = self.get_selector_for(QuestionType.NUMBER_CHECKBOX_TEXT)

            min_value = self.driver.find_element(*QuestionSelectors.INPUT_MIN_VALUE(id_selector))
            max_value = self.driver.find_element(*QuestionSelectors.INPUT_MAX_VALUE(id_selector))
            step_size = self.driver.find_element(*QuestionSelectors.INPUT_STEP_SIZE(id_selector))
            assert min_value.is_displayed(), "Input for minimum value is not displayed."
            assert max_value.is_displayed(), "Input for maximum value is not displayed."
            assert step_size.is_displayed(), "Input for step size is not displayed."

            freetext_label = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.INPUT_FREETEXT_LABEL(id_selector, 0, language_code)))
            assert freetext_label.is_displayed(), "Freetext input field is not displayed."

            wysiwyg_min_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MIN(language_code)))
            assert wysiwyg_min_text.is_displayed(), "WYSIWYG editor for text at minimum position is not displayed."

            wysiwyg_max_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                QuestionSelectors.INPUT_WYSIWYG_NUMERIC_CHECKBOX_FREETEXT_MAX(language_code)))
            assert wysiwyg_max_text.is_displayed(), "WYSIWYG editor for text at maximum position is not displayed."
        except AssertionError as e:
            raise e

    def assert_fields_create_dropdown_question(self):
        id_selector= self.get_selector_for(QuestionType.DROP_DOWN)

        self.assert_mc_dd_common_validations(QuestionType.DROP_DOWN)

        # If min and max should be readonly
        input_min_answers = self.driver.find_element(*QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS(id_selector))
        input_max_answers = self.driver.find_element(*QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS(id_selector))
        assert input_min_answers.get_attribute("readonly"), "Min answers input is not readonly."
        assert input_max_answers.get_attribute("readonly"), "Max answers input is not readonly."
        assert input_min_answers.get_attribute("value") == "1", "Min answers input does not have the expected value '1'."
        assert input_max_answers.get_attribute("value") == "1", "Max answers input does not have the expected value '1'."

    def assert_fields_create_number_question(self):
        question_type = QuestionType.NUMBER_INPUT
        id_selector = self.get_selector_for(question_type)

        min_value = self.driver.find_element(*QuestionSelectors.INPUT_MIN_VALUE(id_selector))
        max_value = self.driver.find_element(*QuestionSelectors.INPUT_MAX_VALUE(id_selector))
        step_size = self.driver.find_element(*QuestionSelectors.INPUT_STEP_SIZE(id_selector))
        assert min_value.is_displayed(), "Input for minimum value is not displayed."
        assert max_value.is_displayed(), "Input for maximum value is not displayed."
        assert step_size.is_displayed(), "Input for step size is not displayed."

    def assert_fields_create_date_question(self):
        id_selector = self.get_selector_for(QuestionType.DATE)

        start_date = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.INPUT_START_DATE(id_selector)))
        assert start_date.is_displayed(), "Start date input field is not displayed."

        end_date = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.INPUT_END_DATE(id_selector)))
        assert end_date.is_displayed(), "End date input field is not displayed."

    def assert_fields_create_image_question(self):
        id_selector = self.get_selector_for(QuestionType.IMAGE)

        # Locate the container for the image upload
        container_locator = QuestionSelectors.IMAGE_FILE_CONTAINER(id_selector)
        container = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            container_locator))

        # Check if the container is displayed
        assert container.is_displayed(), "Image upload container is not visible."

        image_input = container.find_element(*QuestionSelectors.INPUT_IMAGE_UPLOAD(id_selector))

        # Validate attributes or further behaviors if necessary
        accepted_file_types = image_input.get_attribute("accept")
        assert accepted_file_types == ".png,.jpeg,.jpg", f"Unexpected accepted file types: {accepted_file_types}"

    def assert_fields_create_body_part_question(self):
        id_selector = self.get_selector_for(QuestionType.BODY_PART)

        # Validate Select for the type of image
        image_type_dropdown = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.DROPDOWN_IMAGE_TYPE(id_selector)))
        assert image_type_dropdown.is_displayed(), "Dropdown for selecting image type is not displayed."

        # Test: Changing the image type updates the graphic
        self.utils.select_dropdown(QuestionSelectors.DROPDOWN_IMAGE_TYPE(id_selector), "BACK", DropdownMethod.VALUE)
        back_svg = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            BodyPartSelectors.BODY_PART_SVG_BACK(id_selector)))
        assert back_svg.is_displayed(), "SVG for 'BACK' view is not displayed."

        self.utils.select_dropdown(QuestionSelectors.DROPDOWN_IMAGE_TYPE(id_selector), "FRONT", DropdownMethod.VALUE)
        front_svg = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            BodyPartSelectors.BODY_PART_SVG_FRONT(id_selector)))
        assert front_svg.is_displayed(), "SVG for 'FRONT' view is not displayed."

        # Validate min and max answers inputs
        input_min_answers = self.driver.find_element(*QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS(id_selector))
        input_max_answers = self.driver.find_element(*QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS(id_selector))
        assert input_min_answers.is_displayed(), "Input for minimum answers is not displayed."
        assert input_max_answers.is_displayed(), "Input for maximum answers is not displayed."

        # Validate interactable body part graphics
        head = BodyPartSelectors.HEAD(id_selector, "front")
        front_body_part = front_svg.find_element(*head)
        assert front_body_part.is_displayed(), "Body part (head) is not displayed in the front SVG."


        self.utils.scroll_to_bottom()
        self.select_body_parts({head})  # Select the body part
        WebDriverWait(self.driver, 10).until(
            lambda driver: "shape-selected" in front_body_part.get_attribute("class"),
            "Body part (head) was not selected."
        )

        # self.utils.click_element(front_body_part)  # Deselect the body part
        self.select_body_parts({head})  # Deselect the body part
        WebDriverWait(self.driver, 10).until(
            lambda driver: "shape-selected" not in front_body_part.get_attribute("class"),
            "Body part (head) was not deselected."
        )

        # Validate select/deselect all buttons
        parts_select = QuestionSelectors.BUTTON_SELECT_ALL_BODY_PARTS("FRONT")
        select_all_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            parts_select))

        parts_deselect = QuestionSelectors.BUTTON_DESELECT_ALL_BODY_PARTS("FRONT")
        deselect_all_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            parts_deselect))

        assert select_all_button.is_displayed(), "Button to select all body parts is not displayed."
        assert deselect_all_button.is_displayed(), "Button to deselect all body parts is not displayed."

        self.utils.click_element(parts_select)
        selected_parts = front_svg.find_elements(By.CLASS_NAME, "shape-selected")
        assert len(selected_parts) > 0, "No body parts were selected after clicking 'Select All'."

        self.utils.click_element(parts_deselect)
        selected_parts = front_svg.find_elements(By.CLASS_NAME, "shape-selected")
        assert len(selected_parts) == 0, "Body parts were not deselected after clicking 'Deselect All'."

    def assert_nq_validation_errors_de(self):
        question_type = QuestionType.NUMBER_INPUT
        id_selector = self.get_selector_for(question_type)

        # Set min > max and validate the corresponding error
        self.set_min_max_step_inputs(min_value="10", max_value="5", step_size="1", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_VALUE(id_selector),
            "Das Minimum der Zahleneingabe war gleich oder größer als das Maximum"
        )

        # Set invalid step size (greater than max-min difference)
        self.set_min_max_step_inputs(min_value="1.0", max_value="4.0", step_size="10", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_STEP_SIZE(id_selector),
            "Die Schrittgröße der Zahleneingabe war größer als der Abstand zwischen Minimum und Maximum\nDer Abstand zwischen Minimum und Maximum ist nicht restlos durch die Schrittgröße teilbar"
        )

    def assert_validation_errors_dd(self):
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)

        id_selector = self.get_selector_for(QuestionType.DROP_DOWN)
        language_code = self.DEFAULT_LANGUAGE_CODE

        # Error: Answer text missing
        self.validate_error_message(
            QuestionSelectors.ERROR_TEXTAREA_ANSWER(id_selector, 0, language_code),
            "Die Auswahlantwort benötigt einen lokalisierten Text"
        )

        # Error: Identification code missing
        self.validate_error_message(
            QuestionSelectors.ERROR_IDENT_CODE(0),
            "Der Identifikationscode darf nicht leer sein"
        )

    def assert_validation_errors_multiple_choice(self):
        id_selector = self.get_selector_for(QuestionType.MULTIPLE_CHOICE)

        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)

        # Error: Min and Max answers missing
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Minimale Anzahl von zu beantwortenden Antworten muss vergeben werden"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_MAX_ANSWERS(id_selector),
            "Maximale Anzahl von zu beantwortenden Antworten muss vergeben werden"
        )

        # Error: Identification code missing
        self.validate_error_message(
            QuestionSelectors.ERROR_IDENT_CODE(0),
            "Der Identifikationscode darf nicht leer sein"
        )

        # Max number greater than existing answers
        self.set_min_max_answers(id_selector, "1000", "1000")
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_MAX_ANSWERS(id_selector),
            "Maximale Anzahl von zu beantwortenden Antworten ist größer als die Anzahl der vorhandenen Antworten"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Minimale Anzahl von zu beantwortenden Antworten ist größer als die Anzahl der vorhandenen Antworten"
        )

        # Add answer and check error: Min greater than Max
        self.utils.scroll_and_click(QuestionSelectors.BUTTON_ADD_ANSWER)

        self.set_min_max_answers(id_selector, "2", "1")
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Minimale Anzahl von zu beantwortenden Antworten darf höchstens so groß wie die maximale Anzahl sein"
        )

        # Delete answer
        panels = self.driver.find_elements(*QuestionSelectors.MULTIPLE_CHOICE_ANSWER_PANELS)
        second_panel = panels[1]
        delete_button = second_panel.find_element(*QuestionSelectors.DELETE_BUTTON_WITHIN_PANEL)
        self.driver.execute_script("arguments[0].click();", delete_button)

    def assert_validation_errors_ncf(self):
        question_type = QuestionType.NUMBER_CHECKBOX_TEXT
        id_selector = self.get_selector_for(question_type)

        # Clear and validate initial errors
        self.clear_min_max_step_inputs(question_type)

        # Click save and validate errors for missing values
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_VALUE(id_selector),
            "Die Frage benötigt einen Minimalwert"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_MAX_VALUE(id_selector),
            "Die Frage benötigt einen Maximalwert"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_STEP_SIZE(id_selector),
            "Die Schrittweite der Frage entspricht nicht dem geforderten Format."
        )

        # Set min > max and validate the corresponding error
        self.set_min_max_step_inputs(min_value="10", max_value="5", step_size="1", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_VALUE(id_selector),
            "Das Minimum der Frage war gleich oder größer als das Maximum"
        )

        # Set invalid step size (greater than max-min difference)
        self.set_min_max_step_inputs(min_value="1.0", max_value="4.0", step_size="10", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_error_message(
            QuestionSelectors.ERROR_STEP_SIZE(id_selector),
            "Die Schrittweite der Frage war größer als der Abstand zwischen Minimum und Maximum\nDer Abstand zwischen Minimum und Maximum ist nicht restlos durch die Schrittweite teilbar"
        )

    def assert_validation_errors_dq(self):
        id_selector = self.get_selector_for(QuestionType.DATE)

        # Find start and end date inputs
        start_date = self.driver.find_element(*QuestionSelectors.INPUT_START_DATE(id_selector))
        end_date = self.driver.find_element(*QuestionSelectors.INPUT_END_DATE(id_selector))

        # Get today's date and date one week ago
        today =  datetime.now().strftime("%m/%d/%Y")
        one_week_ago =  (datetime.now() - timedelta(days=7)).strftime("%m/%d/%Y")

        start_date.clear()
        start_date.send_keys(today)
        end_date.clear()
        end_date.send_keys(one_week_ago)

        # Save the question
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)

        # Validate error message for incorrect date order
        self.validate_error_message(
            QuestionSelectors.ERROR_START_DATE(id_selector),
            "Das späteste Datum ist früher als das früheste Datum"
        )

    def assert_validation_errors_iq(self):
        id_selector = self.get_selector_for(QuestionType.IMAGE)

        # Save the question
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)

        # Validate error for missing image
        self.validate_error_message(
            QuestionSelectors.ERROR_FILE_PATH(id_selector),
            "Der Dateipfad darf nicht leer sein."
        )

    def assert_validation_errors_bp(self):
        id_selector = self.get_selector_for(QuestionType.BODY_PART)
        min_number_answers = QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS(id_selector)
        max_number_answers = QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS(id_selector)

        # Clear inputs and save to trigger validation errors for missing values
        min_input = self.driver.find_element(*min_number_answers)
        max_input = self.driver.find_element(*max_number_answers)
        min_input.clear()
        max_input.clear()
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.utils.scroll_to_bottom()

        # Validate error messages for missing min and max answers
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Es muss mindestens eine Körperregion als Antwort ausgewählt sein.\nMinimale Anzahl von zu beantwortenden Antworten muss vergeben werden"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_MAX_ANSWERS(id_selector),
            "Maximale Anzahl von zu beantwortenden Antworten muss vergeben werden"
        )

        # Select one body part and validate errors for exceeding the available selections
        selected_body_parts = self.select_body_parts({BodyPartSelectors.HEAD(id_selector, "front")})  # 1 body part selected

        min_input = self.driver.find_element(*min_number_answers)
        max_input = self.driver.find_element(*max_number_answers)
        min_input.clear()
        max_input.clear()
        self.utils.fill_text_field(min_number_answers, 100)
        self.utils.fill_text_field(max_number_answers, 100)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.utils.scroll_to_bottom()

        # Validate error messages when min and max exceed the number of selected body parts
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Minimale Anzahl von zu beantwortenden Antworten ist größer als die Anzahl der vorhandenen Antworten"
        )
        self.validate_error_message(
            QuestionSelectors.ERROR_MAX_ANSWERS(id_selector),
            "Maximale Anzahl von zu beantwortenden Antworten ist größer als die Anzahl der vorhandenen Antworten"
        )

        # Select another body part and validate errors for conflicting min and max values
        selected_body_parts.append(
            self.select_body_parts({BodyPartSelectors.THROAT(id_selector, "front")}))  # 2 body parts selected

        min_input = self.driver.find_element(*min_number_answers)
        max_input = self.driver.find_element(*max_number_answers)
        min_input.clear()
        max_input.clear()
        self.utils.fill_text_field(min_number_answers, 2)  # Set min to 2
        self.utils.fill_text_field(max_number_answers, 1)  # Set max to 1 (conflict with min)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.utils.scroll_to_bottom()

        # Validate error message when min exceeds max
        self.validate_error_message(
            QuestionSelectors.ERROR_MIN_ANSWERS(id_selector),
            "Minimale Anzahl von zu beantwortenden Antworten darf höchstens so groß wie die maximale Anzahl sein"
        )

    def assert_validation_errors_sq_nc(self, question_type: QuestionType):

        # Clear and validate initial errors
        self.clear_min_max_step_inputs(question_type)

        # Click save and validate errors for missing values
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)

        self.validate_min_max_step_errors(
            expected_errors=[
                "Die Frage benötigt einen Minimalwert",
                "Die Frage benötigt einen Maximalwert",
                "Die Schrittweite der Frage entspricht nicht dem geforderten Format."
            ]
        )

        # Set min > max and validate the corresponding error
        self.set_min_max_step_inputs(min_value="10", max_value="5", step_size="1", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        self.validate_min_max_step_errors(
            expected_errors=[
                "Das Minimum der Frage war gleich oder größer als das Maximum"
            ]
        )

        # Set invalid step size (greater than max-min difference)
        self.set_min_max_step_inputs(min_value="1.0", max_value="4.0", step_size="10", question_type=question_type)
        self.utils.click_element(QuestionSelectors.BUTTON_SAVE)
        # Second click validates spring
        self.validate_min_max_step_errors(
            expected_errors=[
                "Die Schrittweite der Frage war größer als der Abstand zwischen Minimum und Maximum\nDer Abstand zwischen Minimum und Maximum ist nicht restlos durch die Schrittweite teilbar"
            ]
        )

    def assert_sq_nc_common_validations(self, question_type: QuestionType):
        """
        Common validation for both Slider and Numbered Checkbox questions.
        :param question_type:
        """
        language_code = self.DEFAULT_LANGUAGE_CODE

        id_selector = self.get_selector_for(question_type)
        min_value = self.driver.find_element(*QuestionSelectors.INPUT_MIN_VALUE(id_selector))
        max_value = self.driver.find_element(*QuestionSelectors.INPUT_MAX_VALUE(id_selector))
        step_size = self.driver.find_element(*QuestionSelectors.INPUT_STEP_SIZE(id_selector))
        assert min_value.is_displayed(), "Input for minimum value is not displayed."
        assert max_value.is_displayed(), "Input for maximum value is not displayed."
        assert step_size.is_displayed(), "Input for step size is not displayed."

        wysiwyg_min_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.INPUT_WYSIWYG_SLIDER_MIN(language_code)))
        assert wysiwyg_min_text.is_displayed(), "WYSIWYG editor for text at minimum position is not displayed."

        wysiwyg_max_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.INPUT_WYSIWYG_SLIDER_MAX(language_code)))
        assert wysiwyg_max_text.is_displayed(), "WYSIWYG editor for text at maximum position is not displayed."

    def assert_mc_dd_common_validations(self, question_type):
        try:
            language_code = self.DEFAULT_LANGUAGE_CODE
            id_selector = self.get_selector_for(question_type)

            # Validate min and max answers
            input_min_answers = self.driver.find_element(*QuestionSelectors.INPUT_MIN_NUMBER_ANSWERS(id_selector))
            input_max_answers = self.driver.find_element(*QuestionSelectors.INPUT_MAX_NUMBER_ANSWERS(id_selector))
            assert input_min_answers.is_displayed(), "Input for min answers is not displayed."
            assert input_max_answers.is_displayed(), "Input for max answers is not displayed."

            # Shows select for type of identification
            dropdown_type_of_identification = self.driver.find_element(
                *QuestionSelectors.DROPDOWN_TYPE_OF_IDENTIFICATION)
            assert dropdown_type_of_identification.is_displayed(), "Dropdown for type of identification is not displayed."

            # Answer text
            input_answer_text = self.driver.find_element(
                *QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, 0, language_code))
            assert input_answer_text.is_displayed(), "Input for answer text is not displayed."

            # Initially activated and free text
            checkbox_initial_active = self.driver.find_element(
                *QuestionSelectors.CHECKBOX_ANSWER_INITIAL_ACTIVATION(id_selector, 0))
            checkbox_free_text = self.driver.find_element(*QuestionSelectors.CHECKBOX_FREE_TEXT(id_selector, 0))
            assert checkbox_initial_active.is_displayed(), "Checkbox to make answer initially active is not displayed."
            assert checkbox_free_text.is_displayed(), "Checkbox to activate free text field is not displayed."

            # Identification code and score
            input_identification = self.driver.find_element(*QuestionSelectors.INPUT_IDENTIFICATION(id_selector, 0))
            input_score = self.driver.find_element(*QuestionSelectors.INPUT_SCORE(id_selector, 0))
            assert input_identification.is_displayed(), "Input for identification is not displayed."
            assert input_score.is_displayed(), "Input for score is not displayed."

            # Validate add and delete answer buttons
            add_button = self.driver.find_element(*QuestionSelectors.BUTTON_ADD_ANSWER)
            assert add_button.is_displayed(), "Button to add an answer is not displayed."

            text = 'Text to duplicate'
            self.utils.fill_text_field(QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, 0, language_code), text)
            self.utils.scroll_and_click(QuestionSelectors.BUTTON_ADD_ANSWER)
            second_answer_text = self.driver.find_element(
                *QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, 1, language_code))
            second_answer_text_value = second_answer_text.get_attribute("value")
            assert second_answer_text_value == text, f"Text in the second answer field does not match. Expected: '{text}', Found: '{second_answer_text_value}'"

            panels = self.driver.find_elements(*QuestionSelectors.MULTIPLE_CHOICE_ANSWER_PANELS)
            second_panel = panels[1]
            delete_button = second_panel.find_element(*QuestionSelectors.DELETE_BUTTON_WITHIN_PANEL)
            assert delete_button.is_displayed(), "Button to delete an answer is not displayed."
            self.driver.execute_script("arguments[0].click();", delete_button)
            self.utils.clear_text_field(QuestionSelectors.TEXTAREA_ANSWER_TEXT(id_selector, 0, language_code))
        except AssertionError as e:
            raise e

    def assert_question_inputs(self, allowed_ids_of_visible_inputs):
        try:
            # Wait for the question type dropdown to be visible
            WebDriverWait(self.driver, 30).until(EC.visibility_of_element_located(
                QuestionSelectors.DROPDOWN_QUESTION_TYPE))

            # Get all visible input, select, and textarea elements
            visible_inputs = [
                element for element in self.driver.find_elements(By.CSS_SELECTOR, "input, select, textarea")
                if element.is_displayed()
            ]

            # Validate that each visible input has an ID in the allowed list
            for element in visible_inputs:
                element_id = element.get_attribute("id")
                assert element_id in allowed_ids_of_visible_inputs, f"Unexpected input found with id: {element_id}. Allowed: {allowed_ids_of_visible_inputs}"

            # Ensure the count of visible inputs matches the allowed list
            assert len(visible_inputs) == len(allowed_ids_of_visible_inputs), (
                f"Mismatch in visible inputs. Expected {len(allowed_ids_of_visible_inputs)}, found {len(visible_inputs)}."
            )
        except AssertionError as e:
            raise e

    def validate_error_message(self, selector, expected_message):
        error_message_element = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            selector))
        actual_message = error_message_element.text.strip()
        assert actual_message == expected_message, f"Unexpected error message: '{actual_message}'"

    def validate_min_max_step_errors(self, expected_errors):
        # Wait for all matching elements to be visible
        elements = WebDriverWait(self.driver, 30).until(
            lambda d: [el for el in d.find_elements(*QuestionSelectors.ERROR_SLIDER_FIELD) if el.is_displayed()]
        )

        element_texts = [el.get_attribute("innerHTML").replace("<br>", "\n").strip() for el in elements]
        # Extract the text from all visible elements

        for text in expected_errors:
            assert any(text in element_text for element_text in element_texts), f"'{text}' was not found in any element."



    def assert_question_table_functionality(self, expected_count=11):
        """
        :param expected_count: The expected number of questions.
        """
        self.assert_number_of_questions_and_buttons(expected_count)
        self.validate_reordering(original_index=0, new_index=2)
        self.assert_add_question_button()

    def assert_number_of_questions(self, expected_count):
        """
        :param expected_count: The expected number of questions.
        :raises AssertionError: If the number of questions does not match the expected count.
        """
        rows = [
            row for row in self.driver.find_element(*QuestionSelectors.TABLE_QUESTIONS)
            .find_element(By.TAG_NAME, "tbody")
            .find_elements(By.TAG_NAME, "tr") if row.is_displayed()
        ]
        assert len(rows) == expected_count, f"Expected {expected_count} questions, but found {len(rows)}."
        return rows

    def assert_number_of_questions_and_buttons(self, expected_count, min_buttons=3, max_buttons=4):
        """
        :param expected_count: The expected number of questions.
        :param min_buttons: The minimum number of action buttons per question.
        :param max_buttons: The maximum number of action buttons per question.
        :raises AssertionError: If the number of questions or action buttons does not match expectations.
        """
        rows = self.assert_number_of_questions(expected_count)

        for index, row in enumerate(rows, start=1):
            action_buttons = row.find_elements(*QuestionSelectors.ACTION_BUTTONS)
            assert min_buttons <= len(action_buttons) <= max_buttons, (
                f"Row {index} does not have the expected range of action buttons ({min_buttons}-{max_buttons}). "
                f"Found: {len(action_buttons)}."
            )

    def validate_reordering(self, original_index, new_index):
        """
        :param original_index: The index of the question to be moved.
        :param new_index: The target index where the question should be dropped.
        :raises AssertionError: If the reordering does not succeed.
        """
        # Get all rows
        rows = WebDriverWait(self.driver, 10).until(
            lambda d: d.find_elements(*QuestionSelectors.TABLE_ROWS)
        )
        assert len(rows) > max(original_index, new_index), "Invalid indices for reordering."

        # Identify source and target rows
        source_id = rows[original_index].get_attribute('id')
        target_id = rows[new_index].get_attribute('id')

        # Use the drag-and-drop utility
        self.utils.drag_and_drop(QuestionSelectors.GRIP_SELECTOR(source_id),
                                 QuestionSelectors.GRIP_SELECTOR(target_id))

        # Validate the reordering
        reordered_rows = WebDriverWait(self.driver, 10).until(
            lambda d: d.find_elements(*QuestionSelectors.TABLE_ROWS)
        )
        reordered_ids = [row.get_attribute("id") for row in reordered_rows]

        # Calculate expected order directly
        expected_order = [row.get_attribute("id") for row in rows]
        item_to_move = expected_order.pop(original_index)
        expected_order.insert(new_index, item_to_move)

        # Assert the new order matches the expected order
        assert reordered_ids == expected_order, f"Reordering failed. Expected: {expected_order}, Found: {reordered_ids}"

    def assert_add_question_button(self):
        add_question_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            QuestionSelectors.BUTTON_ADD_QUESTION))
        assert add_question_button.is_displayed(), "The 'Add Question' button is not displayed."



