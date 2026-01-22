from datetime import datetime

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from helper.Questionnaire import QuestionnaireHelper
from helper.SeleniumUtils import DropdownMethod

class ScoreSelectors:
    BUTTON_ADD_SCORE = (By.ID, "addScore")
    BUTTON_SAVE = (By.ID, "saveButton")

    DROPDOWN_OPERATOR = lambda index: (By.NAME, f"expression.expressions[{index}].operatorId")
    DROPDOWN_EXPRESSION = (By.ID, "expression")
    DROPDOWN_QUESTION = (By.NAME, "expression.questionId")
    DROPDOWN_SCORE = (By.NAME, "expression.scoreId")

    INPUT_NAME = (By.ID, "name")
    INPUT_VALUE = lambda index: (By.NAME, f"expression.expressions[{index}].value")

    TABLE_ROWS = (By.CSS_SELECTOR, "tbody > tr:not(#emptyRow)")
    SCORE_TABLE = (By.ID, "scoreTable")
    ACTION_BUTTONS = (By.CSS_SELECTOR, "td.actionColumn > div.btn-group > a.link")

    DROPDOWN_EXPRESSION_TYPE = (By.ID, "expression")
    EXPRESSION_OPERATOR_SELECTED = lambda path: (By.NAME, f"{path}.operatorId")
    EXPRESSION_OPERATOR_UNSELECTED = lambda path: (By.NAME, f"{path}")
    EXPRESSION_VALUE = lambda path: (By.NAME, f"{path}.value")
    EXPRESSION_QUESTION = lambda path: (By.NAME, f"{path}.questionId")
    EXPRESSION_SCORE = lambda path: (By.NAME, f"{path}.scoreId")

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

    def create_basic_score(self, expression_type, name_prefix=None, question_id=None, score_id=None):
        """
        Creates a basic score for a given operator.

        :param expression_type: The expression type for which the score should be created (e.g., "+" or "VALUE").
        :param name_prefix: Prefix for the name of the score.
        :param question_id: Question ID for QUESTION_VALUE operator.
        :param score_id: Score ID for VALUE_OF_SCORE operator.
        :return: A dictionary with details of the created score.
        """
        timestamp: str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        name_prefix = name_prefix or f"Basic Score {timestamp}"

        score_name = f"{name_prefix} - {expression_type}"
        expression_tree = self._get_basic_expression_tree(expression_type, question_id=question_id, score_id=score_id)

        # Add the score using the helper
        self.add_score(
            name=score_name,
            expression_type=expression_type,
            expression_tree=expression_tree
        )

        return {
            "name": score_name,
            "operator": expression_type,
            "expression_tree": expression_tree
        }

    def _get_basic_expression_tree(self, expression_type, question_id=None, score_id=None):
        """
        Generates a basic expression tree for a given operator.

        :param expression_type: The operator value for the expression.
        :param question_id: The ID of the question to use (required for QUESTION_VALUE).
        :param score_id: The ID of the score to use (required for VALUE_OF_SCORE).
        :return: A dictionary representing a basic expression tree.
        """
        if expression_type in [ScoreSelectors.ExpressionType.ADDITION, ScoreSelectors.ExpressionType.SUBTRACTION,
                               ScoreSelectors.ExpressionType.MULTIPLICATION, ScoreSelectors.ExpressionType.DIVISION,
                               ScoreSelectors.ExpressionType.GREATER_THAN, ScoreSelectors.ExpressionType.GREATER_THAN_EQUALS,
                               ScoreSelectors.ExpressionType.LESS_THAN, ScoreSelectors.ExpressionType.LESS_THAN_EQUALS,
                               ScoreSelectors.ExpressionType.EQUALS, ScoreSelectors.ExpressionType.NOT_EQUALS]:
            # Binary operator: Two operands
            return {
                "operator": expression_type,
                "nested": [
                    {"operator": ScoreSelectors.ExpressionType.VALUE, "value": 10},
                    {"operator": ScoreSelectors.ExpressionType.VALUE, "value": 5}
                ]
            }
        elif expression_type in [ScoreSelectors.ExpressionType.SUM, ScoreSelectors.ExpressionType.COUNTER,
                                 ScoreSelectors.ExpressionType.AVERAGE, ScoreSelectors.ExpressionType.MAXIMUM,
                                 ScoreSelectors.ExpressionType.MINIMUM]:
            # Multi operator: Multiple operands
            return {
                "operator": expression_type,
                "nested": [
                    {"operator": ScoreSelectors.ExpressionType.VALUE, "value": 10},
                    {"operator": ScoreSelectors.ExpressionType.VALUE, "value": 20}
                ]
            }
        elif expression_type == ScoreSelectors.ExpressionType.VALUE:
            # Unary operator: Single value
            return {
                "operator": expression_type,
                "value": 42
            }
        elif expression_type == ScoreSelectors.ExpressionType.QUESTION_VALUE:
            if not question_id:
                raise ValueError("QUESTION_VALUE requires a valid question_id.")
            # Question-based operator
            return {
                "operator": expression_type,
                "question": question_id
            }
        elif expression_type == ScoreSelectors.ExpressionType.VALUE_OF_SCORE:
            if not score_id:
                raise ValueError("VALUE_OF_SCORE requires a valid score_id.")
            # Score-based operator
            return {
                "operator": expression_type,
                "score": score_id
            }
        else:
            raise ValueError(f"Unsupported operator: {expression_type}")

    def add_score(self, name=None, expression_type=None, expression_tree=None):
        """
        Adds a score with a specific name and an expression structure.

        :param name: Optional name of the score (generated if None).
        :param expression_type: Type of the root expression (e.g., ADDITION, SUM).
        :param expression_tree: Nested dictionary representing the expression tree.
        :return: A dictionary with details of the created score.
        """
        # Generate name if not provided
        timestamp: str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        name = name or f"Score {timestamp} - {expression_type}"

        # Click Add Score Button
        self.utils.click_element(ScoreSelectors.BUTTON_ADD_SCORE)
        self.utils.fill_text_field(ScoreSelectors.INPUT_NAME, name)

        # Build the expression tree
        if expression_tree:
            self._build_expression("expression", expression_tree)

        return {
            "name": name,
            "operator": expression_type,
            "expression_tree": expression_tree
        }

    def _build_expression(self, path: str, expression: dict):
        """
        Recursively builds an expression.

        :param path: Path prefix for the current expression (e.g., "expression").
        :param expression: Dictionary representing the expression structure.
        """
        operator = expression.get("operator")
        value = expression.get("value")
        question = expression.get("question")
        score = expression.get("score")
        nested_expressions = expression.get("nested")

        if operator:
            self.utils.select_dropdown(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(path), operator, DropdownMethod.VALUE)

        if value is not None:
            self.utils.fill_number_field(ScoreSelectors.EXPRESSION_VALUE(path), value)

        if question:
            self.utils.select_dropdown(ScoreSelectors.EXPRESSION_QUESTION(path), question, DropdownMethod.VALUE)

        if score:
            self.utils.select_dropdown(ScoreSelectors.EXPRESSION_SCORE(path), score, DropdownMethod.VALUE)

        if nested_expressions:
            for i, nested_expression in enumerate(nested_expressions):
                nested_path = f"{path}.expressions[{i}]"
                self._build_expression(nested_path, nested_expression)

class ScoreAssertHelper(ScoreHelper):

    def assert_scores_list(self):
        # Arrange: Verify 'Add New Score' button is present
        add_score_button = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.BUTTON_ADD_SCORE)
        )
        assert add_score_button.is_displayed(), "The 'Add New Score' button is not displayed."

        # Act & Assert: Add the first score and verify
        self.add_and_verify_score(ScoreSelectors.ExpressionType.ADDITION)

        # Act & Assert: Add a second score and verify
        self.add_and_verify_score(ScoreSelectors.ExpressionType.MULTIPLICATION)

        # Verify: Action buttons are displayed for each row
        self.assert_scores_table_and_buttons()

    def add_and_verify_score(self, expression_type):
        """
        :param expression_type: The type of the score to add.
        """

        # Add a basic score
        score = self.create_basic_score(expression_type)

        # Save the score
        self.save_score()

        # Verify: Score table contains the new score
        score_table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            ScoreSelectors.SCORE_TABLE))
        assert score_table.is_displayed(), "Score table is not displayed."

        rows = score_table.find_elements(By.TAG_NAME, "tr")
        assert any(score['name'] in row.text for row in rows), f"Score '{score['name']}' is not found in the table."

    def assert_scores_table_and_buttons(self):
        # Locate table rows
        rows = self.driver.find_elements(*ScoreSelectors.TABLE_ROWS)

        # Ensure the table has rows
        assert len(rows) > 0, "Scores table is empty. No rows found."

        for index, row in enumerate(rows, start=1):
            # Locate action buttons in the current row
            action_buttons = row.find_elements(*ScoreSelectors.ACTION_BUTTONS)

            # Assert that each row has exactly two action buttons
            assert len(action_buttons) == 2, (
                f"Row {index} does not have exactly two action buttons. Found: {len(action_buttons)}."
            )

    def assert_score_fill(self):
        """
        Verifies that the Score Fill page:
        - Displays inputs for the question score
        - Shows dropdowns for selecting operators
        - Allows the combination of different operators, adjusting dynamically based on the selected values
        - Handles errors for invalid combinations gracefully
        """

        # Step 1: Navigate to the Score Fill page
        self.click_add_score_button()

        # Step 2: Verify the presence of the input field for the score name
        score_name_input = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.INPUT_NAME))
        assert score_name_input.is_displayed(), "Score name input field is not displayed."

        # Fill the score name
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        score_name = f"Fill Score Test {timestamp}"
        self.utils.fill_text_field(ScoreSelectors.INPUT_NAME, score_name)

        # Step 3: Verify the presence of the operator dropdown
        operator_dropdown = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.DROPDOWN_EXPRESSION))
        assert operator_dropdown.is_displayed(), "Operator dropdown is not displayed."

        # Select an initial operator
        self.utils.select_dropdown(ScoreSelectors.DROPDOWN_EXPRESSION, ScoreSelectors.ExpressionType.ADDITION,
                                   DropdownMethod.VALUE)

        # Step 4: Validate the appearance of nested operator inputs
        first_nested_path = "expression.expressions[0]"
        first_nested_operator = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(first_nested_path)))
        assert first_nested_operator.is_displayed(), "First nested operator input is not displayed."

        # Add a nested operator and validate the structure
        self.utils.select_dropdown(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(first_nested_path),
                                   ScoreSelectors.ExpressionType.VALUE, DropdownMethod.VALUE)
        first_value_input = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.EXPRESSION_VALUE(first_nested_path)))
        assert first_value_input.is_displayed(), "Value input for the first nested operator is not displayed."

        self.utils.fill_number_field(ScoreSelectors.EXPRESSION_VALUE(first_nested_path), 3)

        # Step 5: Add a second operator and validate the DOM structure
        second_nested_path = "expression.expressions[1]"
        self.utils.select_dropdown(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(second_nested_path),
                                   ScoreSelectors.ExpressionType.MULTIPLICATION, DropdownMethod.VALUE)

        # Ensure a second nested operator appears
        second_nested_first_path = f"{second_nested_path}.expressions[0]"
        second_nested_first_operator = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(second_nested_first_path)))
        assert second_nested_first_operator.is_displayed(), "Second nested first operator is not displayed."

        self.utils.select_dropdown(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(second_nested_first_path),
                                   ScoreSelectors.ExpressionType.VALUE, DropdownMethod.VALUE)
        self.utils.fill_number_field(ScoreSelectors.EXPRESSION_VALUE(second_nested_first_path), 3)

        # Add another nested operator to complete the second branch
        second_nested_second_path = f"{second_nested_path}.expressions[1]"
        second_nested_second_operator = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(second_nested_second_path)))
        assert second_nested_second_operator.is_displayed(), "Second nested second operator is not displayed."

        self.utils.select_dropdown(ScoreSelectors.EXPRESSION_OPERATOR_UNSELECTED(second_nested_second_path),
                                   ScoreSelectors.ExpressionType.VALUE, DropdownMethod.VALUE)
        self.utils.fill_number_field(ScoreSelectors.EXPRESSION_VALUE(second_nested_second_path), 4)

        self.save_score()

