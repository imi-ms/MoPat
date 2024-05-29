package de.imi.mopat.helper.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParameterHelper {

    private final List<String> ignore;

    public ParameterHelper() {
        //Add all parameters that should never be kept in the URL
        this.ignore = new ArrayList<>();
    }

    /**
     * Returns HashMap from Query String.
     *
     * @param queryString to process
     * @return HashMap with key: parameter & value: value
     */
    public Map<String, List<String>> processQueryString(String queryString) {
        queryString = decode(queryString);
        //Check if Query exists
        if (queryString != null) {
            String[] params = queryString.split("&");

            Map<String, List<String>> queryMap = new LinkedHashMap<>();
            for (String param : params) {
                //Split into key and value
                String[] splitted = param.split("=");
                //Ignore all keys that are in ignore list
                if (!this.ignore.contains(splitted[0])) {

                    try {
                        //If value exists
                        addParameter(queryMap, splitted[0], splitted[1]);
                    } catch (Exception ex) {
                        //Empty Value creates error
                        addParameter(queryMap, splitted[0], "");
                    }
                }
            }
            return queryMap;
        }
        return new LinkedHashMap<>();
    }

    /**
     * This method either adds a key value pair or replaces the value of a certain key.
     *
     * @param currentParams Map of current parameters (generate with processQueryString)
     * @param param         to add or replace
     * @param value         of param
     * @return new Parameter Map
     */
    public Map<String, List<String>> replaceParam(final Map<String, List<String>> currentParams,
        final String param, final String value) {
        List<String> valueList = new ArrayList<>();
        valueList.add(stripXSS(value));
        currentParams.put(param, valueList);
        return currentParams;
    }

    /**
     * This methods adds a value to the parameter list. Other than replaceParam it allows for
     * multiple values for the same key
     *
     * @param mapToAddTo parameter map to add to (generate with processQueryString)
     * @param param      key to add
     * @param value      of param
     * @return new Parameter Map
     */
    public Map<String, List<String>> addParameter(final Map<String, List<String>> mapToAddTo,
        final String param, final String value) {
        if (mapToAddTo.containsKey(param)) {
            mapToAddTo.get(param).add(stripXSS(value));
        } else {
            List<String> valueList = new ArrayList<>();
            valueList.add(stripXSS(value));
            mapToAddTo.put(param, valueList);
        }
        return mapToAddTo;
    }

    /**
     * Removes parameter or value from current parameter list. If key has more than one value, only
     * the specific value will be removed
     *
     * @param mapToRemoveFrom map with parameters, where param should be removed
     * @param param           key that should be removed
     * @param value           specific value that should be removed
     * @return Map with parameter or value removed
     */
    public Map<String, List<String>> removeParameter(
        final Map<String, List<String>> mapToRemoveFrom, final String param, final String value) {
        if (mapToRemoveFrom.containsKey(param)) {
            if (mapToRemoveFrom.get(param).contains(value)) {
                List<String> entries = mapToRemoveFrom.get(param);
                if (entries.size() > 1) {
                    entries.remove(value);
                    mapToRemoveFrom.put(param, entries);
                } else {
                    mapToRemoveFrom.remove(param);
                }
            }

        }
        return mapToRemoveFrom;
    }

    /**
     * Method that transforms Query String to Map and adds a given parameter. Returns new Query
     * String To be used with Thymeleaf
     *
     * @param queryString that should be processed
     * @param param       that should be added
     * @param value       for param
     * @return processed Query String
     */
    public String thAddParam(final String queryString, final String param, final String value) {
        Map<String, List<String>> currentParams;
        if (queryString != null && !queryString.isEmpty()) {
            currentParams = processQueryString(queryString);
        } else {
            return "?" + param + "=" + value;
        }

        Map<String, List<String>> processedParams = addParameter(currentParams, param, value);

        StringBuilder result = new StringBuilder("?");
        for (Map.Entry<String, List<String>> entry : processedParams.entrySet()) {
            for (String val : entry.getValue()) {
                result.append(entry.getKey()).append("=").append(val).append("&");
            }
        }
        return result.substring(0, result.lastIndexOf("&"));

    }

    /**
     * Method that transforms Query String to Map and replaces a given parameter. Returns new Query
     * String To be used with Thymeleaf
     *
     * @param queryString that should be processed
     * @param param       that should be replaced
     * @param value       for param
     * @return processed Query String
     */
    public String thReplaceParam(final String queryString, final String param, final String value) {

        Map<String, List<String>> currentParams;
        if (queryString != null && !queryString.isEmpty()) {
            currentParams = processQueryString(queryString);
        } else {
            return "?" + param + "=" + value;
        }

        Map<String, List<String>> processedParams = replaceParam(currentParams, param, value);

        StringBuilder result = new StringBuilder("?");
        for (Map.Entry<String, List<String>> entry : processedParams.entrySet()) {
            for (String val : entry.getValue()) {
                result.append(entry.getKey()).append("=").append(val).append("&");
            }
        }
        return result.substring(0, result.lastIndexOf("&"));

    }

    /**
     * Method that transforms Query String to Map and removes a given parameter. Returns new Query
     * String To be used with Thymeleaf
     *
     * @param queryString that should be processed
     * @param param       that should be removed
     * @param value       for param
     * @return processed Query String
     */
    public String thRemoveParam(final String queryString, final String param, final String value) {
        Map<String, List<String>> currentParams;
        if (queryString != null && !queryString.isEmpty()) {
            currentParams = processQueryString(queryString);
        } else {
            return "";
        }

        Map<String, List<String>> processedParams = removeParameter(currentParams, param, value);

        if (processedParams.size() > 0) {
            StringBuilder result = new StringBuilder("?");
            for (Map.Entry<String, List<String>> entry : processedParams.entrySet()) {
                for (String val : entry.getValue()) {
                    result.append(entry.getKey()).append("=").append(val).append("&");
                }
            }
            return result.substring(0, result.lastIndexOf("&"));
        } else {
            return "";
        }
    }

    /**
     * Decodes URL String to replace Hex Codes.
     *
     * @param value Query String to process
     * @return Query String with removed Hex Codes
     */
    private String decode(final String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String stripXSS(String value) {
        if (value != null) {
            value = value.replace("<", "");
            value = value.replace(">", "");
            return value;

        }
        return null;
    }

}
