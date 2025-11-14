package de.imi.mopat.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Persistence configuration that registers the different data sources that MoPat needs
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfig implements EnvironmentAware {

    private Environment environment;

    /**
     * Creates an abstract data source with a configurations that all other data sources share
     *
     * @return ComboPooledDataSource
     * @throws PropertyVetoException
     */
    public ComboPooledDataSource abstractDataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        dataSource.setDriverClass("com.mysql.cj.jdbc.Driver");
        dataSource.setConnectionTesterClassName("com.mchange.v2.c3p0.impl.DefaultConnectionTester");
        dataSource.setUser(environment.getProperty("de.imi.mopat.datasource.user"));
        dataSource.setPassword(environment.getProperty("de.imi.mopat.datasource.password"));
        dataSource.setInitialPoolSize(5);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(30);
        dataSource.setAcquireIncrement(2);
        dataSource.setMaxIdleTime(300);
        dataSource.setIdleConnectionTestPeriod(50);
        dataSource.setMaxIdleTimeExcessConnections(120);
        dataSource.setTestConnectionOnCheckin(true);
        dataSource.setPreferredTestQuery("SELECT 1");

        return dataSource;
    }

    /**
     * Creates a data source that connects to the main database of MoPat
     *
     * @return ComboPooledDataSource
     * @throws PropertyVetoException
     */
    @Bean
    public ComboPooledDataSource moPatDataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = abstractDataSource();
        dataSource.setJdbcUrl(
            environment.getProperty("de.imi.mopat.datasource.mopatDataSource.jdbc-url"));
        return dataSource;
    }

    /**
     * Creates the local entity manager that scans all model classes of mopat and connects them to
     * the data source
     *
     * @return LocalContainerEntityManagerFactoryBean
     * @throws PropertyVetoException
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean MoPat() throws PropertyVetoException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factoryBean.setDataSource(moPatDataSource());

        factoryBean.setPackagesToScan("de.imi.mopat.model", "de.imi.mopat.model.conditions",
            "de.imi.mopat.model.dto", "de.imi.mopat.model.enumeration", "de.imi.mopat.model.score",
            "de.imi.mopat.model.user");

        factoryBean.setPersistenceUnitName("MoPat");

        factoryBean.setPersistenceProvider(persistenceProvider());

        Properties jpaProperties = new Properties();
        jpaProperties.put("eclipselink.weaving", "static");
        jpaProperties.put("eclipselink.ddl-generation", "create-tables");

        factoryBean.setJpaProperties(jpaProperties);

        return factoryBean;
    }

    /**
     * Transaction manager for the MoPat datasource
     *
     * @return PlatformTransactionManager
     * @throws PropertyVetoException
     */
    @Bean
    @Qualifier("MoPat")
    public PlatformTransactionManager myTxManagerMoPat() throws PropertyVetoException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(MoPat().getObject());
        return transactionManager;
    }

    /**
     * Creates a data source that connects to the user database of MoPat
     *
     * @return ComboPooledDataSource
     * @throws PropertyVetoException
     */
    @Bean
    public ComboPooledDataSource moPatUserDataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = abstractDataSource();
        dataSource.setJdbcUrl(
            environment.getProperty("de.imi.mopat.datasource.mopat_userDataSource.jdbc-url"));
        return dataSource;

    }

    /**
     * Creates the local entity manager that scans all user model classes of mopat and connects them
     * to the user data source
     *
     * @return LocalContainerEntityManagerFactoryBean
     * @throws PropertyVetoException
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean MoPat_User() throws PropertyVetoException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factoryBean.setDataSource(moPatUserDataSource());

        factoryBean.setPackagesToScan("de.imi.mopat.model.user");

        factoryBean.setPersistenceProvider(persistenceProvider());
        factoryBean.setPersistenceUnitName("MoPat_User");

        Properties jpaProperties = new Properties();
        jpaProperties.put("eclipselink.weaving", "static");

        factoryBean.setJpaProperties(jpaProperties);

        return factoryBean;
    }

    /**
     * Transaction manager for user data source
     *
     * @return PlatformTransactionManager
     * @throws PropertyVetoException
     */
    @Bean
    @Qualifier("MoPat_User")
    public PlatformTransactionManager myTxManagerMoPatUser() throws PropertyVetoException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(MoPat_User().getObject());
        return transactionManager;
    }

    /**
     * Creates a data source that connects to the audit database of MoPat
     *
     * @return ComboPooledDataSource
     * @throws PropertyVetoException
     */
    @Bean
    public ComboPooledDataSource moPatAuditDataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = abstractDataSource();
        dataSource.setJdbcUrl(
            environment.getProperty("de.imi.mopat.datasource.mopat_auditDataSource.jdbc-url"));
        return dataSource;
    }

    /**
     * Creates an entity manager that connects the audit model classes to the audit data source
     *
     * @return LocalContainerEntityManagerFactoryBean
     * @throws PropertyVetoException
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean MoPat_Audit() throws PropertyVetoException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factoryBean.setDataSource(moPatAuditDataSource());

        factoryBean.setPackagesToScan("de.imi.mopat.model");

        factoryBean.setPersistenceProvider(persistenceProvider());
        factoryBean.setPersistenceUnitName("MoPat_Audit");

        Properties jpaProperties = new Properties();
        jpaProperties.put("eclipselink.weaving", "static");

        factoryBean.setJpaProperties(jpaProperties);

        return factoryBean;
    }

    /**
     * Creates a transaction manager for the audit entity manager
     *
     * @return PlatformTransactionManager
     * @throws PropertyVetoException
     */
    @Bean
    @Qualifier("MoPat_Audit")
    public PlatformTransactionManager myTxManagerMoPatAudit() throws PropertyVetoException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(MoPat_Audit().getObject());
        return transactionManager;
    }

    /**
     * Allows the use of persistence annotations in Spring
     *
     * @return PersistenceAnnotationBeanPostProcessor
     */
    @Bean
    public PersistenceAnnotationBeanPostProcessor annotationBeanPostProcessor() {
        return new PersistenceAnnotationBeanPostProcessor();
    }

    /**
     * Persistence provider bean that is needed for the entity manager
     *
     * @return PersistenceProvider
     */
    @Bean
    public PersistenceProvider persistenceProvider() {
        return new PersistenceProvider();
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
