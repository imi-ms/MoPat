from selenium.webdriver import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.wait import WebDriverWait

from helper.Questionnaire import QuestionnaireHelper

class ScoreSelectors:
    BUTTON_ADD_SCORE = (By.ID, "addScore")
    BUTTON_SAVE = (By.ID, "saveButton")

    DROPDOWN_OPERATOR = lambda index: (By.NAME, f"expression.expressions[{index}].operatorId")
    DROPDOWN_EXPRESSION = (By.ID, "expression")
    DROPDOWN_QUESTION = (By.NAME, "expression.questionId")
    DROPDOWN_SCORE = (By.NAME, "expression.scoreId")

    INPUT_NAME = (By.ID, "name")
    INPUT_VALUE = lambda index: (By.NAME, f"expression.expressions[{index}].value")

    # Specific score types
    class ExpressionType:
        ADDITION = "1"
        SUBTRACTION = "2"
        MULTIPLICATION = "3"
        DIVISION = "4"
        QUESTION_VALUE = "5"
        VALUE = "6"
        SUM = "7"
        GREATER_THAN = "8"
        GREATER_THAN_EQUALS = "9"
        LESS_THAN = "10"
        LESS_THAN_EQUALS = "11"
        EQUALS = "12"
        NOT_EQUALS = "13"
        COUNTER = "14"
        AVERAGE = "15"
        VALUE_OF_SCORE = "16"
        MAXIMUM = "17"
        MINIMUM = "18"


class ScoreHelper(QuestionnaireHelper):

    def click_add_score_button(self):
        self.utils.click_element(ScoreSelectors.BUTTON_ADD_SCORE)

    def save_score(self):
        self.utils.click_element(ScoreSelectors.BUTTON_SAVE)

    def add_basic_score(self, name, expression_type, steps=1):
        """
        :param name: Name of the score.
        :param expression_type: The type of expression (e.g., '1' for addition, '5' for question value, etc.).
        :param steps: Number of times to press ARROW_UP for value fields (default is 1).
        """
        self.utils.fill_text_field(ScoreSelectors.INPUT_NAME, name)
        self.utils.select_dropdown(ScoreSelectors.DROPDOWN_EXPRESSION, expression_type, "value")

        # Dispatch logic based on expression type
        if expression_type in [ScoreSelectors.ExpressionType.ADDITION, ScoreSelectors.ExpressionType.SUBTRACTION, ScoreSelectors.ExpressionType.MULTIPLICATION, ScoreSelectors.ExpressionType.DIVISION]:
            self._binary_score(steps)
        elif expression_type == ScoreSelectors.ExpressionType.QUESTION_VALUE:
            self._question_value_score()
        elif expression_type == ScoreSelectors.ExpressionType.VALUE:
            self._value_score(steps)
        elif expression_type == ScoreSelectors.ExpressionType.SUM:
            self._sum_score(steps)
        elif expression_type in [ScoreSelectors.ExpressionType.GREATER_THAN, ScoreSelectors.ExpressionType.GREATER_THAN_EQUALS, ScoreSelectors.ExpressionType.LESS_THAN, ScoreSelectors.ExpressionType.LESS_THAN_EQUALS, ScoreSelectors.ExpressionType.EQUALS, ScoreSelectors.ExpressionType.NOT_EQUALS]:
            self._binary_score(steps)
        elif expression_type == ScoreSelectors.ExpressionType.COUNTER:
            self._counter_score(steps)
        elif expression_type == ScoreSelectors.ExpressionType.AVERAGE:
            self._average_score(steps)
        elif expression_type == ScoreSelectors.ExpressionType.VALUE_OF_SCORE:
            self._value_of_score()
        elif expression_type in [ScoreSelectors.ExpressionType.MAXIMUM, ScoreSelectors.ExpressionType.MINIMUM]:
            self._min_or_max_score(steps)

    # Score Logic
    def _binary_score(self, steps):
        """binary scores (e.g., +, -, *, /)."""
        self._set_operator_and_value(0, steps=steps)
        self._set_operator_and_value(1, steps=steps)

    def _question_value_score(self):
        self.utils.select_dropdown(ScoreSelectors.DROPDOWN_QUESTION, 2, "index")

    def _value_score(self, steps):
        self.utils.set_value("expression.value", steps)

    def _sum_score(self, steps):
        self._set_operator_and_value(0, steps=steps)

    def _counter_score(self, steps):
        self._set_operator_and_value(0, nested=True, steps=steps)
        self.utils.select_dropdown((By.NAME, "expression.expressions[0].operatorId"), ScoreSelectors.ExpressionType.LESS_THAN_EQUALS, "value")
        self._set_operator_and_value(1, nested=True, steps=steps)

    def _average_score(self, steps):
        self._set_operator_and_value(0, steps=steps)

    def _value_of_score(self):
        self.utils.select_dropdown(ScoreSelectors.DROPDOWN_SCORE, 1, "index")

    def _min_or_max_score(self, steps):
        self._set_operator_and_value(0, steps=steps)

    # Helpers
    def _set_operator_and_value(self, index, steps=1, nested=False):
        """
        :param index: The index of the operator and value to set.
        :param steps: Number of times to press ARROW_UP for the value (default is 1).
        :param nested: Whether the value is nested in another structure.
        """
        # Adjust the base name for nested structures
        base_name = f"expression.expressions[0]." if nested else ""

        operator_selector = (By.NAME, f"{base_name}expression.expressions[{index}].operatorId")
        value_selector = (By.NAME, f"{base_name}expression.expressions[{index}].value")

        self.utils.select_dropdown(operator_selector, ScoreSelectors.ExpressionType.VALUE, "value")
        self.utils.set_value(value_selector[1], steps=steps)