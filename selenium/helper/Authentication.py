from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class URLPathsAuthentication:
    MOBILE_USER_LOGIN = "/mobile/user/login"


class AuthenticationSelectors:

    class Login:
        BUTTON = (By.ID, "loginButton")
        USERNAME = (By.ID, "username")
        PASSWORD = (By.ID, "password")

    class Logout:
        ADMIN_BUTTON = (By.ID, "adminLogoutButton")
        MOBILE_BUTTON = (By.ID, "logoutButton")

class AuthenticationHelper:

    def __init__(self, driver):
        self.driver = driver

    def login(self, username, password):
        """
        :param username: The username for login.
        :param password: The password for login.
        """
        username_input = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(AuthenticationSelectors.Login.USERNAME)
        )
        username_input.clear()
        username_input.send_keys(username)

        password_input = self.driver.find_element(*AuthenticationSelectors.Login.PASSWORD)
        password_input.clear()
        password_input.send_keys(password)

        submit_button = WebDriverWait(self.driver, 10).until(
            EC.element_to_be_clickable(AuthenticationSelectors.Login.BUTTON)
        )
        submit_button.click()

    def admin_login(self, username=None, password=None):
        username = username or ""
        password = password or ""
        self.login(username, password)

    def user_login(self, username=None, password=None):
        username = username or ""
        password = password or ""
        self.login(username, password)

    def logout(self):
        current_url = self.driver.current_url
        # Check if already logged out
        if URLPathsAuthentication.MOBILE_USER_LOGIN in current_url:
            return

        logout_selectors = [
            AuthenticationSelectors.Logout.ADMIN_BUTTON,
            AuthenticationSelectors.Logout.MOBILE_BUTTON
        ]
        for selector in logout_selectors:
            try:
                logout_button = WebDriverWait(self.driver, 5).until(
                    EC.element_to_be_clickable(selector)
                )
                logout_button.click()
                return
            except:
                continue