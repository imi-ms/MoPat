import datetime

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SearchBoxSelectors
from helper.SeleniumUtils import SeleniumUtils


class DashboardSelectors:
    BUTTON_DASHBOARD = (By.ID, "adminPanelButton")
    TABLE_GITINFO = (By.ID, "gitInformationTable")
    BLOCK_GIT_BUILD_VERSION = (By.ID, "gitBuildVersion")
    BLOCK_GIT_BRANCH = (By.ID, "gitBranch")
    BLOCK_GIT_COMMIT_ID = (By.ID, "gitCommitId")
    BLOCK_GIT_COMMIT_MESSAGE = (By.ID, "gitCommitMessage")  

class DashboardHelper:
    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver)
    
    def open_git_info(self):
        self.utils.click_element(DashboardSelectors.BUTTON_DASHBOARD)

        