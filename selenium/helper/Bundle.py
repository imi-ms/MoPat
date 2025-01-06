import time

from selenium.common import TimeoutException, ElementClickInterceptedException, NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from helper.SeleniumUtils import SearchBoxSelectors
from helper.SeleniumUtils import SeleniumUtils


class BundleSelectors:
    BUTTON_ADD_BUNDLE = (By.ID, "addBundle")
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_MOVE_ITEM = lambda questionnaire_id: (By.ID, f"move_{questionnaire_id}")

    CHECKBOX_PUBLISH = (By.ID, "isPublished1")

    INPUT_NAME = (By.ID, "name")
    INPUT_EDITABLE_DESCRIPTION = (By.CSS_SELECTOR, "div.note-editable")
    INPUT_WELCOME_TEXT = (By.CSS_SELECTOR, "#localizedWelcomeTextCollapsableText_de_DE > div > div.note-editing-area > div.note-editable")
    INPUT_BUNDLE_SEARCH = (By.ID, "bundleTable_filter")
    INPUT_QUESTIONNAIRE = lambda questionnaire_id: (By.ID, f"questionnaire_{questionnaire_id}")
    INPUT_QUESTIONNAIRE_AVAILABLE_SEARCH = (By.ID, "availableQuestionnairesFilter")
    TABLE_BUNDLE = (By.ID, "bundleTable")
    PAGINATION_BUNDLE = (By.CSS_SELECTOR, "#bundleTable_paginate")
    BUTTON_ADD_LANGUAGE = (By.CSS_SELECTOR, "#languageDropdown > a")

class BundleHelper:
    def __init__(self, driver, fragebogen_helper, navigation_helper):
        self.driver = driver
        self.fragebogen_helper = fragebogen_helper
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver)

    def bundle_exists(self, bundle_name):
        """
        :param bundle_name: Name of the bundle to search for.
        :return: True if the bundle exists, False otherwise.
        """
        try:
            # Fill the search box with the bundle name
            self.utils.fill_text_field(SearchBoxSelectors.BUNDLE, bundle_name)

            # Wait until the table rows are present (or timeout if none are found)
            rows = WebDriverWait(self.driver, 2).until(
                EC.presence_of_all_elements_located((By.CSS_SELECTOR, "#DataTables_Table_0 tbody tr"))
            )

            # Check if the first row has more than one <td> (indicating a valid result)
            first_row_columns = rows[0].find_elements(By.TAG_NAME, "td") if rows else []
            return len(first_row_columns) > 1
        except TimeoutException:
            # No rows found within the timeout
            return False
        except Exception as e:
            raise Exception(f"Error while checking if bundle '{bundle_name}' exists: {e}")

    def search_and_open_bundle(self, bundle_name):
        """
        :param bundle_name: Name of the bundle to search for.
        """
        try:
            self.utils.fill_text_field(SearchBoxSelectors.BUNDLE, bundle_name)
            row_hyperlink = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, "#DataTables_Table_0 tbody tr td a"))
            )
            self.utils.click_element((By.CSS_SELECTOR, "#DataTables_Table_0 tbody tr td a"))
        except TimeoutException:
            raise Exception(f"Timeout while searching for bundle '{bundle_name}' to open.")
        except Exception as e:
            raise Exception(f"Error while opening bundle '{bundle_name}': {e}")

    def create_bundle(self, bundle_name, publish_bundle, questionnaires=None):
        """
        :param bundle_name: Name of the bundle to create.
        :param publish_bundle: Whether to publish the bundle.
        :param questionnaires: List of questionnaires to assign to the bundle (optional).
        """
        try:
            self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)

            # Add name
            self.utils.fill_text_field(BundleSelectors.INPUT_NAME, bundle_name)

            # Add description
            description_field = WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_EDITABLE_DESCRIPTION)
            )
            self.utils.scroll_to_element(BundleSelectors.INPUT_EDITABLE_DESCRIPTION)
            description_field.click()
            description_field.send_keys("This bundle contains questionnaires.")

            # Assign questionnaires if provided
            if questionnaires:
                self.assign_multiple_questionnaires_to_bundle(questionnaires)

            # Publish the bundle if requested
            if publish_bundle:
                self.utils.scroll_to_element(BundleSelectors.CHECKBOX_PUBLISH)
                self.utils.toggle_checkbox(BundleSelectors.CHECKBOX_PUBLISH, enable=True)

        except Exception as e:
            raise Exception(f"Error while creating bundle '{bundle_name}': {e}")

    def add_language_to_bundle(self):
        """
        :param language: Language to add to the bundle.
        """
        try:
            self.utils.click_element(BundleSelectors.BUTTON_ADD_LANGUAGE)
            self.utils.click_element((By.ID, 'ur_PK'))
        except Exception as e:
            raise Exception(f"Error while adding language to the bundle: {e}")
        

    def save_bundle(self, bundle_name):
        """
        :param bundle_name: Name of the bundle to locate after saving.
        :return: The extracted bundle ID.
        """
        try:
            # Save the bundle
            self.utils.click_element(BundleSelectors.BUTTON_SAVE)

            # Wait for redirection to the bundle list
            WebDriverWait(self.driver, 15).until(EC.url_contains("/bundle/list"))

            # Search for the bundle by name
            self.utils.fill_text_field(SearchBoxSelectors.BUNDLE, bundle_name)

            # Extract the bundle ID from the link
            bundle_link = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.LINK_TEXT, bundle_name))
            )
            return bundle_link.get_attribute("href").split("id=")[1]
        except Exception as e:
            raise Exception(f"Error while saving bundle '{bundle_name}': {e}")

    def assign_multiple_questionnaires_to_bundle(self, questionnaires, bundle_id=None):
        """
        :param questionnaires: List of dictionaries containing questionnaire details (e.g., {'id': str, 'name': str}).
        :param bundle_id: ID of the bundle to which the questionnaires will be assigned (optional).
        """
        try:
            if bundle_id:
                self.search_and_open_bundle(bundle_id)

            for questionnaire in questionnaires:
                try:
                    self.utils.fill_text_field(BundleSelectors.INPUT_QUESTIONNAIRE_AVAILABLE_SEARCH, questionnaire['name'])

                    questionnaire_selector = BundleSelectors.INPUT_QUESTIONNAIRE(questionnaire['id'])
                    WebDriverWait(self.driver, 10).until(
                        EC.presence_of_element_located(questionnaire_selector)
                    )
                    self.utils.scroll_to_element(BundleSelectors.BUTTON_MOVE_ITEM(questionnaire['id']))
                    self.utils.click_element(BundleSelectors.BUTTON_MOVE_ITEM(questionnaire['id']))
                except TimeoutException:
                    raise Exception(f"Timeout while assigning questionnaire '{questionnaire['name']}'.")
                except Exception as e:
                    raise Exception(f"Error while assigning questionnaire '{questionnaire['name']}': {e}")
        except Exception as e:
            raise Exception(f"Error while assigning multiple questionnaires: {e}")
        
    def get_bundle_id(self):
        edit_link = self.get_first_bundle_row().find_element(By.CSS_SELECTOR, "a[href^='fill?id=']")
        href = edit_link.get_attribute('href')
        # Extract ID from href using split
        bundle_id = href.split('id=')[1]
        return bundle_id

    def get_first_bundle_row(self):
        return self.driver.find_element(*BundleSelectors.TABLE_BUNDLE) \
                           .find_element(By.TAG_NAME, "tbody") \
                           .find_element(By.TAG_NAME, "tr")