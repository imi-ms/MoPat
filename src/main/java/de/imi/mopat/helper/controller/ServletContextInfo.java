package de.imi.mopat.helper.controller;

import jakarta.servlet.ServletContext;

/**
 * Contains the {@link ServletContext ServletContext} object during the running project. The
 * {@link ServletContext ServletContext} object gets set by the
 * {@link ContextLoaderListener ContextLoaderListener} class during the start of the project.
 */
public class ServletContextInfo {

    private static ServletContext servletContext;

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(final ServletContext _servletContext) {
        assert _servletContext != null : "The ServletContext was null";
        servletContext = _servletContext;
    }
}
