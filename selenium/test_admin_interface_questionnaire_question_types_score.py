#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.Login import LoginHelper
from helper.Question import QuestionType
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_admin_interface_questionnaire_question_types_score(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])
        self.navigation_helper.navigate_to_manage_questionnaires()

        # self.questionnaire_assert_helper.assert_questionnaire_list()
        self.questionnaire_helper.click_add_questionnaire_button()
        questionnaire = self.questionnaire_assert_helper.assert_questionnaire_fill_page()
        self.navigation_helper.search_and_open_questionnaire(
            questionnaire['name'])
        self.questionnaire_helper.save_questionnaire_edit_question()
        self.questionnaire_helper.click_add_question_button()
        self.question_assert_helper.assert_question_fill_page()
        # TODO [LJ] implement for all types
        question_list = list()
        excluded_question_types = {QuestionType.IMAGE}
        question_types = [question_type for question_type in QuestionType if
                          question_type not in excluded_question_types]
        for question_type in question_types:
            self.questionnaire_helper.click_add_question_button()
            question_by_type = self.question_assert_helper.assert_question_by_type(
                question_type)
            question_list.append(question_by_type)
        self.question_assert_helper.assert_question_table_functionality(
            len(question_list))
        self.navigation_helper.navigate_to_scores_of_questionnaire(
            questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_scores_list()
        self.navigation_helper.navigate_to_scores_of_questionnaire(
            questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_score_fill()

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
