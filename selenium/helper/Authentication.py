from selenium.common import TimeoutException, NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from datetime import datetime

class URLPathsAuthentication:
    MOBILE_USER_LOGIN = "/mobile/user/login"
    FORGOT_PASSWORD_PATH = "/mobile/user/password"

class AuthenticationSelectors:

    class Login:
        BUTTON = (By.ID, "loginButton")
        USERNAME = (By.ID, "username")
        PASSWORD = (By.ID, "password")
        LANGUAGE_SELECT = (By.ID, "localeChanger")
        LOGO = (By.CSS_SELECTOR, "img.mopatImage")
        FOOTER_TEXT = (By.CSS_SELECTOR, ".footer-text")
        FORGOT_PASSWORD_LINK = (By.ID, "forgotPasswordLink")
        LANGUAGE_OPTIONS = (By.TAG_NAME, "option")

    class Logout:
        ADMIN_BUTTON = (By.ID, "adminLogoutButton")
        MOBILE_BUTTON = (By.ID, "logoutButton")

class PasswordResetSelectors:
    FORGOT_PASSWORD_LINK = (By.ID, "forgotPasswordLink")
    USERNAME_INPUT = (By.ID, "username")
    ERROR_MESSAGE = (By.CLASS_NAME, "error")
    SUBMIT_BUTTON = (By.ID, "submitRequest")
    LOGIN_LINK = (By.ID, "loginLink")

class AdminIndexSelectors:
    HEADER = (By.ID, "header")
    NAVIGATION_BAR = (By.ID, "adminNav")
    FOOTER_WRAPPER = (By.CSS_SELECTOR, ".footer-wrapper")
    MOPAT_LOGO = (By.ID, "mopatImageFooter")
    CLINIC_LOGO = (By.CSS_SELECTOR, "footer img.clinicLogo")
    WELCOME_TEXT = (By.CSS_SELECTOR, ".panel-body")
    PHONE_NUMBER = (By.CSS_SELECTOR, ".contact-phone")
    EMAIL = (By.CSS_SELECTOR, ".contact-email")
    IMPRESSUM_MODAL = (By.ID, "imprintDialog")
    MODAL_BODY = (By.CSS_SELECTOR, ".modal-body")
    BUTTON_CLOSE_MODAL = (By.CSS_SELECTOR, "button.btn-close")
    IMPRESSUM_LINK = (By.ID, "imprintLink")

class AdminIndexExpectedContent:
    LOGO_SRC = "/images/logo.svg"
    FOOTER_TEXT = f"© {str(datetime.now().year)} Institut für Medizinische Informatik,\nUniversität Münster"
    EXPECTED_LANGUAGES = ["Deutsch (Deutschland)", "Englisch (Vereinigtes Königreich)", "Spanisch (Spanien)"]
    FORGOT_PASSWORD_TEXT = "Passwort vergessen?"
    PHONE_NUMBER = "+49 (251) 83-55262"
    EMAIL = "imi@uni-muenster.de"
    WELCOME_TEXT_DE = "Willkommen zur Mobilen Patientenbefragung (MoPat)!"


class AuthenticationHelper:

    def __init__(self, driver):
        self.driver = driver

    def login(self, username, password):
        """
        :param username: The username for login.
        :param password: The password for login.
        """
        username_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            AuthenticationSelectors.Login.USERNAME))

        username_input.clear()
        username_input.send_keys(username)

        password_input = self.driver.find_element(*AuthenticationSelectors.Login.PASSWORD)
        password_input.clear()
        password_input.send_keys(password)

        submit_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            AuthenticationSelectors.Login.BUTTON))

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
                logout_button = WebDriverWait(self.driver, 5).until(EC.element_to_be_clickable(
                    selector))

                logout_button.click()
                return
            except:
                continue

    def back_to_login(self):
        back_to_login = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            PasswordResetSelectors.LOGIN_LINK))

        back_to_login.click()

class AuthenticationAssertHelper(AuthenticationHelper):

    def assert_mobile_user_login(self):
        # Validate username field
        username_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
            AuthenticationSelectors.Login.USERNAME))
        assert username_input.is_displayed(), "Username input field is not visible."

        # Validate password field
        password_input = self.driver.find_element(*AuthenticationSelectors.Login.PASSWORD)
        assert password_input.is_displayed(), "Password input field is not visible."

        # Validate login button
        submit_button = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            AuthenticationSelectors.Login.BUTTON))
        assert submit_button.is_displayed(), "Login button is not visible."

        # Validate language select dropdown
        try:
            language_select = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                AuthenticationSelectors.Login.LANGUAGE_SELECT))
            assert language_select.is_displayed(), "Language select dropdown is not visible."
        except TimeoutException:
            raise AssertionError("Language select dropdown was not found or is not visible.")

        # Check available languages
        options = language_select.find_elements(*AuthenticationSelectors.Login.LANGUAGE_OPTIONS)
        available_languages = [option.text.strip() for option in options if option.is_displayed()]
        assert set(available_languages) == set(AdminIndexExpectedContent.EXPECTED_LANGUAGES), (
            f"Languages mismatch. Expected: {AdminIndexExpectedContent.EXPECTED_LANGUAGES}, Found: {available_languages}"
        )

        # Validate MoPat logo
        logo = self.driver.find_element(*AuthenticationSelectors.Login.LOGO)
        assert logo.is_displayed(), "MoPat logo is not visible."
        assert logo.get_attribute("src").endswith(AdminIndexExpectedContent.LOGO_SRC), "Logo source is incorrect."

        # Validate footer text
        footer_text = self.driver.find_element(*AuthenticationSelectors.Login.FOOTER_TEXT).text.strip()

        # Normalize spaces and newlines for comparison
        normalized_footer_text = " ".join(footer_text.split())
        normalized_expected_footer_text = " ".join(AdminIndexExpectedContent.FOOTER_TEXT.split())

        assert normalized_footer_text == normalized_expected_footer_text, (
            f"Footer text is incorrect. Expected: '{AdminIndexExpectedContent.FOOTER_TEXT}', Found: '{footer_text}'"
        )

        # Validate 'Forgot password' link
        forget_password_link = self.driver.find_element(*AuthenticationSelectors.Login.FORGOT_PASSWORD_LINK)
        assert forget_password_link.is_displayed(), "'Forgot password' link is not visible."
        assert forget_password_link.get_attribute("href").endswith(URLPathsAuthentication.FORGOT_PASSWORD_PATH), (
            "'Forgot password' link URL is incorrect."
        )
        assert forget_password_link.text.strip() == AdminIndexExpectedContent.FORGOT_PASSWORD_TEXT, (
            "'Forgot password' link text is incorrect."
        )

    def assert_mobile_user_password(self):
        try:
            forgot_password_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                PasswordResetSelectors.FORGOT_PASSWORD_LINK))
            forgot_password_link.click()

            username_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                PasswordResetSelectors.USERNAME_INPUT))
            assert username_input.is_displayed(), "Username input field is not displayed."

            submit_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                PasswordResetSelectors.SUBMIT_BUTTON))
            assert submit_button.is_displayed(), "Submit button is not displayed."

            login_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                PasswordResetSelectors.LOGIN_LINK))
            assert login_link.is_displayed(), "Link to switch back to login is not displayed."

        except TimeoutException as e:
            raise AssertionError(f"Timeout while asserting elements in '/mobile/user/password' view: {e}")

    def assert_admin_index(self):
        try:
            header = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                AdminIndexSelectors.HEADER))
            assert header.is_displayed(), "Header is not displayed."

            nav_bar = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                AdminIndexSelectors.NAVIGATION_BAR))
            assert nav_bar.is_displayed(), "Navigation bar is not displayed."

            footer = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                AdminIndexSelectors.FOOTER_WRAPPER))
            assert footer.is_displayed(), "Footer wrapper is not displayed."
            self.assert_footer_logo()

            try:
                clinic_logo = self.driver.find_element(*AdminIndexSelectors.CLINIC_LOGO)
                assert clinic_logo.is_displayed(), "Clinic logo is not displayed in the footer."
            except NoSuchElementException:
                pass

            welcome_text = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                AdminIndexSelectors.WELCOME_TEXT))
            assert AdminIndexExpectedContent.WELCOME_TEXT_DE in welcome_text.text, "Welcome text is incorrect or not displayed."

            self.assert_contact_information()

        except TimeoutException as e:
            raise AssertionError(f"Timeout while asserting elements in '/admin/index': {e}")

    def assert_footer_logo(self):
        try:
            mopat_logo = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                AdminIndexSelectors.MOPAT_LOGO))

            # CSS-Validation
            logo_visibility = self.driver.execute_script("return window.getComputedStyle(arguments[0]).visibility;", mopat_logo)
            logo_display = self.driver.execute_script("return window.getComputedStyle(arguments[0]).display;", mopat_logo)

            assert logo_visibility == "visible", "MoPat logo is not visible (visibility is not 'visible')."
            assert logo_display != "none", "MoPat logo is not displayed (display is 'none')."

        except TimeoutException:
            raise AssertionError("MoPat logo not found in the footer.")
        except AssertionError as e:
            raise e

    def assert_contact_information(self):
        try:
            # Open the imprint modal
            imprint_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
                AdminIndexSelectors.IMPRESSUM_LINK))
            imprint_link.click()

            imprint_modal = WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(
                AdminIndexSelectors.IMPRESSUM_MODAL))

            modal_body = imprint_modal.find_element(*AdminIndexSelectors.MODAL_BODY)
            modal_text = modal_body.text

            # Verify phone number and email address
            assert AdminIndexExpectedContent.PHONE_NUMBER in modal_text, f"Expected phone number '{AdminIndexExpectedContent.PHONE_NUMBER}' is not in the modal."
            assert AdminIndexExpectedContent.EMAIL in modal_text, f"Expected email address '{AdminIndexExpectedContent.EMAIL}' is not in the modal."

            close_button = imprint_modal.find_element(*AdminIndexSelectors.BUTTON_CLOSE_MODAL)
            close_button.click()

        except TimeoutException:
            raise AssertionError("Failed to open the imprint modal.")
        except AssertionError as e:
            raise e