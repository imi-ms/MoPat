window.HtmlUtilities = window.HtmlUtilities || {};

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