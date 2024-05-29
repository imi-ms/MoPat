package de.imi.mopat.config;

import jakarta.servlet.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.context.WebApplicationContext;

import java.util.EnumSet;

/**
 * Main initializer class to load servlet
 */
@Configuration
public class MvcWebApplicationInitializer extends
    AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * Adds root configuration to servlet
     *
     * @return AppConfig.class
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppConfig.class};
    }

    /**
     * Returns servlet config class. Is currently part of AppConfig
     *
     * @return None configured
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    /**
     * Returns the servlet mappings as String array.
     *
     * @return Base path with all servlets are mapped to.
     */

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /**
     * Things to do on application start
     * <p>
     * Filters should be added here
     *
     * @param servletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        FilterRegistration.Dynamic charsetFilter;
        charsetFilter = servletContext.addFilter("charsetFilter",
            new CharacterEncodingFilter("UTF-8", true));
        charsetFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic springMultipartFilter;
        springMultipartFilter = servletContext.addFilter("springMultipartFilter",
            new MultipartFilter());
        springMultipartFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic springSecurityFilterChain;
        springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain",
            new DelegatingFilterProxy());
        springSecurityFilterChain.addMappingForUrlPatterns(
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
                DispatcherType.ERROR), true, "/*");
    }

    /**
     * Central dispatcher for HTTP request handlers/controllers
     *
     * @param servletAppContext
     * @return DispatcherServlet
     */
    @Override
    protected DispatcherServlet createDispatcherServlet(WebApplicationContext servletAppContext) {
        return (DispatcherServlet) super.createDispatcherServlet(servletAppContext);
    }

    @Override
    protected void registerContextLoaderListener(ServletContext servletContext) {
        super.registerContextLoaderListener(servletContext);
    }

    /**
     * Customizes registration and adds support for multipart uploads.
     *
     * @param registration Servlet registration.
     */

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        long maxSize = 10L * 1024 * 1024 * 1024;

        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(null, maxSize,
            maxSize * 2, (int) (maxSize / 2));

        registration.setMultipartConfig(multipartConfigElement);
    }


    /**
     * Sets Application filters of the servlet
     *
     * @return Filter[]
     */
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{};
    }

}