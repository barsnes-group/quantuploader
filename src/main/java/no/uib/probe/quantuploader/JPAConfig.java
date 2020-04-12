package no.uib.probe.quantuploader;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;


/*
 * @author carlos.horro
 * This class automatically generates y configures the basic beans to get access
 * to the database and persistence functionality.
 * (ie: gives you access to the main utilities of the Java Persistence API):
 * entityManagerFactory, dataSource and transactionManager
 */
@Configuration 
@ComponentScan("no.uib.probe.quantuploader.beans")
@PropertySource("classpath:application.properties")
@EnableJpaRepositories("no.uib.probe.quantuploader.repository")  //( basePackageClasses = StudiesRepository.class )// or ("no.uib.probe.repository") 
@EnableTransactionManagement
public class JPAConfig {
    
    /*
    *  The persistence unit name is the ID of the base entity (configured at a low level
    *  in src/main/resources/META-INF/persistence.xml) in charge of the persistence
    *  low-level functinality, and it is used by other high-level entities.
    */
    private final String PERSISTENCE_UNIT_NAME = "my_persistence_unit";
      
    private static final Logger LOGGER = Logger.getLogger(JPAConfig.class.getName());
    
    @Autowired
    private Environment env;
 
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() 
      throws NamingException {
        LocalContainerEntityManagerFactoryBean em 
          = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
        // it's mandatory if we want to have functionalities like custom transaction isolation
        em.setJpaDialect(new EclipseLinkJpaDialect());
        em.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        
        /* Example of extra properties if we need to add them.
        Properties properties = new Properties();
        properties.put("javax.persistence.schema-generation.create-source", "script");
        properties.put("javax.persistence.schema-generation.create-script-source", "META-INF/create_views.sql");
        em.setJpaProperties(properties);
        */
        LOGGER.log(Level.FINE, "LocalContainerEntityManagerFactoryBean set up for [" + PERSISTENCE_UNIT_NAME + "]");
        return em;
    }
 
    @Bean(name="dataSource",destroyMethod="") // fix an Spring error closing de dataSource before than expected
    public DataSource dataSource() throws NamingException {
        
        String jndiName = env.getProperty("spring.datasource.jndi-name");
        DataSource datasource = null;
        try {
            datasource = (DataSource)(new JndiTemplate().lookup(jndiName));
            LOGGER.log(Level.FINE, "JTA UserTransaction found at default JNDI location [" + jndiName + "]");
          
        }catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, "No JTA UserTransaction found at default JNDI location [" + jndiName + "]", ex);
        }
        return datasource;
    }
 
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
 
    /*
    @PostConstruct
    private void createViewsAfterDeployment(){
        AnnotationConfigApplicationContext context =
              new AnnotationConfigApplicationContext(JPAConfig.class);
        EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);  
        EntityManager em = emf.createEntityManager();
        try {
            em.createNativeQuery("CREATE OR REPLACE VIEW my_view AS select id FROM quantstudybean");
        } finally {
            em.close();
            emf.close();
        }
    }
    */
    
}
