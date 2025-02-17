from enum import Enum
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SearchBoxSelectors, SeleniumUtils

class EmailSelectors:
    SUBJECT_INPUT = (By.ID, "subject")
    CONTENT_INPUT = (By.ID, "mailContent")
    MAIL_PREVIEW_BUTTON = (By.ID, "mailPreviewButton")
    SEND_BUTTON = (By.ID, "mailButton")
    
class UserSelector:
    BUTTON_INVITE_USER = (By.ID, "newInvitationButton")
    BUTTON_ADD_USER = (By.ID, "addInvitationButton")
    BUTTON_MOVE_CLINIC = lambda id: (By.ID, f"{id}")
    BUTTON_SEND_INVITE = (By.ID, "inviteButton")
    BUTTON_EDIT_INVITE = (By.ID, "editInvite")
    BUTTON_REMOVE_INVITATION = (By.CLASS_NAME, "removeInvitationButton")
    BUTTON_PREVIEW = (By.ID, "previewButton")
    BUTTON_MAIL_PREVIEW = (By.ID, "mailPreviewButton")
    BUTTON_MAIL_TO_ALL = (By.ID, "mailButton")

    INPUT_USER_FIRST_NAME = lambda index: (By.ID, f"invitationUsers{index}.firstName")
    INPUT_USER_LAST_NAME = lambda index: (By.ID, f"invitationUsers{index}.lastName")
    INPUT_USER_EMAIL = lambda index: (By.ID, f"invitationUsers{index}.email")
    INPUT_PERSONAL_TEXT = (By.ID, "personalText")
    INPUT_CSV = (By.CLASS_NAME, "file-input")
    INPUT_SUBJECT = (By.ID, "subject")
    INPUT_MESSAGE = (By.ID, "mailContent")

    SELECT_USER_ROLE = (By.ID, "userRoleSelect")
    SELECT_USER_LANGUAGE = (By.ID, "userLanguageSelect")
    SELECT_MAIL_LANGUAGE = (By.ID, "language")

    TABLE_USERS = (By.ID, "userTable")
    TABLE_INVITAIONS = (By.ID, "invitationTable")
    TABLE_ACTION_BUTTONS= (By.CSS_SELECTOR, "td.actionColumn")
    TABLE_AVAILABLE_CLINICS = (By.ID, "availableClinicsTable")
    TABLE_ASSIGNED_CLINICS = (By.ID, "assignedClinicsTable")
    
    PAGINATION_USER_TABLE = (By.ID, "userTable_paginate")
    PAGINATION_INVITATION_TABLE = (By.ID, "invitationTable_paginate")

    DIV_PREVIEW = (By.ID, "previewDiv")
    DIV_PREVIEW_MAIL = (By.ID, "preview")


class UserRoles(Enum):
    ADMIN = "ROLE_USER"
    USER = "ROLE_USER"
    MODERATOR = "ROLE_MODERATOR"
    ENCOUNTERMANAGER = "ROLE_ENCOUNTERMANAGER"
    EDITOR = "ROLE_EDITOR"

class UserHelper:
    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.navigation_helper = navigation_helper
        self.utils = SeleniumUtils(driver)

    def invite_user(self, users, role=UserRoles.USER, language="DEUTSCH", invite_message=None, clinic=None):
        """
        :param users: An array of user json objects like {"first_name": str, "last_name":str, "email":str}.
        :param role: The role of the user to be invited.
        :param language: The language of the invitation email.
        :param invite_message: The message to be sent with the invitation.
        :param clinic: The array of clinics to which the user is invited.
        """
        try:
            self.navigation_helper.navigate_to_manager_user()

            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(UserSelector.BUTTON_INVITE_USER)
            )

            self.utils.click_element(UserSelector.BUTTON_INVITE_USER)
            
            for index in range(len(users)):
                try:
                    WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(UserSelector.INPUT_USER_FIRST_NAME(index))
                    )
                    self.utils.fill_text_field(UserSelector.INPUT_USER_FIRST_NAME(index), users[index]['first_name'])
                    self.utils.fill_text_field(UserSelector.INPUT_USER_LAST_NAME(index), users[index]['last_name'])
                    self.utils.fill_text_field(UserSelector.INPUT_USER_EMAIL(index), users[index]['email'])
                    self.utils.click_element(UserSelector.BUTTON_ADD_USER)
                except Exception as e:
                    raise Exception(f"Failed to fill user data: {e}")
            
            self.utils.select_dropdown(UserSelector.SELECT_USER_ROLE, role.value)
            self.utils.select_dropdown(UserSelector.SELECT_USER_LANGUAGE, language)
            if invite_message:
                self.utils.fill_text_field(UserSelector.INPUT_PERSONAL_TEXT, invite_message)

            if clinic:
                try:
                    for c in clinic:
                        self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(c))
                except Exception as e:
                    raise Exception(f"Failed to move assign clinics to user while inviting: {e}")
                
            self.utils.click_element(UserSelector.BUTTON_INVITE_USER)
                
        except Exception as e:
            raise Exception(f"Failed to invite user: {e}")
        
    def send_invite(self, users):
        """
        :param users: An array of user json objects like {"first_name": str, "last_name":str, "email":str}.
        """
        try:
            # send the invite
            self.utils.click_element(UserSelector.BUTTON_SEND_INVITE)

            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.ID, "invitationTable"))
            )
            invite_ids = []
            for user in users:
            # Search for the bundle by name
                self.utils.fill_text_field(SearchBoxSelectors.INVITATION, user['email'])

                # Extract the bundle ID from the link
                edit_link = WebDriverWait(self.driver, 10).until(
                    EC.presence_of_element_located((UserSelector.BUTTON_EDIT_INVITE))
                )
                invite_id = edit_link.get_attribute("href").split("id=")[1]
                if invite_id:
                    invite_ids.append(invite_id)
            
            return invite_ids

        except Exception as e:
            raise Exception(f"Error while inviting users: {e}")