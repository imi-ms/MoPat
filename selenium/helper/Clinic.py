from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SearchBoxSelectors, DropdownMethod
from helper.SeleniumUtils import SeleniumUtils

class URLPathsClinic:
    CLINIC_LIST = "/clinic/list"

class ClinicSelectors:
    BUTTON_ADD_CLINIC = (By.ID, "addClinic")
    BUTTON_SAVE = (By.ID, "saveButton")
    BUTTON_DELETE_CLINIC = lambda clinic_id: (By.ID, f"removeClinic_{clinic_id}")
    BUTTON_MOVE_ITEM = lambda bundle_id: (By.ID, f"move_{bundle_id}")

    DROPDOWN_CONFIG = lambda parent_id: (By.CSS_SELECTOR, f"li[parentid='{parent_id}'] select")

    INPUT_CLINIC_NAME = (By.ID, "name")
    INPUT_EDITABLE_DESCRIPTION = (By.CSS_SELECTOR, "div.note-editable")
    INPUT_USER_AVAILABLE_SEARCH = (By.ID, "availableUsersFilter")
    INPUT_USER = lambda username: (By.ID, f"user_{username}")
    INPUT_BUNDLE_AVAILABLE_SEARCH = (By.ID, "availableBundlesFilter")
    INPUT_BUNDLE = lambda bundle_id: (By.ID, f"bundle_{bundle_id}")

    # Configuration selectors
    CONFIG_USE_PATIENT_DATA_LOOKUP = (By.CSS_SELECTOR, "li#usePatientDataLookup input[type='checkbox']")
    CONFIG_USE_PSEUDONYMIZATION_SERVICE = (By.CSS_SELECTOR, "li#usePseudonymizationService input[type='checkbox']")
    CONFIG_REGISTER_PATIENT_DATA = (By.CSS_SELECTOR, "li#registerPatientData input[type='checkbox']")

    TABLE_ROW_LINK = (By.CSS_SELECTOR, "#clinicTable > tbody > tr > td.dtr-control > a")

class ClinicHelper:
    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver)

    def create_clinic(self, clinic_name, clinic_description, configurations=None, bundles=None, users=None):
        """
        :param clinic_name: Name of the clinic to be created.
        :param clinic_description: Description of the clinic.
        :param configurations: List of configurations to enable. Each configuration is a dict with:
        - 'selector': The selector for the configuration checkbox.
        - 'dropdown_value': Optional dropdown value to select for the configuration (default: None).
        :param bundles: List of bundles to be associated with the clinic (optional).
        """
        try:
            # Click "Add Clinic" button
            self.utils.click_element(ClinicSelectors.BUTTON_ADD_CLINIC)

            # Fill in the clinic name
            self.utils.fill_text_field(ClinicSelectors.INPUT_CLINIC_NAME, clinic_name)

            # Add description
            description_field = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                ClinicSelectors.INPUT_EDITABLE_DESCRIPTION))
            description_field.click()          #  self.utils.click_element(ClinicSelectors.INPUT_EDITABLE_DESCRIPTION)
            description_field.send_keys(clinic_description)

            # Configure settings if provided
            if configurations:
                for config in configurations:
                    selector = config.get('selector')
                    dropdown_value = config.get('dropdown_value')
                    if selector:
                        self.configure_clinic_setting(selector, dropdown_value)

            # Assign bundles if provided
            if bundles:
                self.assign_multiple_bundes_to_clinic(bundles)

            # Assign users if provided
            if users:
                self.assign_multiple_users_to_clinic(users)

        except Exception as e:
            raise Exception(f"Error while creating clinic '{clinic_name}': {e}")

    def save_clinic(self, clinic_name):
        """
        :param clinic_name: Name of the clinic being saved.
        """
        try:
            # Save the clinic
            self.utils.click_element(ClinicSelectors.BUTTON_SAVE)

            # Search for the bundle by name
            self.utils.fill_text_field(SearchBoxSelectors.CLINIC, clinic_name)

            # Extract the bundle ID from the link
            clinic_link = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                (By.LINK_TEXT, clinic_name)))

            return clinic_link.get_attribute("href").split("id=")[1]
        except Exception as e:
            raise Exception(f"Error while saving clinic '{clinic_name}': {e}")

    def delete_clinic(self, clinic_id):
        """
        Deletes a clinic by its ID.

        :param clinic_id: ID of the clinic to be deleted.
        :return: True if the clinic was successfully deleted, False if it does not exist.
        """
        try:
            # Navigate to "Manage Clinics"
            self.navigation_helper.navigate_to_manage_clinics()

            # Wait for and click the delete button for the specified clinic
            delete_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ClinicSelectors.BUTTON_DELETE_CLINIC(clinic_id)))
            delete_button.click()
            return True
        except TimeoutException:
            # Clinic not found
            return False
        except Exception as e:
            raise Exception(f"Error while deleting clinic with ID '{clinic_id}': {e}")

    def configure_clinic_setting(self, config_selector, config_dropdown_value=None):
        """
        :param config_selector: The selector for the configuration element (e.g., ClinicSelectors.CONFIG_USE_PATIENT_DATA_LOOKUP).
        :param config_dropdown_value: Optional dropdown value to select for the configuration.
        """
        try:
            # Enable the checkbox for the configuration
            checkbox = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                config_selector))

            self.utils.scroll_to_element(config_selector)
            self.utils.toggle_checkbox(config_selector, enable=True)

            # Select the value from the dropdown if specified
            if config_dropdown_value:
                parent_id = checkbox.get_attribute("triggerid")
                self.utils.select_dropdown(ClinicSelectors.DROPDOWN_CONFIG(parent_id), config_dropdown_value, DropdownMethod.VALUE)
        except Exception as e:
            raise Exception(
                f"Error configuring setting '{config_selector}' with dropdown value '{config_dropdown_value}': {e}")

    def assign_multiple_bundes_to_clinic(self, bundles, clinic_id=None):
        """
        :param bundles: List of dictionaries containing bundle details (e.g., {'id': str, 'name': str}).
        :param clinic_id: ID of the clinic to which the bundles will be assigned (optional).
        """
        try:
            if clinic_id:
                self.search_and_open_clinic(clinic_id)

            for bundle in bundles:
                try:
                    self.utils.fill_text_field(ClinicSelectors.INPUT_BUNDLE_AVAILABLE_SEARCH, bundle['name'])

                    WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                        ClinicSelectors.INPUT_BUNDLE(bundle['id'])))

                    self.utils.scroll_to_element(ClinicSelectors.BUTTON_MOVE_ITEM(bundle['id']))
                    self.utils.click_element(ClinicSelectors.BUTTON_MOVE_ITEM(bundle['id']))
                except TimeoutException:
                    raise Exception(f"Timeout while assigning bundle '{bundle['name']}'.")
                except Exception as e:
                    raise Exception(f"Error while assigning bundle '{bundle['name']}': {e}")
        except Exception as e:
            raise Exception(f"Error while assigning multiple bundles: {e}")

    def assign_multiple_users_to_clinic(self, usernames, clinic_id=None):
        """
        :param usernames: List of usernames.
        :param clinic_id: ID of the clinic to which the bundles will be assigned (optional).
        """
        try:
            if clinic_id:
                self.search_and_open_clinic(clinic_id)

            for username in usernames:
                try:
                    self.utils.fill_text_field(ClinicSelectors.INPUT_USER_AVAILABLE_SEARCH, username)

                    user_selector = ClinicSelectors.INPUT_USER(username)
                    WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                        user_selector))
                    self.utils.scroll_to_element(ClinicSelectors.BUTTON_MOVE_ITEM(username))
                    self.utils.click_element(ClinicSelectors.BUTTON_MOVE_ITEM(username))
                except TimeoutException:
                    raise Exception(f"Timeout while assigning user '{username}'.")
                except Exception as e:
                    raise Exception(f"Error while assigning user '{username}': {e}")
        except Exception as e:
            raise Exception(f"Error while assigning multiple users: {e}")

    def search_and_open_clinic(self, clinic_name):
        """
        :param clinic_name: Name of the clinic to search for.
        """
        # Navigate to the clinic management page if not already there
        if URLPathsClinic.CLINIC_LIST not in self.driver.current_url:
            self.navigation_helper.navigate_to_manage_clinics()

        try:
            # Fill the search box with the clinic name
            self.utils.fill_text_field(SearchBoxSelectors.CLINIC, clinic_name)

            # Wait for the clinic link to be clickable
            clinic_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                ClinicSelectors.TABLE_ROW_LINK))

            # Click the clinic link
            clinic_link.click()
        except TimeoutException:
            raise Exception(f"Timeout while searching for clinic '{clinic_name}' to open.")
        except Exception as e:
            raise Exception(f"Error while opening clinic '{clinic_name}': {e}")