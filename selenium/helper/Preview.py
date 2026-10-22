import re

from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils


PREVIEW_PRESETS = {
    "phone-portrait": (390, 844),
    "phone-landscape": (844, 390),
    "tablet-portrait": (810, 1080),
    "tablet-landscape": (1080, 810),
    "laptop": (1920, 1080),
    "desktop": (2560, 1080),
    "fit": (None, None)  # special case: unscaled, fills the modal
}

PREVIEW_SCALE = 0.9
PREVIEW_MARGIN = 16
PREVIEW_BORDER = 2


class PreviewSelectors:
    BUTTON_PREVIEW = (By.ID, "previewButton")
    DROPDOWN_SIZES = (By.ID, "preview-sizes-dropdown")
    DROPDOWN_ITEMS = (By.CSS_SELECTOR, "#preview-sizes-dropdown .dropdown-item")

    MODAL = (By.ID, "previewModal")
    MODAL_DIALOG = (By.ID, "previewModalDialog")
    MODAL_CONTENT = (By.ID, "previewModalContent")
    MODAL_HEADER = (By.CSS_SELECTOR, "#previewModalContent .modal-header")
    META = (By.ID, "previewMeta")

    VIEWPORT = (By.ID, "previewViewport")
    FRAME = (By.ID, "previewFrame")

    HEADER = (By.CSS_SELECTOR, "#previewFrame .header.stickyHeader")
    QUESTIONNAIRE_TITLE = (By.ID, "previewQuestionnaireTitle")
    NAVIGATION = (By.CSS_SELECTOR, "#previewFrame .questionnaireNavigation")
    BUTTON_PREVIOUS = (By.CSS_SELECTOR, "#previewFrame #buttonPrevious")
    BUTTON_NEXT = (By.CSS_SELECTOR, "#previewFrame #buttonNext")
    BUTTON_FONT_SIZE = (By.CSS_SELECTOR, "#previewFrame .btn-lowEmphasis")
    PROGRESS_ELEMENT = (By.CSS_SELECTOR, "#previewFrame .progressElement")

    LAYOUT_CONTAINER = (By.CSS_SELECTOR, "#previewFrame .layoutContainer")
    CONTENT = (By.CSS_SELECTOR, "#previewFrame .content")
    QUESTION_TITLE = (By.CSS_SELECTOR, "#previewFrame #questionTitle")
    QUESTION_TITLE_TEXT = (By.CSS_SELECTOR, "#previewFrame #questionTitleText")
    QUESTION_CONTENT = (By.CSS_SELECTOR, "#previewFrame #questionContent")
    FOOTER = (By.CSS_SELECTOR, "#previewFrame .footer")

    MULTIPLE_CHOICE = (By.CSS_SELECTOR, "#previewFrame #multiple-choice")
    MULTIPLE_CHOICE_OPTIONS = (By.CSS_SELECTOR, "#previewFrame #multiple-choice .form-check")
    MULTIPLE_CHOICE_LABELS = (By.CSS_SELECTOR, "#previewFrame #multiple-choice .form-check .right")


_STATE_SCRIPT = """
var modal = document.getElementById('previewModal');
var dialog = document.getElementById('previewModalDialog');
var viewport = document.getElementById('previewViewport');
var frame = document.getElementById('previewFrame');
if (!modal || !dialog || !viewport || !frame) { return null; }

function px(el, prop) {
    return Math.round(parseFloat(getComputedStyle(el)[prop]) || 0);
}

function bp(name, fallback) {
    var raw = getComputedStyle(modal).getPropertyValue(name).trim();
    if (!raw) { return fallback; }
    var n = parseFloat(raw);
    if (isNaN(n)) { return fallback; }
    if (raw.indexOf('rem') > -1) {
        n *= parseFloat(getComputedStyle(document.documentElement).fontSize);
    }
    return n;
}

var scale = 1;
var tr = getComputedStyle(frame).transform;
if (tr && tr !== 'none') {
    var m = tr.match(/matrix\\(([^)]+)\\)/);
    if (m) { scale = parseFloat(m[1].split(',')[0]); }
}

var head = document.querySelector('#previewModalContent .modal-header');
var titleEl = document.getElementById('questionTitleText');
var contentEl = document.getElementById('questionContent');
var metaEl = document.getElementById('previewMeta');
var qTitleEl = document.getElementById('questionTitle');

return {
    shown: modal.classList.contains('show'),
    isSm: modal.classList.contains('preview-sm'),
    isLg: modal.classList.contains('preview-lg'),
    isPhone: modal.classList.contains('preview-phone'),
    frameW: px(frame, 'width'),
    frameH: px(frame, 'height'),
    viewportW: px(viewport, 'width'),
    viewportH: px(viewport, 'height'),
    dialogW: px(dialog, 'width'),
    scale: scale,
    frameStyle: (frame.getAttribute('style') || '').trim(),
    dialogStyle: (dialog.getAttribute('style') || '').trim(),
    viewportStyle: (viewport.getAttribute('style') || '').trim(),
    meta: metaEl ? metaEl.textContent.trim() : '',
    titleText: titleEl ? titleEl.textContent.trim() : null,
    titleFontSize: qTitleEl ? parseFloat(getComputedStyle(qTitleEl).fontSize) : null,
    contentChildren: contentEl ? contentEl.children.length : 0,
    innerW: window.innerWidth,
    innerH: window.innerHeight,
    headerH: head ? Math.round(head.offsetHeight) : 0,
    bpElement: bp('--preview-bp-element', 640),
    bpPhone: bp('--preview-bp-phone', 400)
};
"""

_READ_TEXT_SCRIPT = """
var lang = arguments[0];
var ta = document.querySelector("textarea[name='localizedQuestionText[" + lang + "]']")
      || document.querySelector("textarea[name^='localizedQuestionText']");
if (!ta) { return null; }
var $ta = window.jQuery ? window.jQuery(ta) : null;
if ($ta && $ta.summernote && $ta.hasClass('wysiwyg')) {
    try { return $ta.summernote('code'); } catch (e) { }
}
return ta.value;
"""

_WRITE_TEXT_SCRIPT = """
var lang = arguments[0], text = arguments[1];
var ta = document.querySelector("textarea[name='localizedQuestionText[" + lang + "]']")
      || document.querySelector("textarea[name^='localizedQuestionText']");
if (!ta) { return false; }
var $ta = window.jQuery ? window.jQuery(ta) : null;
if ($ta && $ta.summernote && $ta.hasClass('wysiwyg')) {
    try { $ta.summernote('code', text); } catch (e) { }
}
ta.value = text;
return true;
"""

_FIRST_LANGUAGE_SCRIPT = """
var first = document.querySelector('#addedLanguages > *');
if (!first) { return null; }
var id = first.getAttribute('id') || '';
var sep = id.lastIndexOf('_');
return sep > 0 ? id.substring(0, sep) : null;
"""


class PreviewHelper:

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper

    def get_state(self):
        return self.driver.execute_script(_STATE_SCRIPT)

    def get_first_added_language(self):
        return self.driver.execute_script(_FIRST_LANGUAGE_SCRIPT)

    def read_question_text(self, language_code="de_DE"):
        return self.driver.execute_script(_READ_TEXT_SCRIPT, language_code)

    def write_question_text(self, text, language_code="de_DE"):
        return self.driver.execute_script(_WRITE_TEXT_SCRIPT, language_code, text)

    def is_open(self):
        state = self.get_state()
        return bool(state and state["shown"])

    def open_preview(self, preset=None):
        """Opens the preview. `preset` is a key of PREVIEW_PRESETS, a [w, h]
        list, or None for the default (unscaled) preview."""
        if self.is_open():
            self.close_preview()

        if preset is None:
            self.utils.click_element(PreviewSelectors.BUTTON_PREVIEW)
        else:
            self.driver.execute_script("showModal(arguments[0]);", preset)

        self._wait_until_rendered()

    def close_preview(self):
        self.driver.execute_script(
            "bootstrap.Modal.getOrCreateInstance("
            "document.getElementById('previewModal')).hide();"
        )
        WebDriverWait(self.driver, 10).until_not(
            EC.visibility_of_element_located(PreviewSelectors.MODAL)
        )
        WebDriverWait(self.driver, 10).until(
            lambda d: not (self.get_state() or {}).get("frameStyle")
        )

    def _wait_until_rendered(self):
        WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(PreviewSelectors.MODAL)
        )
        WebDriverWait(self.driver, 20).until(
            lambda d: (self.get_state() or {}).get("contentChildren", 0) > 0,
            "Preview never rendered any question content"
        )

    def expected_scale(self, state, width, height):
        assert width and height, "expected_scale requires fixed dimensions."
        avail_w = state["innerW"] - PREVIEW_MARGIN * 2 - PREVIEW_BORDER
        avail_h = state["innerH"] - PREVIEW_MARGIN * 2 - PREVIEW_BORDER - state["headerH"]
        return min(PREVIEW_SCALE, avail_w / float(width), avail_h / float(height))

    def get_severe_console_errors(self):
        """Requires goog:loggingPrefs -> browser: ALL. Returns None if
        browser logs are unavailable on this driver."""
        try:
            logs = self.driver.get_log("browser")
        except Exception:
            return None
        return [entry for entry in logs if entry.get("level") == "SEVERE"]

    @staticmethod
    def strip_html(value):
        text = re.sub(r"<[^>]+>", " ", value or "")
        text = text.replace("&nbsp;", " ").replace("\xa0", " ")
        return " ".join(text.split())


class PreviewAssertHelper(PreviewHelper):

    def assert_trigger_controls(self):
        preview_button = WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(PreviewSelectors.BUTTON_PREVIEW))
        assert preview_button.is_displayed(), "Preview button is not displayed."

        WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(PreviewSelectors.DROPDOWN_SIZES))

        items = self.driver.find_elements(*PreviewSelectors.DROPDOWN_ITEMS)
        assert len(items) == len(PREVIEW_PRESETS), (
            f"Expected {len(PREVIEW_PRESETS)} preview size entries, found {len(items)}."
        )

    def assert_survey_chrome_present(self):
        checks = [
            (PreviewSelectors.HEADER, "Sticky survey header"),
            (PreviewSelectors.QUESTIONNAIRE_TITLE, "Questionnaire title"),
            (PreviewSelectors.NAVIGATION, "Questionnaire navigation bar"),
            (PreviewSelectors.BUTTON_PREVIOUS, "Previous question button"),
            (PreviewSelectors.BUTTON_NEXT, "Next question button"),
            (PreviewSelectors.BUTTON_FONT_SIZE, "Font size button"),
            (PreviewSelectors.PROGRESS_ELEMENT, "Progress element"),
            (PreviewSelectors.LAYOUT_CONTAINER, "layoutContainer"),
            (PreviewSelectors.CONTENT, "content container"),
            (PreviewSelectors.QUESTION_TITLE, "questionTitle"),
            (PreviewSelectors.QUESTION_CONTENT, "questionContent"),
            (PreviewSelectors.FOOTER, "footer"),
        ]
        for selector, label in checks:
            try:
                WebDriverWait(self.driver, 10).until(
                    EC.presence_of_element_located(selector))
            except Exception:
                raise AssertionError(f"{label} is missing from the preview frame.")

    def assert_dom_structure(self):
        order_ok = self.driver.execute_script("""
            var frame = document.getElementById('previewFrame');
            if (!frame) { return 'no frame'; }
            var header = frame.querySelector(':scope > .header');
            var layout = frame.querySelector(':scope > .layoutContainer');
            if (!header || !layout) { return 'header/layoutContainer not direct children of frame'; }
            var content = layout.querySelector(':scope > .content');
            var footer = layout.querySelector(':scope > .footer');
            if (!content || !footer) { return 'content/footer not direct children of layoutContainer'; }
            var title = content.querySelector('#questionTitle');
            var body = content.querySelector('#questionContent');
            if (!title || !body) { return 'questionTitle/questionContent not inside content'; }
            if (!(title.compareDocumentPosition(body) & Node.DOCUMENT_POSITION_FOLLOWING)) {
                return 'questionContent does not follow questionTitle';
            }
            if (!(header.compareDocumentPosition(layout) & Node.DOCUMENT_POSITION_FOLLOWING)) {
                return 'layoutContainer does not follow header';
            }
            return 'ok';
        """)
        assert order_ok == "ok", f"Preview DOM structure wrong: {order_ok}"

    def assert_question_title_matches_form(self, language_code="de_DE"):
        form_html = self.read_question_text(language_code)
        assert form_html, "Could not read the localized question text from the edit form."

        expected = self.strip_html(form_html)
        state = self.get_state()
        actual = " ".join((state["titleText"] or "").split())

        assert actual, "Preview question title is empty."
        assert expected in actual or actual in expected, (
            "Preview title does not match the question text currently in the form.\n"
            f"  form (stripped): '{expected}'\n"
            f"  preview        : '{actual}'\n"
            "If the form text is newer than the preview, the WYSIWYG editor is not "
            "synced into the textarea that FormData reads."
        )

    def assert_multiple_choice_rendered(self, options):
        WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(PreviewSelectors.MULTIPLE_CHOICE),
            "Multiple choice block was not rendered in the preview"
        )
        rendered = self.driver.find_elements(*PreviewSelectors.MULTIPLE_CHOICE_LABELS)
        rendered_texts = [el.text.strip() for el in rendered]

        assert len(rendered_texts) == len(options), (
            f"Expected {len(options)} answer options in the preview, "
            f"found {len(rendered_texts)}: {rendered_texts}"
        )
        for option in options:
            assert option in rendered_texts, (
                f"Answer option '{option}' missing from preview. Found: {rendered_texts}"
            )

    def assert_preset_geometry(self, preset, width, height):
        if width is None or height is None:
            return self.assert_fit_geometry(preset)
        return self.assert_fixed_preset_geometry(preset, width, height)

    def assert_fit_geometry(self, preset="fit"):
        state = self.get_state()
        assert state is not None, "Preview state unavailable."

        assert abs(state["scale"] - 1.0) < 0.001, (
            f"[{preset}] fit mode must not scale, got {state['scale']:.3f}."
        )

        assert state["frameW"] == state["viewportW"], (
            f"[{preset}] unscaled frame width {state['frameW']} should equal "
            f"viewport width {state['viewportW']}."
        )
        assert state["frameH"] == state["viewportH"], (
            f"[{preset}] unscaled frame height {state['frameH']} should equal "
            f"viewport height {state['viewportH']}."
        )

        avail_w = state["innerW"] - PREVIEW_MARGIN * 2 - PREVIEW_BORDER
        avail_h = state["innerH"] - PREVIEW_MARGIN * 2 - PREVIEW_BORDER - state["headerH"]

        assert abs(state["frameW"] - avail_w) <= 2, (
            f"[{preset}] frame width {state['frameW']} should fill the available "
            f"width {avail_w}."
        )
        assert abs(state["frameH"] - avail_h) <= 2, (
            f"[{preset}] frame height {state['frameH']} should fill the available "
            f"height {avail_h}."
        )

        assert state["frameW"] <= state["innerW"], (
            f"[{preset}] frame ({state['frameW']}px) overflows the window "
            f"({state['innerW']}px)."
        )
        assert state["frameH"] <= state["innerH"], (
            f"[{preset}] frame ({state['frameH']}px) overflows the window "
            f"({state['innerH']}px)."
        )

        self._assert_dialog_tracks_viewport(preset, state)
        return state

    def assert_fixed_preset_geometry(self, preset, width, height):
        state = self.get_state()
        assert state is not None, "Preview state unavailable."

        assert state["frameW"] == width, (
            f"[{preset}] frame width should be the device width {width}, "
            f"got {state['frameW']}."
        )
        assert state["frameH"] == height, (
            f"[{preset}] frame height should be the device height {height}, "
            f"got {state['frameH']}."
        )

        expected = self.expected_scale(state, width, height)
        assert abs(expected - state["scale"]) < 0.01, (
            f"[{preset}] expected scale {expected:.3f}, got {state['scale']:.3f}."
        )

        assert abs(round(width * state["scale"]) - state["viewportW"]) <= 1, (
            f"[{preset}] viewport width {state['viewportW']} != "
            f"frame width * scale ({width} * {state['scale']:.3f})."
        )
        assert abs(round(height * state["scale"]) - state["viewportH"]) <= 1, (
            f"[{preset}] viewport height {state['viewportH']} != "
            f"frame height * scale ({height} * {state['scale']:.3f})."
        )

        device_ratio = width / float(height)
        shown_ratio = state["viewportW"] / float(state["viewportH"])
        assert abs(device_ratio - shown_ratio) < 0.02, (
            f"[{preset}] aspect ratio drifted: device {device_ratio:.3f} "
            f"vs rendered {shown_ratio:.3f}."
        )

        assert str(width) in state["meta"], (
            f"[{preset}] size badge should report the device width {width}, "
            f"got '{state['meta']}'."
        )

        self._assert_dialog_tracks_viewport(preset, state)
        return state

    def _assert_dialog_tracks_viewport(self, preset, state):
        assert state["dialogW"] >= state["viewportW"], (
            f"[{preset}] modal-dialog ({state['dialogW']}px) is narrower than the "
            f"viewport ({state['viewportW']}px) - a max-width rule is clamping it."
        )
        assert state["dialogW"] - state["viewportW"] <= 4, (
            f"[{preset}] modal-dialog ({state['dialogW']}px) is much wider than the "
            f"viewport ({state['viewportW']}px)."
        )

    def assert_breakpoint_classes(self, preset, width, state):
        if width is None:
            width = state["frameW"]
            
        expect_sm = width <= state["bpElement"]
        expect_lg = width >= state["bpElement"]
        expect_phone = width <= state["bpPhone"]

        assert state["isSm"] == expect_sm, (
            f"[{preset}] preview-sm should be {expect_sm} for a {width}px frame "
            f"(element breakpoint {state['bpElement']}px)."
        )
        assert state["isLg"] == expect_lg, (
            f"[{preset}] preview-lg should be {expect_lg} for a {width}px frame "
            f"(element breakpoint {state['bpElement']}px)."
        )
        assert state["isPhone"] == expect_phone, (
            f"[{preset}] preview-phone should be {expect_phone} for a {width}px frame "
            f"(phone breakpoint {state['bpPhone']}px)."
        )

    def assert_typography_follows_frame_width(self):
        self.open_preview("phone-portrait")
        phone = self.get_state()
        self.close_preview()

        self.open_preview("desktop")
        desktop = self.get_state()
        self.close_preview()

        assert phone["titleFontSize"] and desktop["titleFontSize"], (
            "Could not read the question title font size."
        )
        assert phone["titleFontSize"] != desktop["titleFontSize"], (
            "Question title font size is identical on phone "
            f"({phone['titleFontSize']}px) and desktop ({desktop['titleFontSize']}px). "
            "The preview-sm / preview-lg classes are not driving typography - "
            "breakpoints are still resolving against the browser window."
        )

    def assert_reset_after_close(self):
        state = self.get_state()
        assert state["frameStyle"] == "", (
            f"Frame inline style not cleared on close: '{state['frameStyle']}'")
        assert state["dialogStyle"] == "", (
            f"Dialog inline style not cleared on close: '{state['dialogStyle']}'")
        assert state["viewportStyle"] == "", (
            f"Viewport inline style not cleared on close: '{state['viewportStyle']}'")
        assert not (state["isSm"] or state["isLg"] or state["isPhone"]), (
            "Breakpoint classes were not removed when the preview closed.")
        assert state["contentChildren"] == 0, (
            "questionContent was not emptied when the preview closed.")

    def assert_no_severe_console_errors(self, context=""):
        errors = self.get_severe_console_errors()
        if errors is None:
            return
        messages = [e.get("message", "") for e in errors]
        assert not messages, (
            f"Severe console errors during preview {context}:\n" + "\n".join(messages)
        )