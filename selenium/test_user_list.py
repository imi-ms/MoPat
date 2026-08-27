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
from helper.User import UserSelector
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_user_list(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_manager_user()

        self.utils.check_visibility_of_element(UserSelector.TABLE_USERS,
                                               "User list not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.PAGINATION_USER_TABLE, "Pagination not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.TABLE_ACTION_BUTTONS, "Action buttons not displayed")
        self.utils.check_visibility_of_element(UserSelector.BUTTON_INVITE_USER,
                                               "Invite user button not displayed")


if __name__ == "__main__":
    unittest.main(verbosity=2)
