from selenium.common import NoSuchElementException, TimeoutException
from selenium.webdriver import Keys, ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from helper.Question import QuestionType
from helper.Questionnaire import QuestionnaireHelper
from helper.SeleniumUtils import DropdownMethod


class ConditionSelectors:
    BUTTON_ADD_CONDITION = (By.ID, 'addCondition')
    BUTTON_ADD_TARGET = (By.ID, 'addTargetButton')
    BUTTON_SAVE_CONDITION = (By.ID, 'saveCondition')
    BUTTON_CANCEL = (By.ID, "cancelButton")
    BUTTON_BACK_TO_QUESTIONNAIRE = (By.ID, 'backToQuestionnaire')
    BUTTON_DELETE_CONDITION = lambda condition_id : (By.XPATH, f"//a[contains(@href, 'remove?id={condition_id}')]")

    DROPDOWN_TRIGGER = (By.ID, "triggerId")
    DROPDOWN_QUESTION = (By.ID, 'questionDropDown0')

    INPUT_THRESHOLD = (By.ID, 'conditionDTOs0.thresholdValue')

    TABLE_CONDITIONS_QUESTION = (By.ID, "DataTables_Table_0")
    TABLE_CONDITIONS_ANSWER = (By.ID, "DataTables_Table_1")
    TABLE_CONDITIONS_QUESTIONNAIRE = (By.ID, "DataTables_Table_2")
    TABLE_LAST_ROW = (By.XPATH, ".//tbody/tr[last()]")

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

    def add_condition_for_triggered_questions(self, trigger_index=0,
                                              target_type=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                              answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        :param trigger_index: The index of the trigger option in the dropdown (default is 0).
        :param target_type: The target type for the condition (e.g., 'QUESTION', 'ANSWER').
        :param target_option_index: The index of the target question/answer in the dropdown.
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER'.
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.click_add_condition()

        # Set the trigger option
        if trigger_index > 0:
            self.utils.select_dropdown(ConditionSelectors.DROPDOWN_TRIGGER, trigger_index, DropdownMethod.INDEX)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_type, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

        return self.get_last_added_condition_id(target_type)


    def add_condition_for_threshold_questions(self, threshold_type=ConditionSelectors.ThresholdType.EQUALS,
                                              threshold_steps=1, target_type=ConditionSelectors.TargetType.QUESTION, target_option_index=0,
                                              answer_target_index=0, action=ConditionSelectors.ActionType.DISABLE):
        """
        Adds a condition for threshold-based question types like Slider, Number Input, and Number Checkbox.

        :param threshold_type: The type of threshold to use (e.g., 'EQUALS').
        :param threshold_steps: The number of arrow-up key presses to set the threshold value.
        :param target_type: The target type for the condition (e.g., 'QUESTION', 'ANSWER').
        :param target_option_index: The index of the target question/answer in the dropdown.
        :param answer_target_index: The index of the answer target if the target class is 'ANSWER'.
        :param action: The action to perform (e.g., 'DISABLE' or 'ENABLE').
        """
        self.click_add_condition()
        
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

        return self.get_last_added_condition_id(target_type)


    def add_basic_condition_based_on_question_type(self, target_type, question):
        """
        :param target_type: The target type for the condition ('QUESTION', 'ANSWER', 'QUESTIONNAIRE').
        :param question: The question details as a dictionary.
        """
        if question['type'] in [QuestionType.SLIDER, QuestionType.NUMBER_INPUT, QuestionType.NUMBER_CHECKBOX]:
            return self.add_condition_for_threshold_questions(target_type=target_type)
        elif question['type'] in [QuestionType.DROP_DOWN, QuestionType.MULTIPLE_CHOICE]:
            return self.add_condition_for_triggered_questions(target_type=target_type)
        else:
            raise ValueError(f"Unsupported QuestionType: {question['type']}")

    def navigate_back_to_questions_of_questionnaire(self):
        try:
            back_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ConditionSelectors.BUTTON_BACK_TO_QUESTIONNAIRE))
            back_button.click()
        except TimeoutException:
            print("Failed to find the 'Back to Questionnaire' button.")

    def open_conditions_of_question(self, question_id):
        self.driver.find_element(*ConditionSelectors.CONDITION_LINK(question_id)).click()

    def click_add_condition(self):
        try: 
            add_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ConditionSelectors.BUTTON_ADD_CONDITION))
            add_button.click()
        except:
            print("Failed to click 'add button' for conditions.")

    def cancel_condition_editing(self):
        self.utils.click_element(ConditionSelectors.BUTTON_CANCEL)

    def get_last_added_condition_id(self, target_type):
        """
        Retrieves the ID of the most recently added condition from the appropriate table.

        :param target_type: The target type of the condition (e.g., 'QUESTION', 'ANSWER', 'QUESTIONNAIRE').
        :return: The ID of the last condition as a string.
        :raises ValueError: If the target type is unsupported.
        :raises Exception: If the ID cannot be found.
        """
        # Map the target type to the corresponding table selector
        if target_type == ConditionSelectors.TargetType.QUESTION:
            target_table_selector = ConditionSelectors.TABLE_CONDITIONS_QUESTION
        elif target_type == ConditionSelectors.TargetType.ANSWER:
            target_table_selector = ConditionSelectors.TABLE_CONDITIONS_ANSWER
        elif target_type == ConditionSelectors.TargetType.QUESTIONNAIRE:
            target_table_selector = ConditionSelectors.TABLE_CONDITIONS_QUESTIONNAIRE
        else:
            raise ValueError(f"Unsupported TargetType: {target_type}")

        # Wait for the table to be present
        WebDriverWait(self.driver, 30).until(EC.presence_of_element_located(target_table_selector))

        # Locate the last row in the table
        table = self.driver.find_element(*target_table_selector)
        last_row = table.find_element(*ConditionSelectors.TABLE_LAST_ROW)

        # Extract the ID from the last row
        condition_id = last_row.get_attribute("id")

        # Ensure the ID is found
        if not condition_id:
            raise Exception("The ID of the last condition could not be found.")

        return condition_id


    def delete_condition(self, condition_id, questionnaire=None, question_id=None):
        """
        :param condition_id: The ID of the condition to delete.
        :param questionnaire: The questionnaire containing the condition (optional).
        :param question_id: The question associated with the condition (optional).
        """
        try:
            # Navigate to the questionnaire if provided
            if questionnaire:
                self.navigation_helper.navigate_to_questions_of_questionnaire(questionnaire['id'], questionnaire['name'])

            # Open the conditions of the specified question if provided
            if question_id:
                self.open_conditions_of_question(question_id)

            # Locate and click the delete button for the condition
            delete_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(ConditionSelectors.BUTTON_DELETE_CONDITION(condition_id))
            )
            delete_button.click()

        except Exception as e:
            raise AssertionError(f"Error deleting condition with ID {condition_id}: {e}")

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

            search_input = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(search_input_selector)
            )
            search_input.clear()
        except Exception as e:
            raise AssertionError(f"Error asserting search functionality: {e}")

    def assert_condition_list_and_search_de(self):

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


    def assert_add_condition_page(self, questionnaire):

        questions = questionnaire['questions']
        number_of_questions = len(questions)

        info_text_question = next((question for question in questions if question['type'] == QuestionType.INFO_TEXT), None)

        if not info_text_question:
            self.click_add_question_button()
            info_text_question = self.question_helper.add_question_by_type_default_value(QuestionType.INFO_TEXT)
            info_text_question['id'] = self.question_helper.save_question()
            number_of_questions += 1

        self.reorder_question(info_text_question['id'], number_of_questions-1)

        try:
            for question in questions:
                if question['type'] not in [QuestionType.SLIDER, QuestionType.MULTIPLE_CHOICE, QuestionType.NUMBER_CHECKBOX, QuestionType.DROP_DOWN, QuestionType.NUMBER_INPUT]:
                    continue
                # Open the 'Add Condition' page
                self.open_conditions_of_question(question['id'])
                self.click_add_condition()
                # Validate the inputs for adding a condition
                self.validate_condition_inputs(question)
                self.cancel_condition_editing()
                self.navigate_back_to_questions_of_questionnaire()

            question = next((question for question in questions if question['type'] in [QuestionType.SLIDER, QuestionType.MULTIPLE_CHOICE, QuestionType.NUMBER_CHECKBOX, QuestionType.DROP_DOWN, QuestionType.NUMBER_INPUT]),None)
            self.open_conditions_of_question(question['id'])

            target_type_question = ConditionSelectors.TargetType.QUESTION
            # Assert no conditions are initially present in the table
            self.assert_condition_count(target_type_question, expected_count=0)

            condition_id = self.add_basic_condition_based_on_question_type(target_type_question, question)

            # Assert the condition is successfully created and added to the table
            self.assert_condition_count(target_type_question, expected_count=1)

            self.delete_condition(condition_id)

        except Exception as e:
            raise AssertionError(f"Error in assert_condition_addCondition_page: {e}")

    def validate_condition_inputs(self, question):
        """
        :param question: A dictionary containing the details of the question.
        :raises AssertionError: If any expected input is not displayed.
        """
        try:
            # Common inputs for all question types
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ConditionSelectors.DROPDOWN_TARGET_CLASS))
            assert self.driver.find_element(
                *ConditionSelectors.DROPDOWN_TARGET_CLASS).is_displayed(), "Target class dropdown is not displayed."
            WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(ConditionSelectors.DROPDOWN_ACTION))
            assert self.driver.find_element(
                *ConditionSelectors.DROPDOWN_ACTION).is_displayed(), "Action dropdown is not displayed."

            # Specific inputs based on question type
            if question['type'] in [QuestionType.SLIDER, QuestionType.NUMBER_CHECKBOX, QuestionType.NUMBER_INPUT]:
                WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(ConditionSelectors.THRESHOLD_TYPE))
                assert self.driver.find_element(
                    *ConditionSelectors.THRESHOLD_TYPE).is_displayed(), "Threshold type dropdown is not displayed."
                WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(ConditionSelectors.INPUT_THRESHOLD))
                assert self.driver.find_element(
                    *ConditionSelectors.INPUT_THRESHOLD).is_displayed(), "Threshold input is not displayed."

            elif question['type'] in [QuestionType.MULTIPLE_CHOICE, QuestionType.DROP_DOWN]:
                WebDriverWait(self.driver, 10).until(
                    EC.presence_of_element_located(ConditionSelectors.DROPDOWN_TRIGGER))
                assert self.driver.find_element(
                    *ConditionSelectors.DROPDOWN_TRIGGER).is_displayed(), "Trigger dropdown is not displayed."

            else:
                raise ValueError(f"Unsupported QuestionType: {question['type']}")

        except Exception as e:
            raise AssertionError(f"Error validating condition inputs for question type '{question['type']}': {e}")

    def assert_condition_count(self, target_type, expected_count):
        """
        Asserts the number of conditions in the table matches the expected count.

        :param target_type: The target type for the condition (e.g., 'QUESTION', 'ANSWER', 'QUESTIONNAIRE').
        :param expected_count: The expected number of conditions in the table.
        """
        # Determine the table selector based on the target type
        if target_type == ConditionSelectors.TargetType.QUESTION:
            table_selector = ConditionSelectors.TABLE_CONDITIONS_QUESTION
        elif target_type == ConditionSelectors.TargetType.ANSWER:
            table_selector = ConditionSelectors.TABLE_CONDITIONS_ANSWER
        else:
            table_selector = ConditionSelectors.TABLE_CONDITIONS_QUESTIONNAIRE

        # Wait for the table to be present
        table = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(table_selector)
        )

        # Find all rows with an `id` attribute in the table
        rows_with_id = table.find_elements(By.XPATH, ".//tbody/tr[@id]")

        # Check if the number of rows with an `id` matches the expected count
        if not rows_with_id and expected_count == 0:
            # Special case: Table is empty, and no conditions are expected
            return

        # Assert the count of rows with an `id`
        assert len(rows_with_id) == expected_count, (
            f"Expected {expected_count} conditions, but found {len(rows_with_id)}."
        )