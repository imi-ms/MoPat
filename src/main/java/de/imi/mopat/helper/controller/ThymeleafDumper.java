package de.imi.mopat.helper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.WebEngineContext;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class can be used as a development tool. It is added in the GlobalControllerAdvice and
 * therefore usable in any view.
 * <p>
 * It can be used to gain more information on the current variables in the model.
 */
@Component
public class ThymeleafDumper {

    private final Logger log = LoggerFactory.getLogger(ThymeleafDumper.class);

    /**
     * Log the current String representation of a specific var
     *
     * @param name:   Name of the var for better tracking
     * @param object: The object itself
     */
    public void logVar(final String name, final Object object) {
        log.info("Var:" + name + " - " + object.getClass() + ":" + object);
    }

    /**
     * logs all vars that are currently in scope
     *
     * @param ctx: The webengine context
     */
    public void debug(final WebEngineContext ctx) {
        SortedMap<String, Object> result = debugContext(ctx);
        log.debug("Debug: ");
        for (Entry<String, Object> availableVar : result.entrySet()) {
            log.debug(availableVar.getKey() + " - " + availableVar.getValue().toString());
        }
    }

    /**
     * Helper method to receive all variables from context in a Sorted Map.
     *
     * @param ctx: The webengine context
     * @return SortedMap with all vars from webengine context
     */
    private SortedMap<String, Object> debugContext(final WebEngineContext ctx) {
        Set<String> variableNames = ctx.getVariableNames();
        SortedMap<String, Object> result = new TreeMap<>();
        for (String varName : variableNames) {
            var modelObject = ctx.getVariable(varName);

            result.put(varName, modelObject);
        }

        return result;
    }
}
