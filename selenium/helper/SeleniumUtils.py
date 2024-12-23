import time

from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, ElementClickInterceptedException
from selenium.webdriver.support.ui import Select
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains

class RemoveButtonSelectors:
    QUESTIONNAIRE = "removeQuestionnaire_{}"
    BUNDLE = "removeBundle_{}"
    CLINIC = "removeClinic_{}"

class SearchBoxSelectors:
    QUESTIONNAIRE = (By.CSS_SELECTOR, "#questionnaireTable_filter input[type='search']")
    BUNDLE = (By.CSS_SELECTOR, "#bundleTable_filter input[type='search']")
    CLINIC = (By.CSS_SELECTOR, "#clinicTable_filter input[type='search']")

class SeleniumUtils:
    def __init__(self, driver, navigator=None):
        self.driver = driver
        self.navigator = navigator

    def click_element(self, selector):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        """
        try:
            element = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(selector))
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
            text_field = WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(selector)
            )

            text_field.clear()
            text_field.send_keys(text)
        except TimeoutException:
            raise Exception(f"Timeout while waiting for text field {selector} to be visible.")
        except Exception as e:
            raise Exception(f"Failed to fill text field {selector}: {e}")

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

    def scroll_to_element(self, selector):
        """
        :param selector: A tuple representing the element locator (e.g., (By.ID, "element_id")).
        """
        try:
            element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(selector))
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)
        except TimeoutException:
            raise Exception(f"Timeout while trying to scroll to element {selector}.")
        except Exception as e:
            raise Exception(f"Failed to scroll to element {selector}: {e}")

    def toggle_checkbox(self, selector, enable=True):
        """
        :param selector: Locator of the checkbox element.
        :param enable: True to enable the checkbox, False to disable it.
        """
        try:
            checkbox = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(selector)
            )
            self.scroll_to_element(selector)
            if checkbox.is_selected() != enable:
                self.click_element(selector)
        except TimeoutException:
            raise Exception(f"Timeout while waiting for checkbox {selector} to be present.")
        except Exception as e:
            raise Exception(f"Error while toggling checkbox {selector}: {e}")

    def select_dropdown(self, selector, value, method="visible_text"):
        """
        :param selector: The locator tuple for the dropdown element.
        :param value: The value or visible text of the option to select.
        :param method: The selection method, either "visible_text" or "value".
        """
        try:
            dropdowns = WebDriverWait(self.driver, 10).until(
                EC.presence_of_all_elements_located(selector)
            )

            # Filter visible dropdowns
            visible_dropdown = next(el for el in dropdowns if el.is_displayed())

            # Initialize the Select object with the visible dropdown
            select = Select(visible_dropdown)

            if method == "visible_text":
                select.select_by_visible_text(value)
            elif method == "value":
                select.select_by_value(value)
            else:
                raise ValueError(f"Invalid method '{method}'. Use 'visible_text' or 'value'.")
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
            table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located((by, value)))
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

