from selenium.common import NoSuchElementException, TimeoutException
from selenium.webdriver import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from helper.Questionnaire import QuestionnaireHelper
from helper.SeleniumUtils import DropdownMethod


class ConditionSelectors:
    BUTTON_ADD_CONDITION = (By.ID, 'addCondition')
    BUTTON_ADD_TARGET = (By.ID, 'addTargetButton')
    BUTTON_SAVE_CONDITION = (By.ID, 'saveCondition')
    BUTTON_BACK_TO_QUESTIONNAIRE = (By.ID, 'backToQuestionnaire')

    DROPDOWN_TRIGGER = (By.ID, "triggerId")
    DROPDOWN_QUESTION = (By.ID, 'questionDropDown0')

    INPUT_THRESHOLD = (By.ID, 'conditionDTOs0.thresholdValue')

    TABLE_CONDITIONS_QUESTION = (By.ID, "DataTables_Table_0")
    TABLE_CONDITIONS_ANSWER = (By.ID, "DataTables_Table_1")
    TABLE_CONDITIONS_QUESTIONNAIRE = (By.ID, "DataTables_Table_2")

    # Search input fields for each table
    SEARCH_INPUT_CONDITIONS_QUESTION = (By.CSS_SELECTOR, "#DataTables_Table_0_filter input")
    SEARCH_INPUT_CONDITIONS_ANSWER = (By.CSS_SELECTOR, "#DataTables_Table_1_filter input")
    SEARCH_INPUT_CONDITIONS_QUESTIONNAIRE = (By.CSS_SELECTOR, "#DataTables_Table_2_filter input")

    # Empty row in each table
    EMPTY_ROW = (By.CSS_SELECTOR, "tbody .dataTables_empty")

    CONDITION_LINK = lambda question_id: (By.XPATH, f'//a[contains(@href, "/condition/listQuestionConditions?questionId={question_id}")]')

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
        QUESTIONNAIRE = "de.imi.mopat.model.Questionnaire"

    DROPDOWN_TARGET_QUESTION = (By.ID, 'targetAnswerQuestionDropDown0')
    DROPDOWN_TARGET_ANSWER = (By.ID, 'questionDropDown0')
    DROPDOWN_ANSWER_TARGET = (By.ID, 'answerDropDown0')

    DROPDOWN_ACTION = (By.ID, 'action0')
    class ActionType:
        DISABLE = "DISABLE"
        ENABLE = "ENABLE"

class ConditionHelper(QuestionnaireHelper):

    def can_add_condition(self, question_id):
        """
        :param question_id: The ID of the question.
        :return: True if a condition can be added, False otherwise.
        """
        try:
            self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id))
            return True
        except NoSuchElementException:
            return False

    def set_target_and_action(self, target_class, target_option_index, answer_target_index, action):
        """
        :param target_class: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown.
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER'.
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TARGET_CLASS, target_class, DropdownMethod.VALUE)

        if target_option_index > 0:
            if target_class == ConditionSelectors.TargetType.ANSWER:
                self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TARGET_QUESTION, target_option_index, DropdownMethod.INDEX)
            else:
                self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TARGET_QUESTION, target_option_index,DropdownMethod.INDEX)
            self.utils.handle_popup_alert(accept=True)

        if target_class == ConditionSelectors.TargetType.ANSWER:
            self.utils.select_dropdown(ConditionSelectors.DROPDOWN_ANSWER_TARGET, answer_target_index, DropdownMethod.INDEX)

        self.utils.select_dropdown(ConditionSelectors.DROPDOWN_ACTION, action, DropdownMethod.VALUE)

    def add_condition_for_multiple_choice(self, question_id, trigger_index=0,
                                          target_class=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                          answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param question_id: The ID of the question to which the condition is being added.
        :param trigger_index: The index of the trigger option in the dropdown (default is 0, which means no trigger is selected).
        :param target_class: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown (default is 0, which means no target is selected).
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER' (default is 0).
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id)).click()
        self.driver.find_element(*ConditionSelectors.BUTTON_ADD_CONDITION).click()

        # Set the trigger option
        if trigger_index > 0:
            self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TRIGGER, trigger_index, DropdownMethod.INDEX)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def add_condition_for_slider(self, threshold_type=ConditionSelectors.ThresholdType.BIGGER_THAN_EQUALS,
                                 threshold_steps=1, target_type=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                 answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param threshold_type: The type of threshold to use (e.g., 'BIGGER_THAN_EQUALS').
        :param threshold_steps: The number of arrow-up key presses to set the threshold value.
        :param target_type: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown (default is 0, which means no target is selected).
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER' (default is 0).
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.driver.find_element(*ConditionSelectors.BUTTON_ADD_CONDITION).click()

        # Set the threshold type and value
        self.utils.select_dropdown(ConditionSelectors.THRESHOLD_TYPE, threshold_type, DropdownMethod.VALUE)
        if threshold_type != ConditionSelectors.ThresholdType.SMALLER_THAN:
            self.utils.handle_popup_alert(True)

        # Increment the threshold value using the arrow key
        for _ in range(threshold_steps):
            threshold_input = self.driver.find_element(*ConditionSelectors.INPUT_THRESHOLD)
            threshold_input.click()
            threshold_input.send_keys(Keys.ARROW_UP)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_type, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def add_condition_for_number_input(self, question_id, threshold_type=ConditionSelectors.ThresholdType.EQUALS,
                                       target_class=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                       answer_target_index=2, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param question_id: The ID of the question to which the condition is being added.
        :param threshold_type: The type of threshold to use (e.g., 'EQUALS').
        :param target_class: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown (default is 0, which means no target is selected).
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER' (default is 2).
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id)).click()
        self.driver.find_element(*ConditionSelectors.BUTTON_ADD_CONDITION).click()

        # Set the threshold type and value
        self.utils.select_dropdown(ConditionSelectors.THRESHOLD_TYPE, threshold_type, DropdownMethod.VALUE)
        if threshold_type != ConditionSelectors.ThresholdType.SMALLER_THAN:
            self.utils.handle_popup_alert(True)

        threshold_input = self.driver.find_element(*ConditionSelectors.INPUT_THRESHOLD)
        threshold_input.click()
        threshold_input.send_keys(Keys.ARROW_UP)
        self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()


    def add_condition_for_numbered_checkbox(self, question, threshold_type=ConditionSelectors.ThresholdType.BIGGER_THAN_EQUALS,
                                 target_class=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                 answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param question: A dictionary containing question details (must include 'id').
        :param threshold_type: The type of threshold to use (e.g., 'BIGGER_THAN_EQUALS').
        :param target_class: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown (default is 0, which means no target is selected).
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER' (default is 0).
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question['id'])).click()
        self.driver.find_element(*ConditionSelectors.BUTTON_ADD_CONDITION).click()

        # Set the threshold type and value
        self.utils.select_dropdown(ConditionSelectors.THRESHOLD_TYPE, threshold_type, DropdownMethod.VALUE)
        if threshold_type != ConditionSelectors.ThresholdType.SMALLER_THAN:
            self.utils.handle_popup_alert(True)

        # Increment the threshold value using the arrow key
        threshold_input = self.driver.find_element(*ConditionSelectors.INPUT_THRESHOLD)
        threshold_input.click()
        threshold_input.send_keys(Keys.ARROW_UP)
        self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def add_condition_for_drop_down(self, question_id, trigger_index=1,
                                    target_class=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                    answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param question_id: The ID of the question to which the condition is being added.
        :param trigger_index: The index of the trigger option in the dropdown (default is 1).
        :param target_class: The type of the target (e.g., 'ANSWER' or 'QUESTION').
        :param target_option_index: The index of the target question/answer in the dropdown (default is 0, which means no target is selected).
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER' (default is 0).
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id)).click()
        self.driver.find_element(*ConditionSelectors.BUTTON_ADD_CONDITION).click()

        # Set the trigger option
        if trigger_index > 0:
            self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TRIGGER, trigger_index, DropdownMethod.INDEX)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def navigate_back_to_questionnaire(self):
        try:
            back_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ConditionSelectors.BUTTON_BACK_TO_QUESTIONNAIRE))
            back_button.click()
        except TimeoutException:
            print("Failed to find the 'Back to Questionnaire' button.")

    def open_conditions_of_question(self, question_id):
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id)).click()


class ConditionAssertHelper(ConditionHelper):

    def assert_condition_in_section(self, table_selector, condition_text):
        """
        :param table_selector: The table selector to check.
        :param condition_text: The condition text to search for.
        """
        try:
            table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(table_selector))
            rows = table.find_elements(By.CSS_SELECTOR, "tbody tr")

            assert any(condition_text in row.text for row in rows), (
                f"Condition '{condition_text}' not found in table {table_selector}."
            )

        except Exception as e:
            raise AssertionError(f"Error asserting condition in section: {e}")

    def assert_search_functionality(self, search_input_selector, table_selector, condition_text):
        """
        :param search_input_selector: The search input field selector.
        :param table_selector: The table selector to check.
        :param condition_text: The condition text to search for.
        """
        try:
            # Input search text
            search_input = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(search_input_selector)
            )
            search_input.clear()
            search_input.send_keys(condition_text)

            # Verify the condition appears in the table
            self.assert_condition_in_section(table_selector, condition_text)

        except Exception as e:
            raise AssertionError(f"Error asserting search functionality: {e}")

    def assert_question_condition_list_de(self):

        # Assert that the sections are displayed
        try:
            # Wait for and verify the three tables are visible
            tables = [
                ConditionSelectors.TABLE_CONDITIONS_QUESTION,
                ConditionSelectors.TABLE_CONDITIONS_ANSWER,
                ConditionSelectors.TABLE_CONDITIONS_QUESTIONNAIRE
            ]

            for table in tables:
                WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(table))
                assert self.driver.find_element(*table).is_displayed(), f"Table {table} is not displayed."

        except Exception as e:
            raise AssertionError(f"Error asserting conditions sections: {e}")

        # Assert conditions in respective sections
        self.assert_condition_in_section(
            ConditionSelectors.TABLE_CONDITIONS_QUESTION, "Die Frage"
        )
        self.assert_condition_in_section(
            ConditionSelectors.TABLE_CONDITIONS_ANSWER, "Die Antwort"
        )
        self.assert_condition_in_section(
            ConditionSelectors.TABLE_CONDITIONS_QUESTIONNAIRE, "Den Fragebogen"
        )

        # Test search functionality
        self.assert_search_functionality(
            ConditionSelectors.SEARCH_INPUT_CONDITIONS_QUESTION,
            ConditionSelectors.TABLE_CONDITIONS_QUESTION,
            "Die Frage"
        )