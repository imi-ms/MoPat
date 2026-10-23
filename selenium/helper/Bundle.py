import datetime

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SearchBoxSelectors
from helper.SeleniumUtils import SeleniumUtils

# Root of the questionnaire assignment list fragment on bundle/edit.html.
# Defined at module level because lambdas inside a class body cannot
# access other class attributes.
_QUESTIONNAIRE_LIST_ROOT = "div.assignment-list[data-component-id='bundle-questionnaires']"
_QUESTIONNAIRE_ASSIGNED_ITEM = _QUESTIONNAIRE_LIST_ROOT + " .assignment-list__assigned > .assignment-list-item[data-item-id='{}']"


class BundleSelectors:
    BUTTON_ADD_BUNDLE = (By.ID, "addBundle")
    BUTTON_SAVE = (By.ID, "saveButton")

    CHECKBOX_PUBLISH = (By.ID, "isPublished1")
    CHECKBOX_NAME_PROGRESS = (By.ID, "deactivateProgressAndNameDuringSurvey1")
    CHECKBOX_PROGRESS_WHOLE_PACKAGE = (By.ID, "showProgressPerBundle1")

    INPUT_NAME = (By.ID, "name")
    INPUT_EDITABLE_DESCRIPTION = (By.CSS_SELECTOR, "div.note-editable")
    INPUT_WELCOME_TEXT = (By.CSS_SELECTOR, "#localizedWelcomeTextCollapsableText_de_DE > div > div.note-editing-area > div.note-editable")
    INPUT_END_TEXT = (By.CSS_SELECTOR, "#localizedFinalTextCollapsableText_de_DE > div:nth-child(2) > div:nth-child(2) > div:nth-child(3)")
    INPUT_BUNDLE_SEARCH = (By.ID, "bundleTable_filter")

    # Questionnaire assignment list (fragments/assignmentList.html, componentId 'bundle-questionnaires')
    QUESTIONNAIRE_ASSIGNMENT_LIST = (By.CSS_SELECTOR, _QUESTIONNAIRE_LIST_ROOT)
    BUTTON_OPEN_QUESTIONNAIRE_MODAL = (By.CSS_SELECTOR, f"{_QUESTIONNAIRE_LIST_ROOT} .assignment-list__open-modal")
    MODAL_QUESTIONNAIRE_SELECTION = (By.CSS_SELECTOR, f"{_QUESTIONNAIRE_LIST_ROOT} .assignment-list__modal")
    INPUT_QUESTIONNAIRE_MODAL_SEARCH = (By.CSS_SELECTOR, f"{_QUESTIONNAIRE_LIST_ROOT} .assignment-list__filter")
    CHECKBOX_QUESTIONNAIRE = lambda questionnaire_id: (By.ID, f"bundle-questionnaires-select-item-{questionnaire_id}")
    BUTTON_ADD_SELECTED_QUESTIONNAIRES = (By.CSS_SELECTOR, f"{_QUESTIONNAIRE_LIST_ROOT} .assignment-list__add-selected")
    ASSIGNED_QUESTIONNAIRE = lambda questionnaire_id: (By.CSS_SELECTOR, _QUESTIONNAIRE_ASSIGNED_ITEM.format(questionnaire_id))
    BUTTON_REMOVE_QUESTIONNAIRE = lambda questionnaire_id: (By.CSS_SELECTOR, _QUESTIONNAIRE_ASSIGNED_ITEM.format(questionnaire_id) + " .remove-assignment-item-button")

    BUNDLE_LINK = lambda bundle_id: (By.ID, f"bundleLink_{bundle_id}")
    BUNDLE_LINK_BY_NAME = lambda bundle_name: (By.XPATH, f"//table[@id='bundleTable']//a[text()='{bundle_name}']")

    TABLE_ROWS = (By.CSS_SELECTOR, "#DataTables_Table_0 tbody tr")
    TABLE_BUNDLE = (By.ID, "bundleTable")

    CELL_FLAGICON=(By.CSS_SELECTOR, "#bundleTable > tbody > tr > td:nth-child(2) > img")
    PAGINATION_BUNDLE = (By.CSS_SELECTOR, "#bundleTable_paginate")

class BundleHelper:
    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver)

    def bundle_exists(self, bundle_name):
        """
        :param bundle_name: Name of the bundle to search for.
        :return: True if the bundle exists, False otherwise.
        """
        try:
            # Fill the search box with the bundle name
            self.utils.fill_text_field(BundleSelectors.INPUT_BUNDLE_SEARCH, bundle_name)

            # Check if the bundle link with the specified name exists
            WebDriverWait(self.driver, 2).until(
                EC.presence_of_element_located(BundleSelectors.BUNDLE_LINK_BY_NAME(bundle_name))
            )
            return True
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

            bundle_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                BundleSelectors.BUNDLE_LINK_BY_NAME(bundle_name)))
            bundle_link.click()

        except TimeoutException:
            raise Exception(f"Timeout while searching for bundle '{bundle_name}' to open.")
        except Exception as e:
            raise Exception(f"Error while opening bundle '{bundle_name}': {e}")

    def create_bundle(self, bundle_name=None, publish_bundle=False, questionnaires=None):
        """
        :param bundle_name: Name of the bundle to create.
        :param publish_bundle: Whether to publish the bundle.
        :param questionnaires: List of questionnaires to assign to the bundle (optional).
        """
        timestamp: str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        bundle_name = bundle_name or f"Test Bundle {timestamp}"

        try:
            self.navigation_helper.navigate_to_manage_bundles()

            self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)

            # Add name
            self.utils.fill_text_field(BundleSelectors.INPUT_NAME, bundle_name)

            # Add description
            description_field = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                BundleSelectors.INPUT_EDITABLE_DESCRIPTION))

            self.utils.scroll_to_element(BundleSelectors.INPUT_EDITABLE_DESCRIPTION)
            description_field.click()
            description_field.send_keys("This bundle contains questionnaires.")

            # Assign questionnaires if provided
            # (must happen before publishing: the publish checkbox is disabled while the list is empty)
            if questionnaires:
                self.assign_multiple_questionnaires_to_bundle(questionnaires)

            # Publish the bundle if requested
            if publish_bundle:
                self.utils.toggle_checkbox(BundleSelectors.CHECKBOX_PUBLISH, enable=True)

            return {
                "name": bundle_name
            }

        except Exception as e:
            raise Exception(f"Error while creating bundle '{bundle_name}': {e}")

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
            bundle_link = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                (By.LINK_TEXT, bundle_name)))

            return bundle_link.get_attribute("href").split("id=")[1]
        except Exception as e:
            raise Exception(f"Error while saving bundle '{bundle_name}': {e}")

    def assign_multiple_questionnaires_to_bundle(self, questionnaires, bundle_id=None):
        """
        Opens the questionnaire selection modal, selects all given questionnaires and adds them in one go.

        :param questionnaires: List of dictionaries containing questionnaire details (e.g., {'id': str, 'name': str}).
        :param bundle_id: ID of the bundle to which the questionnaires will be assigned (optional).
        """
        try:
            if bundle_id:
                self.search_and_open_bundle(bundle_id)

            # Open the selection modal
            self.utils.scroll_to_element(BundleSelectors.BUTTON_OPEN_QUESTIONNAIRE_MODAL)
            WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                BundleSelectors.BUTTON_OPEN_QUESTIONNAIRE_MODAL)).click()
            WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                BundleSelectors.MODAL_QUESTIONNAIRE_SELECTION))

            # Select every questionnaire (filter first so the row is in view)
            for questionnaire in questionnaires:
                try:
                    self.utils.fill_text_field(BundleSelectors.INPUT_QUESTIONNAIRE_MODAL_SEARCH, questionnaire['name'])
                    WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                        BundleSelectors.CHECKBOX_QUESTIONNAIRE(questionnaire['id']))).click()
                except TimeoutException:
                    raise Exception(f"Timeout while selecting questionnaire '{questionnaire['name']}'.")

            # Add the selection and wait until the modal is closed
            WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                BundleSelectors.BUTTON_ADD_SELECTED_QUESTIONNAIRES)).click()
            WebDriverWait(self.driver, 10).until(EC.invisibility_of_element_located(
                BundleSelectors.MODAL_QUESTIONNAIRE_SELECTION))

            # Verify that every questionnaire is now in the assigned list
            for questionnaire in questionnaires:
                try:
                    WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                        BundleSelectors.ASSIGNED_QUESTIONNAIRE(questionnaire['id'])))
                except TimeoutException:
                    raise Exception(f"Questionnaire '{questionnaire['name']}' did not appear in the assigned list.")
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

    def remove_multiple_questionnaires_from_bundle(self, questionnaires, bundle_id=None):
        """
        Removes questionnaires via the trash button in the assigned list.

        :param questionnaires: List of dictionaries containing questionnaire details (e.g., {'id': str, 'name': str}).
        :param bundle_id: ID of the bundle from which the questionnaires will be removed (optional).
        """
        try:
            if bundle_id:
                self.search_and_open_bundle(bundle_id)

            for questionnaire in questionnaires:
                try:
                    self.utils.scroll_to_element(BundleSelectors.BUTTON_REMOVE_QUESTIONNAIRE(questionnaire['id']))
                    WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                        BundleSelectors.BUTTON_REMOVE_QUESTIONNAIRE(questionnaire['id']))).click()
                    WebDriverWait(self.driver, 10).until(EC.invisibility_of_element_located(
                        BundleSelectors.ASSIGNED_QUESTIONNAIRE(questionnaire['id'])))
                except TimeoutException:
                    raise Exception(f"Timeout while removing questionnaire '{questionnaire['name']}'.")
        except Exception as e:
            raise Exception(f"Error while removing multiple questionnaires: {e}")