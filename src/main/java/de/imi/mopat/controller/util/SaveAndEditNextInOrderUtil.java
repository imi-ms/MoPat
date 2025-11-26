package de.imi.mopat.controller.util;

import java.util.Map;

/*
 * Util for rerouting to the next workflow element if the save and edit XY button is clicked.
 */
public class SaveAndEditNextInOrderUtil {

    private static final Map<String, String> order = Map.of(
            "questionnaire", "question",
            "question", "bundle",
            "bundle", "clinic",
            "clinic", "encounter");

    public static String determineNextRoute(String current, String action, String defaultRoute) {
        if ("saveAndEdit".equals(action)) {
            String nextInOrder = order.get(current);
            return "redirect:/" + nextInOrder + "/list";
        }
        return defaultRoute;
    }
}
