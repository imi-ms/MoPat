package de.imi.mopat.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds several attributes to the model that should be accessible on all views
 */
@ControllerAdvice
public class RootController {

    @Autowired
    private HtmlUtilities htmlUtilities;

    @Autowired
    private ThymeleafDumper thymeleafDumper;

    @Autowired
    private ThymeleafMessageHelper thymeleafMessageHelper;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private LocaleHelper localeHelper;

    @Autowired
    private ServletContext context;
    
    /**
     * Globally set Form limit to more than 256
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(2058);
    }
    
    /**
     * Adding the parameterHelper to all pages.
     *
     * @return new ParameterHelper instance
     */
    @ModelAttribute("parameterHelper")
    public ParameterHelper parameterHelper() {
        return new ParameterHelper();
    }

    /**
     * Adds the htmlUtilities to all pages.
     *
     * @return htmlUtilities
     */
    @ModelAttribute("htmlUtilities")
    public HtmlUtilities htmlUtilities() {
        return htmlUtilities;
    }

    /**
     * Adds the ThymeleafDumper to all pages.
     *
     * @return ThymleafDumper
     */
    @ModelAttribute("dumper")
    public ThymeleafDumper dumper() {
        return this.thymeleafDumper;
    }

    /**
     * Adds a var Holder to all pages.
     *
     * @return new HashMap
     */
    @ModelAttribute("vars")
    public Map<String, Object> holder() {
        return new HashMap<>();
    }

    /**
     * Adds the Thymeleaf Message helper to all pages.
     *
     * @return thymeleafMessageHelper
     */
    @ModelAttribute("messages")
    public ThymeleafMessageHelper helper() {
        return this.thymeleafMessageHelper;
    }

    /**
     * Adds the CounterBean to all pages.
     *
     * @return new CounterBean instance
     */
    @ModelAttribute("counter")
    public CounterBean counter() {
        return new CounterBean();
    }

    /**
     * Adds the configurationDao to all pages to access its information.
     *
     * @return configurationDao
     */
    @ModelAttribute("configurationDao")
    public ConfigurationDao configurationDao() {
        return configurationDao;
    }

    /**
     * Adds the localeHelper to all pages.
     *
     * @return localeHelper
     */
    @ModelAttribute("localeHelper")
    public LocaleHelper localeHelper() {
        return localeHelper;
    }

    @ModelAttribute("contextPath")
    public String getRequestContextPath(HttpServletRequest request) {
        return request.getContextPath();
    }

    @ModelAttribute("queryString")
    public String getQueryString(HttpServletRequest request) {
        return request.getQueryString();
    }

    @ModelAttribute("requestURL")
    public String getRequestURL(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    @ModelAttribute("realPath")
    public String getRealPath() {
        return this.context.getRealPath("");
    }

    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("servletPath")
    public String getServletPath(HttpServletRequest request) {
        return request.getServletPath();
    }
}
