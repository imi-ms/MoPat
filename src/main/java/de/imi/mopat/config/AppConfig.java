package de.imi.mopat.config;

import de.imi.mopat.helper.controller.MailSender;
import de.imi.mopat.helper.controller.PatientDataRetriever;
import de.imi.mopat.helper.controller.PatientDataRetrieverFactoryBean;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.apache.commons.lang.LocaleUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The main configuration class defining the view.
 * <p>
 * Includes other configuration classes, e.g. Security Config. Takes over functionality of old
 * spring-servlet.xml.
 */

@Configuration
@EnableWebMvc
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"de.imi.mopat.auth", "de.imi.mopat.config",
    "de.imi.mopat.controller", "de.imi.mopat.cron", "de.imi.mopat.dao", "de.imi.mopat.helper.model",
    "de.imi.mopat.helper.controller", "de.imi.mopat.io", "de.imi.mopat.io.impl",
    "de.imi.mopat.model", "de.imi.mopat.validator"})
@PropertySource("classpath:mopat.properties")
@PropertySource(value = "file:${de.imi.mopat.config.path}/${de.imi.mopat.config.name}", ignoreResourceNotFound = true)
@EnableJpaRepositories(basePackages = {"de.imi.mopat.dao"}, entityManagerFactoryRef = "MoPat")
@EnableTransactionManagement
public class AppConfig implements WebMvcConfigurer, AsyncConfigurer, EnvironmentAware {

    private Environment environment;

    /**
     * Adds validator to servlet
     *
     * @return LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(validationMessageSource());
        return validator;
    }

    @Override
    public Validator getValidator() {
        return validator();
    }

    /**
     * Adds Validation message source to servlet
     *
     * @return ReloadableResourceBundleMessageSource
     */
    @Bean
    public ReloadableResourceBundleMessageSource validationMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:message/validation/validationMessages");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Adds the standard message source to servlet
     *
     * @return ReloadableResourceBundleMessageSource
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:message/messages",
            "classpath:message/language/languageCodes", "classpath:message/language/countryCodes",
            "classpath:message/validation/validationMessages");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Adds a cache manager that uses the below defined aclCache
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        // configure and return an implementation of Spring's CacheManager SPI
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(new ConcurrentMapCache("aclCache")));
        return cacheManager;
    }

    /**
     * Adds interceptor that adds the URL parameter "lang" upon locale change
     *
     * @return LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Adds interceptors to servlet
     * <p>
     * Currently just uses locale interceptor
     *
     * @param registry InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Resolves locales from cookies
     * <p>
     * Uses "de_DE" as a fallback value
     *
     * @return CookieLocaleResolver
     */
    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(LocaleUtils.toLocale("de_DE"));
        return resolver;
    }

    /**
     * Adds resource handlers to the main servlet.
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCachePeriod(604800);
        registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(604800);
        registry.addResourceHandler("/images/**").addResourceLocations("/images/")
            .setCachePeriod(604800);
        registry.addResourceHandler("/configuration/**").addResourceLocations("/configuration/")
            .setCachePeriod(0);

    }

    /**
     * Creates PatientDataRetrieverFactoryBean
     *
     * @return PatientDataRetrieverFactoryBean
     */
    @Bean(name = "patientDataRetriever")
    public PatientDataRetrieverFactoryBean pdrfb() {
        return new PatientDataRetrieverFactoryBean();
    }

    /**
     * Adds PatientDataRetriever to servlet depending on whether it is set in the database
     * <p>
     * Since it is not advised to use NullBeans with java config it is checked whether the
     * PatientDataRetriever was set in the PatientDataRetrieverFactoryBean by comparing the toString
     * results
     *
     * @return PatientDataRetriever / null
     * @throws Exception
     */
    @Bean
    public PatientDataRetriever patientDataRetriever() throws Exception {
        PatientDataRetriever result = pdrfb().getObject();
        if (result.toString().equals("null")) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Task executor to handle
     *
     * @return TaskExecutor
     * @Scheduled and @Async annotated methods
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        return executor;
    }

    /**
     * Mail sender bean that is registered on servlet creation and is not hot reloadable
     *
     * @return MailSender
     */
    @Bean
    public MailSender mailSender() {
        return new MailSender();
    }

    /**
     * Thymeleaf template resolver.
     * <p>
     * Scans WEB-INF directory for .html files
     *
     * @return SpringResourceTemplateResolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();

        templateResolver.setPrefix("/WEB-INF/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    /**
     * Thymeleaf templating engine that translates thymeleaf expressions into static HTML files.
     * <p>
     * Registers the custom Thymleaf dialects
     *
     * @return SpringTemplateEngine
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setAdditionalDialects(
            Set.of(new LayoutDialect(), new SpringSecurityDialect()));
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    /**
     * Thymleaf view resolver that is called upon HTTP Request
     *
     * @return ThymeleafViewResolver
     */
    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
        thymeleafViewResolver.setTemplateEngine(templateEngine());
        thymeleafViewResolver.setCharacterEncoding("UTF-8");
        thymeleafViewResolver.setOrder(1);
        return thymeleafViewResolver;
    }

    /**
     * Bean to allow multi-part requests
     *
     * @return MultipartResolver
     */
    @Bean
    public MultipartResolver filterMultipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Implementation of ViewResolver that resolves a view based on the request file name or Accept
     * header
     *
     * @param contentNegotiationManager ContentNegotiationManager
     * @return ContentNegotiatingViewResolver
     */
    @Bean
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver(
        ContentNegotiationManager contentNegotiationManager) {
        ContentNegotiatingViewResolver contentNegotiatingViewResolver = new ContentNegotiatingViewResolver();

        contentNegotiatingViewResolver.setContentNegotiationManager(contentNegotiationManager);
        contentNegotiatingViewResolver.setOrder(0);
        return contentNegotiatingViewResolver;
    }

    /**
     * Configuration for ContentNegotiationManager
     *
     * @param configurer ContentNegotiationConfigurer
     */
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false).favorParameter(true).parameterName("mediaType")
            .ignoreAcceptHeader(true).useJaf(false).useRegisteredExtensionsOnly(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaTypes(Map.of("json", MediaType.APPLICATION_JSON));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

}