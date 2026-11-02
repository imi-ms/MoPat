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

from helper.Dashboard import DashboardSelectors
from helper.Login import LoginHelper
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_git_info(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        self.dashboard_helper.open_git_info()

        self.utils.check_visibility_of_element(DashboardSelectors.TABLE_GITINFO,
                                               "Git Information Table not found")

        # Assert git information elements are present
        self.utils.check_visibility_of_element(
            DashboardSelectors.BLOCK_GIT_BUILD_VERSION,
            "Git Build Version not found")
        self.utils.check_visibility_of_element(
            DashboardSelectors.BLOCK_GIT_BRANCH, "Git Branch not found")
        self.utils.check_visibility_of_element(
            DashboardSelectors.BLOCK_GIT_COMMIT_ID, "Git Commit ID not found")
        self.utils.check_visibility_of_element(
            DashboardSelectors.BLOCK_GIT_COMMIT_MESSAGE,
            "Git Commit Message not found")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
