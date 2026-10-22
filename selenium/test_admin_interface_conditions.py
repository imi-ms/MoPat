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

from helper.Condition import ConditionSelectors
from helper.Login import LoginHelper
from helper.Question import QuestionType
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_admin_interface_conditions(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Create source and target questionnaires with specific question types and add the questionnaires to a bundle to enable selection as a condition target
        source_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.SLIDER, QuestionType.MULTIPLE_CHOICE,
                            QuestionType.DROP_DOWN})
        target_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.INFO_TEXT})
        bundle = self.bundle_helper.create_bundle(
            questionnaires=[source_questionnaire, target_questionnaire])
        bundle['id'] = self.bundle_helper.save_bundle(bundle['name'])

        threshold_supported_question_types = {QuestionType.SLIDER,
                                              QuestionType.NUMBER_CHECKBOX,
                                              QuestionType.NUMBER_INPUT}

        # Select a question where condition can be added with threshold value from the source questionnaire
        threshold_condition_question = next(
            (question for question in source_questionnaire['questions']
             if question['type'] in threshold_supported_question_types), None)

        # Navigate to the source questionnaire's questions and reorder the slider question to the first position
        self.navigation_helper.navigate_to_questions_of_questionnaire(
            source_questionnaire['id'], source_questionnaire['name'])
        self.questionnaire_helper.reorder_question(
            threshold_condition_question['id'], 0)
        # Open the condition page for the question and add conditions targeting question, answer, and questionnaire
        self.condition_helper.open_conditions_of_question(
            threshold_condition_question['id'])
        condition_id_1 = self.condition_helper.add_condition_for_threshold_questions(
            threshold_steps=1,
            target_type=ConditionSelectors.TargetType.QUESTION)
        condition_id_2 = self.condition_helper.add_condition_for_threshold_questions(
            threshold_steps=2, target_type=ConditionSelectors.TargetType.ANSWER)
        condition_id_3 = self.condition_helper.add_condition_for_threshold_questions(
            threshold_steps=3,
            target_type=ConditionSelectors.TargetType.QUESTIONNAIRE)

        # Assert the conditions are correctly listed in the tables
        self.condition_assert_helper.assert_condition_list_and_search_de()
        self.condition_helper.delete_condition(condition_id_1)
        self.condition_helper.delete_condition(condition_id_2)
        self.condition_helper.delete_condition(condition_id_3)
        self.condition_helper.navigate_back_to_questions_of_questionnaire()

        self.condition_assert_helper.assert_add_condition_page(
            source_questionnaire)

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
