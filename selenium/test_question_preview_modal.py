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
from helper.Preview import PREVIEW_PRESETS
from helper.Question import QuestionType
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_question_preview_modal(self):
        created_questionnaire = {}
        preview = self.preview_assert_helper

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
                question_types={QuestionType.MULTIPLE_CHOICE}
            )
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            mc_question = next(
                q for q in created_questionnaire['questions']
                if q['type'] == QuestionType.MULTIPLE_CHOICE
            )
        except StopIteration:
            self.fail("No MULTIPLE_CHOICE question was created.")

        try:
            # --- persisted question, default preview -----------------------
            self.navigation_helper.open_question(mc_question['id'])
            preview.assert_trigger_controls()

            language_code = preview.get_first_added_language()
            self.assertIsNotNone(
                language_code,
                "No language found in #addedLanguages - the preview cannot resolve a language."
            )

            preview.open_preview()

            preview.assert_survey_chrome_present()
            preview.assert_dom_structure()
            preview.assert_question_title_matches_form(language_code)
            preview.assert_multiple_choice_rendered(mc_question['options'])

            state = preview.get_state()
            self.assertTrue(
                state['scale'] <= 1.0,
                f"Default preview should never scale up, got {state['scale']}."
            )

            preview.close_preview()
            preview.assert_reset_after_close()

            # --- every device preset ---------------------------------------
            for preset, (width, height) in PREVIEW_PRESETS.items():
                preview.open_preview(preset)

                geometry = preview.assert_preset_geometry(preset, width, height)
                preview.assert_breakpoint_classes(preset, width, geometry)
                preview.assert_survey_chrome_present()
                preview.assert_multiple_choice_rendered(mc_question['options'])

                preview.close_preview()
                preview.assert_reset_after_close()

            # --- typography must follow the frame, not the window ----------
            preview.assert_typography_follows_frame_width()

            # --- preview reflects unsaved edits ----------------------------
            edited_text = "Vorschau Test - geaenderter Fragetext"
            self.assertTrue(
                preview.write_question_text(f"<p>{edited_text}</p>",
                                            language_code),
                "Could not write into the question text editor."
            )

            preview.open_preview("tablet-portrait")
            state = preview.get_state()
            self.assertIn(
                edited_text, " ".join((state['titleText'] or "").split()),
                "Preview did not pick up the unsaved question text - it is not "
                "reading live form state."
            )
            preview.assert_multiple_choice_rendered(mc_question['options'])
            preview.close_preview()

            preview.assert_no_severe_console_errors("of a persisted question")

            self.question_helper.cancel_question_editing()

            # --- brand new, never saved question (id == null) --------------
            self.navigation_helper.navigate_to_questions_of_questionnaire(
                created_questionnaire['id'], created_questionnaire['name']
            )
            self.questionnaire_helper.click_add_question_button()

            new_question = self.question_helper.add_question_multiple_choice(
                language_code=language_code,
                question_text="Vorschau Test - neue Frage",
                options=["Alpha", "Beta"],
                min_answers=1,
                max_answers=2,
            )

            preview.open_preview("phone-portrait")
            preview.assert_survey_chrome_present()
            preview.assert_question_title_matches_form(language_code)
            preview.assert_multiple_choice_rendered(new_question['options'])
            preview.close_preview()
            preview.assert_reset_after_close()

            preview.assert_no_severe_console_errors("of an unsaved question")

            self.question_helper.cancel_question_editing()

        finally:
            if created_questionnaire:
                self.utils.search_and_delete_item(
                    created_questionnaire['name'], created_questionnaire['id'],
                    "questionnaire"
                )
            self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
