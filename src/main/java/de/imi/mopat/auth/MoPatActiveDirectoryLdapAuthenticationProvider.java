package de.imi.mopat.auth;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;

/**
 *
 */
public class MoPatActiveDirectoryLdapAuthenticationProvider extends
    AbstractLdapAuthenticationProvider {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ConfigurationDao configurationDao;

    // Initialize every needed configuration information as a final string
    private final String className = this.getClass().getName();
    // Configuration: The name of the attribute for provider domain of LDAP
    private static final String adLdapAuthenticationDomainProperty = "activeDirectoryLdapAuthenticationProviderDomain";
    // Configuration: The name of the attribute for the provider url of LDAP
    private static final String adLdapAuthenticationUrlProperty = "activeDirectoryLdapAuthenticationProviderUrl";
    // Configuration: The name of the attribute for the default language of LDAP
    private static final String adLdapAuthenticationDefaultLanguageProperty = "activeDirectoryLdapAuthenticationProviderDefaultLanguage";
    // Configuration: The name of the attribute for the phone number of the
    // LDAP support
    private static final String adLdapAuthenticationProviderSupportPhoneProperty = "activeDirectoryLdapAuthenticationProviderSupportPhone";

    @Override
    protected DirContextOperations doAuthentication(
        final UsernamePasswordAuthenticationToken auth) {
        Boolean activatedLdap = isActiveDirectoryLDAPAuthenticationActivated();
        if (activatedLdap) {
            String domain = getActiveDirectoryLDAPDomain();
            String url = getActiveDirectoryLDAPUrl();
            ActiveDirectoryLdapAuthenticationProvider activeDirectoryAuthenticationProvider = new ActiveDirectoryLdapAuthenticationProvider(
                domain, url);
            activeDirectoryAuthenticationProvider.setConvertSubErrorCodesToExceptions(true);
            Authentication authentication = null;
            try {
                authentication = activeDirectoryAuthenticationProvider.authenticate(auth);
            } catch (AuthenticationException authenticationException) {
                return null;
            }
            DirContextOperations dirContext = new DirContextAdapter();
            return dirContext;
        }
        return null;
    }

    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(
        final DirContextOperations userData, final String username, final String password) {
        return null;
    }

    /**
     * Returns true if the LDAP authentication is activeted and false otherwise.
     *
     * @return The configured LDAPAuthentikationActivated boolean.
     */
    public Boolean isActiveDirectoryLDAPAuthenticationActivated() {
        // Configuration: The name of the attribute for the activation of LDAP
        // authentication
        String adLdapAuthenticationActivetedProperty = "activeDirectoryLdapAuthenticationProviderActivated";
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            adLdapAuthenticationActivetedProperty, className);
        return Boolean.valueOf(configuration.getValue());
    }

    /**
     * Returns the name of the LDAP domain.
     *
     * @return The the name of the LDAP domain.
     */
    public String getActiveDirectoryLDAPDomain() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            adLdapAuthenticationDomainProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the url for LDAP.
     *
     * @return The url for LDAP.
     */
    public String getActiveDirectoryLDAPUrl() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            adLdapAuthenticationUrlProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the phone number of the LDAP support.
     *
     * @return The phone number of the LDAP support.
     */
    public String getActiveDirectoryLDAPSupportPhone() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            adLdapAuthenticationProviderSupportPhoneProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the default language locale for LDAP.
     *
     * @return The default language locale for LDAP.
     */
    public Locale getActiveDirectoryLDAPDefaultLanguage() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            adLdapAuthenticationDefaultLanguageProperty, className);
        String domainDefaultLanguage = configuration.getValue();
        String[] localeSplit = domainDefaultLanguage.split("_");
        Locale domainDefaultLanguageLocale = new Locale(localeSplit[0]);
        if (localeSplit.length == 2) {
            domainDefaultLanguageLocale = new Locale(localeSplit[0], localeSplit[1]);
        }
        return domainDefaultLanguageLocale;
    }
}