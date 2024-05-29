package de.imi.mopat.helper.controller;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * At the start of the project the {@link ContextLoaderListener ContextLoaderListener} object gets
 * called. It is set as a listener in web.xml.
 */
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContextInfo.setServletContext(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        // no action here
    }
}
