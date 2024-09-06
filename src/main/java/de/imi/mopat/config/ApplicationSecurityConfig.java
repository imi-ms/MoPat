package de.imi.mopat.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.imi.mopat.helper.controller.NoOpAclCache;
import de.imi.mopat.auth.CustomAuthenticationFailureHandler;
import de.imi.mopat.auth.CustomPostAuthenticationChecks;
import de.imi.mopat.auth.CustomPreAuthenticationChecks;
import de.imi.mopat.auth.LDAPUserDetailsService;
import de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider;
import de.imi.mopat.auth.MoPatUserDetailService;
import de.imi.mopat.auth.PepperedBCryptPasswordEncoder;
import de.imi.mopat.auth.RoleBasedAuthenticationSuccessHandler;
import java.beans.PropertyVetoException;
import java.util.Properties;
import org.apache.groovy.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuration for the Spring security settings of the Servlet application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableCaching
public class ApplicationSecurityConfig {

    @Autowired
    ComboPooledDataSource moPatUserDataSource;

    @Autowired
    CacheManager cacheManager;

    /**
     * Register authProviders for the authManagerBuilder
     *
     * @param auth AuthManagerBuilder
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //auth.userDetailsService(userDetailsService);
        // DO NOT add userDetailsService and authenticationProvider, since two DaoAuthenticationProvider instances will be created
        auth.authenticationProvider(authenticationProviderBCrypt());
        auth.authenticationProvider(adAuthenticationProvider());
    }


    /**
     * Sets the RoleHierarchy
     *
     * @return RoleHierarchyImpl
     */
    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();

        roleHierarchy.setHierarchy("""
            ROLE_ADMIN > ROLE_MODERATOR
            ROLE_MODERATOR > ROLE_EDITOR
            ROLE_EDITOR > ROLE_ENCOUNTERMANAGER
            ROLE_ENCOUNTERMANAGER > ROLE_USER
             """);

        return roleHierarchy;
    }

    /**
     * Basic Peppered password encoder
     *
     * @return PepperedBCryptPasswordEncoder
     */
    @Bean
    public PepperedBCryptPasswordEncoder passwordEncoderBCrypt() {
        return new PepperedBCryptPasswordEncoder(10);
    }

    /**
     * Authority list for the acl cache
     *
     * @return SimpleGrantedAuthority
     */
    @Bean
    public SimpleGrantedAuthority[] authorities() {
        return new SimpleGrantedAuthority[]{new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_ADMIN")};
    }

    /**
     * Authorization strategy for acl cache
     *
     * @return AclAuthorizationStrategyImpl
     */
    @Bean
    public AclAuthorizationStrategyImpl aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(authorities());
    }

    /**
     * Create a simple audit logger bean
     *
     * @return ConsoleAuditLogger
     */
    @Bean
    public ConsoleAuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

    /**
     * Creates a permission granting strategy that just registers the logger
     *
     * @return DefaultPermissionGrantingStrategy
     */
    @Bean
    public DefaultPermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(auditLogger());
    }

    /**
     * Creates a spring based acl cache
     *
     * @return
     */
    @Bean
    public AclCache aclCache() {
        return new NoOpAclCache("aclCache");
    }

    /**
     * Creates the lookup strategy for the acl cache that uses the mopat user repository
     *
     * @return BasicLookupStrategy
     * @throws PropertyVetoException
     */
    @Bean
    public BasicLookupStrategy lookupStrategy() throws PropertyVetoException {
        return new BasicLookupStrategy(moPatUserDataSource, aclCache(), aclAuthorizationStrategy(),
            auditLogger());
    }

    /**
     * Creates a service bean for the acl cache
     *
     * @return JdbcMutableAclService
     * @throws PropertyVetoException
     */
    @Bean
    public JdbcMutableAclService aclService() throws PropertyVetoException {
        return new JdbcMutableAclService(moPatUserDataSource, lookupStrategy(), aclCache());
    }

    /**
     * Bean that evaluates the permissions for the acl service
     *
     * @return AclPermissionEvaluator
     * @throws PropertyVetoException
     */
    @Bean
    public AclPermissionEvaluator permissionEvaluator() throws PropertyVetoException {
        return new AclPermissionEvaluator(aclService());
    }

    /**
     * Expression handler that allows to use spring security for controller methods
     *
     * @return DefaultMethodSecurityExpressionHandler
     * @throws PropertyVetoException
     */
    @Bean
    public DefaultMethodSecurityExpressionHandler expressionHandler() throws PropertyVetoException {
        DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();

        defaultMethodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator());
        defaultMethodSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());

        return defaultMethodSecurityExpressionHandler;
    }

    /**
     * Bean that returns user information for authorization
     *
     * @return UserDetailsService
     */
    @Bean(name = "MoPatUserDetailsService")
    public UserDetailsService moPatUserDetailService() {
        return new MoPatUserDetailService();
    }

    /**
     * Bean that handles pre authorization checks automatically
     *
     * @return CustomPreAuthenticationChecks
     */
    @Bean
    public CustomPreAuthenticationChecks preAuthenticationChecks() {
        return new CustomPreAuthenticationChecks();
    }

    /**
     * Bean that handles post authorization checks automatically
     *
     * @return CustomPostAuthenticationChecks
     */
    @Bean
    public CustomPostAuthenticationChecks postAuthenticationChecks() {
        return new CustomPostAuthenticationChecks();
    }

    /**
     * Bean to handle authorization with the created password encryption bean and the user
     * information service
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProviderBCrypt() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(moPatUserDetailService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoderBCrypt());
        return daoAuthenticationProvider;
    }

    /**
     * Service to handle LDAP authorization requests
     *
     * @return LDAPUserDetailsService
     */
    @Bean
    public LDAPUserDetailsService LDAPUserDetailsService() {
        return new LDAPUserDetailsService();
    }

    /**
     * Registers the custom MoPat LDAP authorization service
     *
     * @return MoPatActiveDirectoryLdapAuthenticationProvider
     */
    @Bean
    public MoPatActiveDirectoryLdapAuthenticationProvider adAuthenticationProvider() {
        MoPatActiveDirectoryLdapAuthenticationProvider moPatActiveDirectoryLdapAuthenticationProvider = new MoPatActiveDirectoryLdapAuthenticationProvider();
        moPatActiveDirectoryLdapAuthenticationProvider.setUserDetailsContextMapper(
            LDAPUserDetailsService());

        return moPatActiveDirectoryLdapAuthenticationProvider;
    }

    /**
     * Bean to handle redirects for specific roles after authorization succeeded
     *
     * @return RoleBasedAuthenticationSuccessHandler
     */
    @Bean
    public RoleBasedAuthenticationSuccessHandler redirectRoleStrategy() {
        RoleBasedAuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler = new RoleBasedAuthenticationSuccessHandler();

        roleBasedAuthenticationSuccessHandler.setRoleUrlMap(
            Maps.of("ROLE_ADMIN", "/admin/index", "ROLE_ENCOUNTERMANAGER", "/mobile/survey/index",
                "ROLE_USER", "/mobile/survey/index"));
        return roleBasedAuthenticationSuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }


    /**
     * Basic filter chain for http requests
     * <p>
     * Sets the allowed resources and views that do not need authorization and that are not handled
     * in their specific controller
     * <p>
     * Also registers the authentication manager with the above defined beans
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                authz -> authz.requestMatchers(
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/conf/**")
                ).permitAll().requestMatchers(
                    new AntPathRequestMatcher("/favicon.ico")
                ).permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/mobile/user/password"),
                    new AntPathRequestMatcher("/mobile/user/passwordreset"),
                    new AntPathRequestMatcher("/mobile/user/register"),
                    new AntPathRequestMatcher("/mobile/survey/test"),
                    new AntPathRequestMatcher("/mobile/survey/questionnairetest/**"),
                    new AntPathRequestMatcher("/mobile/survey/encounter"),
                    new AntPathRequestMatcher("/mobile/survey/schedule"),
                    new AntPathRequestMatcher("/mobile/survey/questionnaireScheduled"),
                    new AntPathRequestMatcher("/mobile/survey/scores"),
                    new AntPathRequestMatcher("/mobile/survey/finishQuestionnaire"),
                    new AntPathRequestMatcher("/mobile/survey/pseudonym"),
                    new AntPathRequestMatcher("/error/maintenance"),
                    new AntPathRequestMatcher("/mobile/user/login"),
                    new AntPathRequestMatcher("/login")
                ).permitAll().anyRequest().authenticated())
            .formLogin(
                form -> form.loginPage("/mobile/user/login")
                    .loginProcessingUrl("/mobile/user/login")
                    .failureHandler(customAuthenticationFailureHandler())
                    .successHandler(redirectRoleStrategy())
            ).logout(
                logout -> logout.logoutUrl("/j_spring_security_logout")
                    .logoutSuccessUrl("/mobile/user/login")
            ).exceptionHandling(
                exceptionHandler -> exceptionHandler.accessDeniedPage("/error/accessdenied")
            ).authenticationManager(authenticationManager(http))
            .csrf(authz -> authz.disable());
        return http.build();
    }

    /**
     * Basic bean to check password correctness
     *
     * @return UsernamePasswordAuthenticationFilter
     */
    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(
        AuthenticationManager authenticationManager) {
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
        usernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager);
        return usernamePasswordAuthenticationFilter;
    }

    /**
     * Custom bean that handles redirects upon failed authentication
     *
     * @return
     */
    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        CustomAuthenticationFailureHandler customAuthenticationFailureHandler = new CustomAuthenticationFailureHandler();
        Properties properties = new Properties();

        properties.put("org.springframework.security.authentication.BadCredentialsException",
            "/mobile/user/login?message=BadCredentialsException");
        properties.put(
            "org.springframework.security.authentication.InsufficientAuthenticationException",
            "/mobile/user/login?message=InsufficientAuthenticationException");
        properties.put("org.springframework.security.authentication.DisabledException",
            "/mobile/user/login?message=DisabledException");
        customAuthenticationFailureHandler.setExceptionMappings(properties);
        return customAuthenticationFailureHandler;
    }

}
