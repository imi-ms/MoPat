from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils

from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

class LanguageSelectors:
    LANGUAGE_DROPDOWN_BUTTON = (By.CSS_SELECTOR, "#languageDropdown > a")
    LANGUAGE_DROPDOWN = (By.ID, "addLanguageDropdown")

class LanguageHelper:
    def __init__(self, driver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver, navigation_helper)

    def open_language_dropdown(self):
        try:
            self.utils.click_element(LanguageSelectors.LANGUAGE_DROPDOWN_BUTTON)
        except Exception as e:
            raise Exception(f"Failed to open language dropdown: {e}")
    