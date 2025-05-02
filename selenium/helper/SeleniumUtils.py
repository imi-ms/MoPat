from enum import Enum

from selenium.common.exceptions import TimeoutException, ElementClickInterceptedException
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait


class RemoveButtonSelectors:
    QUESTIONNAIRE = "removeQuestionnaire_{}"
    BUNDLE = "removeBundle_{}"
    CLINIC = "removeClinic_{}"

class SearchBoxSelectors:
    QUESTIONNAIRE = (By.CSS_SELECTOR, "#questionnaireTable_filter input[type='search']")
    BUNDLE = (By.CSS_SELECTOR, "#bundleTable_filter input[type='search']")
    CLINIC = (By.CSS_SELECTOR, "#clinicTable_filter input[type='search']")
    SCHEDULED_ENCOUNTER = (By.CSS_SELECTOR, "#encounterScheduled_filter input[type='search']")
    INVITATION = (By.CSS_SELECTOR, "#invitationTable_filter input[type='search']")

class DropdownMethod(Enum):
    VISIBLE_TEXT = "visible_text"
    VALUE = "value"
    INDEX = "index"

class ErrorSelectors:
    INPUT_VALIDATION_SELECTOR=(By.CLASS_NAME, "validationError")
    CONFIGURATION_ERROR_SELECTOR=(By.CLASS_NAME, "configurationError")

class SeleniumUtils:
    def __init__(self, driver, navigation_helper = None):
        self.driver = driver
        self.navigator = navigation_helper

    def click_element(self, selector):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        """
        try:
            element = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(selector))
            self.driver.execute_script("arguments[0].scrollIntoView(true);", element)
            element.click()
        except ElementClickInterceptedException:
            try:
                element = self.driver.find_element(*selector)
                self.driver.execute_script("arguments[0].click();", element)
            except Exception as e:
                raise Exception(f"Failed to click element {selector} using JavaScript: {e}")
        except TimeoutException:
            raise Exception(f"Timeout while waiting for element {selector} to be clickable.")

    def fill_text_field(self, selector, text):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "text_field_id")).
        :param text: The text to input into the text field.
        """
        try:
            text_field = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                selector))

            text_field.clear()
            text_field.send_keys(text)
        except TimeoutException:
            raise Exception(f"Timeout while waiting for text field {selector} to be visible.")
        except Exception as e:
            raise Exception(f"Failed to fill text field {selector}: {e}")

    def fill_number_field(self, selector, number):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "text_field_id")).
        :param number: The number to input into the field.
        """
        try:
            text_field = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                selector))

            text_field.clear()
            text_field.send_keys(number)
        except TimeoutException:
            raise Exception(f"Timeout while waiting for text field {selector} to be visible.")
        except Exception as e:
            raise Exception(f"Failed to fill text field {selector}: {e}")

    def clear_text_field(self, selector):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "text_field_id")).
        """
        try:
            text_field = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                selector))
            text_field.clear()
        except TimeoutException:
            raise Exception(f"Timeout while waiting for text field {selector} to be visible.")
        except Exception as e:
            raise Exception(f"Failed to clear text field {selector}: {e}")

    def fill_editable_div(self, editable_div, text):
        """
        Fills an editable div element (e.g., a WYSIWYG editor) with the specified text.
        :param editable_div: WebElement representing the editable div to be filled.
        :param text: The text to insert into the editable div.
        """
        try:
            # Scroll to the visible area
            self.driver.execute_script("arguments[0].scrollIntoView(true);", editable_div)

            # Wait until the element is clickable
            WebDriverWait(self.driver, 10).until(EC.visibility_of(editable_div))
            WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(editable_div))

            ActionChains(self.driver).move_to_element(editable_div).click().perform()

            # Select all text and delete
            try:
                editable_div.send_keys(Keys.CONTROL + "a")  # Select all (CTRL + A)
                editable_div.send_keys(Keys.DELETE)  # Delete selected text
            except Exception:
                # Fallback: Clear content using JavaScript
                self.driver.execute_script("arguments[0].innerHTML = '';", editable_div)

            # Enter new text
            editable_div.send_keys(text)

        except Exception as e:
            raise Exception(f"Error while filling the editable div: {e}")

    def scroll_and_click(self, selector):
        # Scroll to element
        element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            selector))
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)

        # Wait until the element is clickable
        try:
            WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(selector)).click()
        except ElementClickInterceptedException:
            # Fallback: JavaScript click if the click is blocked
            self.driver.execute_script("arguments[0].click();", element)

    def scroll_to_element(self, selector):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        """
        try:
            element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                selector))
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)
        except TimeoutException:
            raise Exception(f"Timeout while trying to scroll to element {selector}.")
        except Exception as e:
            raise Exception(f"Failed to scroll to element {selector}: {e}")

    def scroll_to_bottom(self):
        try:
            # Wait for the page to fully load
            WebDriverWait(self.driver, 30).until(lambda driver: driver.execute_script("return document.readyState") == "complete")

            # Execute JavaScript to scroll to the bottom
            self.driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")

            # Wait for the scrolling to complete
            WebDriverWait(self.driver, 10).until(
                lambda driver: driver.execute_script(
                    "return window.scrollY + window.innerHeight") >= driver.execute_script(
                    "return document.body.scrollHeight")
            )
        except TimeoutException:
            raise Exception("Timeout while trying to scroll to the bottom of the page.")
        except Exception as e:
            raise Exception(f"Failed to scroll to the bottom of the page: {e}")

    def scroll_to_element_or_bottom(self, selector, timeout=10):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        :param timeout: Maximum time to wait for scrolling to complete.
        """
        try:
            # Wait for the page to fully load
            WebDriverWait(self.driver, 30).until(lambda driver: driver.execute_script("return document.readyState") == "complete")

            # Locate the element
            element = WebDriverWait(self.driver, timeout).until(EC.presence_of_element_located(
                selector))

            # Scroll the element into view
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)

            # Wait until the element is approximately centered or the bottom is reached
            WebDriverWait(self.driver, timeout).until(
                lambda driver: abs(driver.execute_script("return arguments[0].getBoundingClientRect().top;", element)
                ) < driver.execute_script("return window.innerHeight / 2") or
                    driver.execute_script("return window.scrollY + window.innerHeight >= document.body.scrollHeight")
            )
        except TimeoutException:
            raise Exception(
                f"Timeout while trying to scroll to element {selector} or detecting the bottom of the page.")

    def toggle_checkbox(self, selector, enable=True):
        """
        :param selector: Locator of the checkbox element.
        :param enable: True to enable the checkbox, False to disable it.
        """
        try:
            checkbox = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                selector))
            self.scroll_to_element(selector)
            if checkbox.is_selected() != enable:
                self.click_element(selector)
        except TimeoutException:
            raise Exception(f"Timeout while waiting for checkbox {selector} to be present.")
        except Exception as e:
            raise Exception(f"Error while toggling checkbox {selector}: {e}")

    def select_dropdown(self, selector, value, method=DropdownMethod.VISIBLE_TEXT):
        """
        :param selector: The locator tuple for the dropdown element.
        :param value: The value, visible text, or index of the option to select.
        :param method: The selection method, an instance of DropdownMethod Enum.
        """
        try:
            dropdowns = WebDriverWait(self.driver, 10).until(EC.presence_of_all_elements_located(
                selector))

            # Filter visible dropdowns
            visible_dropdown = next(el for el in dropdowns if el.is_displayed())

            # Initialize the Select object with the visible dropdown
            select = Select(visible_dropdown)

            if method == DropdownMethod.VISIBLE_TEXT:
                select.select_by_visible_text(value)
            elif method == DropdownMethod.VALUE:
                select.select_by_value(value)
            elif method == DropdownMethod.INDEX:
                visible_options = [
                    option for option in select.options
                    if option.is_displayed() and not option.get_attribute("disabled")
                ]
                if value < 0 or value >= len(visible_options):
                    raise ValueError(f"Invalid index '{value}'. Must be between 0 and {len(visible_options) - 1}.")
                # Click the selected visible option
                visible_options[value].click()
            else:
                raise ValueError(f"Invalid method '{method}'. Use DropdownMethod Enum values.")
        except TimeoutException:
            raise Exception(f"Timeout while selecting '{value}' in dropdown {selector}.")
        except Exception as e:
            raise Exception(f"Error while selecting '{value}' in dropdown {selector} using method '{method}': {e}")

    def get_visible_table_rows(self, by, value):
        """
        Returns a list of visible rows from a table located by the given selector.
        :param by: Selenium locator strategy (e.g., By.ID).
        :param value: Locator value (e.g., "table_id").
        :return: List of WebElement rows.
        """
        try:
            table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                (by, value)))
            tbody = table.find_element(By.TAG_NAME, "tbody")
            rows = [row for row in tbody.find_elements(By.TAG_NAME, "tr") if row.is_displayed()]
            return rows
        except TimeoutException:
            raise Exception(f"Timeout while locating table rows in table '{value}'.")

    def search_and_delete_item(self, item_name, item_id, item_type):
        """
        :param item_name: The name of the item to search for.
        :param item_id: The unique ID of the item to delete.
        :param item_type: The type of the item to delete (e.g., "questionnaire", "bundle", "clinic").
        """
        try:
            # Initialize variables based on the item type
            if item_type == "questionnaire":
                self.navigator.navigate_to_manage_questionnaires()
                search_box_selector = SearchBoxSelectors.QUESTIONNAIRE
                button_id = RemoveButtonSelectors.QUESTIONNAIRE.format(item_id)
            elif item_type == "bundle":
                self.navigator.navigate_to_manage_bundles()
                search_box_selector = SearchBoxSelectors.BUNDLE
                button_id = RemoveButtonSelectors.BUNDLE.format(item_id)
            elif item_type == "clinic":
                self.navigator.navigate_to_manage_clinics()
                search_box_selector = SearchBoxSelectors.CLINIC
                button_id = RemoveButtonSelectors.CLINIC.format(item_id)
            else:
                raise ValueError(f"Unknown item type: {item_type}")

            # Search for the element using the appropriate search box
            self.fill_text_field(search_box_selector, item_name)

            # click the remove button
            self.click_element((By.ID, button_id))
        except TimeoutException:
            raise Exception(f"Failed to delete {item_type} '{item_name}' with ID {item_id}'.")
        except Exception as e:
            raise Exception(f"An error occurred while deleting {item_type} '{item_name}': {e}")

    def handle_popup_alert(self, accept=True, timeout=2):
        """
        :param accept: Boolean indicating whether to accept (True) or dismiss (False) the alert.
        :param timeout: Time to wait for the alert to appear.
        """
        try:
            WebDriverWait(self.driver, timeout).until(EC.alert_is_present())
            alert = self.driver.switch_to.alert
            if accept:
                alert.accept()
            else:
                alert.dismiss()
        except TimeoutException:
            pass  # No alert appeared

    def set_value(self, field_name, steps=1):
        """
        :param field_name: The name of the input field.
        :param steps: Number of times to press the ARROW_UP key (default is 1).
        """
        value_input = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
            (By.NAME, field_name)))
        value_input.click()
        for _ in range(steps):
            value_input.send_keys(Keys.ARROW_UP)


    def drag_and_drop(self, source_selector, target_selector):
        """
        :param source_selector: A tuple (By, value) identifying the source element to be dragged.
        :param target_selector: A tuple (By, value) identifying the target element where the source should be dropped.
        :raises TimeoutException: If either the source or target element is not found within the timeout.
        """
        # Wait for the source and target elements to be present
        source_element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            source_selector))
        target_element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            target_selector))

        # Scroll the elements into view to ensure they are interactable
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", source_element)
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", target_element)

        # Perform the drag-and-drop action
        ActionChains(self.driver).drag_and_drop(source_element, target_element).perform()

    def search_item(self, item_name, item_type):
        """
        :param item_name: The name of the item to search for.
        :param item_type: The type of the item to delete (e.g., "questionnaire", "bundle", "clinic").
        """
        try:
            # Initialize variables based on the item type
            if item_type == "questionnaire":
                self.navigator.navigate_to_manage_questionnaires()
                search_box_selector = SearchBoxSelectors.QUESTIONNAIRE
            elif item_type == "bundle":
                self.navigator.navigate_to_manage_bundles()
                search_box_selector = SearchBoxSelectors.BUNDLE
            elif item_type == "clinic":
                self.navigator.navigate_to_manage_clinics()
                search_box_selector = SearchBoxSelectors.CLINIC
            else:
                raise ValueError(f"Unknown item type: {item_type}")

            # Search for the element using the appropriate search box
            self.fill_text_field(search_box_selector, item_name)

        except Exception as e:
            raise Exception(f"An error occurred while searching for {item_type} '{item_name}'")
        
    def check_visibility_of_element(self, selector, error_message):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        :param error_message: The error message to display when the element is not visible.
        """
        try:
            WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(selector))
        except TimeoutException:
            raise Exception(error_message)
        
    def check_presence_of_element(self, selector, error_message): 
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        :param error_message: The error message to display when the element is not visible.
        """
        try:
            WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(selector))
        except TimeoutException:
            raise Exception(error_message)
        
    def check_interactability_of_element(self, selector, error_message): 
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        :param error_message: The error message to display when the element is not visible.
        """
        try:
            WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(selector))
        except TimeoutException:
            raise Exception(error_message)