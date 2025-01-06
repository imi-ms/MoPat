from selenium.common import NoSuchElementException, TimeoutException
from selenium.webdriver import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.wait import WebDriverWait

from helper.Questionnaire import QuestionnaireHelper, ConditionSelectors


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
        Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_TARGET_CLASS)).select_by_value(target_class)

        if target_option_index > 0:
            if target_class == ConditionSelectors.TargetType.ANSWER:
                Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_TARGET_ANSWER)).select_by_index(target_option_index)
            else:
                Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_TARGET_QUESTION)).select_by_index(target_option_index)
            self.utils.handle_popup_alert(True)

        if target_class == ConditionSelectors.TargetType.ANSWER:
            Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_ANSWER_TARGET)).select_by_index(answer_target_index)

        Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_ACTION)).select_by_value(action)

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
            Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_TRIGGER)).select_by_index(trigger_index)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def add_condition_for_slider(self, question, threshold_type=ConditionSelectors.ThresholdType.BIGGER_THAN_EQUALS,
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
        Select(self.driver.find_element(*ConditionSelectors.THRESHOLD_TYPE)).select_by_value(threshold_type)
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
        Select(self.driver.find_element(*ConditionSelectors.THRESHOLD_TYPE)).select_by_value(threshold_type)
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
        Select(self.driver.find_element(*ConditionSelectors.THRESHOLD_TYPE)).select_by_value(threshold_type)
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
            Select(self.driver.find_element(*ConditionSelectors.DROPDOWN_TRIGGER)).select_by_index(trigger_index)
            self.utils.handle_popup_alert(True)

        # Set the target and action
        self.set_target_and_action(target_class, target_option_index, answer_target_index, action)

        # Save the condition
        self.driver.find_element(*ConditionSelectors.BUTTON_SAVE_CONDITION).click()

    def navigate_back_to_questionnaire(self):
        """
        Navigates back to the questionnaire overview by clicking the 'Back to Questionnaire' button.
        """
        try:
            back_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(ConditionSelectors.BUTTON_BACK_TO_QUESTIONNAIRE)
            )
            back_button.click()
        except TimeoutException:
            print("Failed to find the 'Back to Questionnaire' button.")
