window.HtmlUtilities = window.HtmlUtilities || {};

// Converts HTML content to countable visible text for validation.
HtmlUtilities.getVisibleText = function (html) {
  if (!html) {
    return "";
  }

  var text = html
    .replace(/\r\n/g, "\n")
    .replace(/\r/g, "\n");

  text = text.replace(/<br\s*\/?>/gi, "\n");
  text = text.replace(/<\/(?:div|p|li|h[1-6])\s*>/gi, "\n");
  text = text.replace(/<[^>]+>/g, "");

  text = text.replace(/&nbsp;/g, " ");
  text = text.replace(/&amp;/g, "&");
  text = text.replace(/&lt;/g, "<");
  text = text.replace(/&gt;/g, ">");
  text = text.replace(/&quot;/g, "\"");
  text = text.replace(/&#39;/g, "'");

  return text.replace(/\n{3,}/g, "\n\n").trim();
};

/* Updates the inline validation message for a WYSIWYG editor. */
HtmlUtilities.updateWysiwygTextLengthValidation = function (noteEditable, options) {
  if (!options || !options.textareaNamePrefix || !options.maxLength || !options.message) {
    return;
  }

  var $editor = $(noteEditable).closest(".note-editor");

  var $textarea = $editor
    .parent()
    .find("textarea")
    .filter(function () {
      return ($(this).attr("name") || "").indexOf(options.textareaNamePrefix) === 0;
    })
    .first();

  if ($textarea.length === 0) {
    return;
  }

  var currentLength = HtmlUtilities.getVisibleText($(noteEditable).html()).length;
  var $errorLabel = $editor.prev(".text-length-error");

  if ($errorLabel.length === 0) {
    $errorLabel = $("<label/>", {
      class: "text-length-error text-danger d-block"
    });
    $editor.before($errorLabel);
  }

  if (currentLength > options.maxLength) {
    $errorLabel.text(
      options.message
        .replace("{0}", currentLength)
        .replace("{1}", options.maxLength)
    );
    $errorLabel.show();
  } else {
    $errorLabel.text("");
    $errorLabel.hide();
  }
};

/* Binds live length validation to WYSIWYG editors matching a textarea name prefix. */
HtmlUtilities.bindWysiwygTextLengthValidation = function (options) {
  $(document).on("input keyup paste", ".note-editable", function () {
    var noteEditable = this;

    setTimeout(function () {
      HtmlUtilities.updateWysiwygTextLengthValidation(noteEditable, options);
    }, 0);
  });
};