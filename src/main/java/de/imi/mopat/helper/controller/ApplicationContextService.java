package de.imi.mopat.helper.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Is needed to load beans from the application context at runtime.
 */
@Service
public class ApplicationContextService {

    @Autowired
    private ApplicationContext applicationContextHelper;

    private static ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        applicationContext = applicationContextHelper;
    }

    /**
     * Get the application context at runtime.
     *
     * @return The application context.
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
