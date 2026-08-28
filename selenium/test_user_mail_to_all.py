#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.Login import LoginHelper
from helper.User import UserSelector, EmailSelectors
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_user_mail_to_all(self):
        test_subject = "Test Subject"
        test_content = "Test Content"
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        self.navigation_helper.navigate_to_email_to_all_users()

        self.utils.check_visibility_of_element(EmailSelectors.SUBJECT_INPUT,
                                               "Subject input field not displayed")
        self.utils.check_visibility_of_element(EmailSelectors.CONTENT_INPUT,
                                               "Content input field not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.SELECT_MAIL_LANGUAGE,
            "Language dropdown not displayed")
        self.utils.check_visibility_of_element(
            EmailSelectors.MAIL_PREVIEW_BUTTON, "Preview button not displayed")
        self.utils.check_visibility_of_element(EmailSelectors.SEND_BUTTON,
                                               "Send button not displayed")

        # Assert - Check if the preview button works
        try:
            self.utils.fill_text_field(EmailSelectors.SUBJECT_INPUT,
                                       test_subject)
            self.utils.fill_text_field(EmailSelectors.CONTENT_INPUT,
                                       test_content)
            self.utils.click_element(EmailSelectors.MAIL_PREVIEW_BUTTON)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.DIV_PREVIEW_MAIL)
            )
        except Exception as e:
            self.fail("Preview not displayed")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
