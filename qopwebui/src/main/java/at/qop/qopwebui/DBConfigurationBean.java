package at.qop.qopwebui;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.vaadin.server.VaadinServlet;

import at.qop.qopwebui.QopUI.QopUIServlet;

import org.apache.catalina.Context;

@Configuration
@EnableTransactionManagement
public class DBConfigurationBean {

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(new String[]
          {"at.qop" });
        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
    	
    	String host = "localhost";
    	String db = "qop";
    	String user = "qopuser";
    	String password = "qoppostgis";
    	int port =5432;
    	
    	DataSource dataSource = DataSourceBuilder.create().url("jdbc:postgresql://"+host+":"+port+"/"+db).username(user).password(password).build();
    	
    	//SingleConnectionDataSource dataSource = new SingleConnectionDataSource(, user, password, false);
//    	BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.h2.Driver");
//        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
//        dataSource.setUsername("sa");
//        dataSource.setPassword("sa");

        return dataSource;
    }

//    @Bean
//    public PlatformTransactionManager hibernateTransactionManager() {
//        HibernateTransactionManager transactionManager
//          = new HibernateTransactionManager();
//        transactionManager.setSessionFactory(sessionFactory().getObject());
//        return transactionManager;
//    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
//        hibernateProperties.setProperty(
//          "hibernate.hbm2ddl.auto", "create-drop");
//        hibernateProperties.setProperty(
//          "hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        //hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.spatial.dialect.postgis.PostgisDialect" );
        hibernateProperties.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver" );
        hibernateProperties.setProperty("hibernate.show_sql", "true" );
        hibernateProperties.setProperty("hibernate.cache.default_cache_concurrency_strategy"
           , "nonstrict-read-write" );
//        hibernateProperties.setProperty("hibernate.cache.region.factory_class"
//           , "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
        hibernateProperties.setProperty("hibernate.max_fetch_depth", "1" );

        return hibernateProperties;
    }
    
//    @Bean
//    public TomcatServletWebServerFactory tomcatFactory() {
//      return new TomcatServletWebServerFactory() {
//        @Override
//        protected void postProcessContext(Context context) {
//          ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
//        }
//      };
//    }
    
	@Bean(name="springBootServletRegistrationBean")
    public ServletRegistrationBean<?> servletRegistrationBean() {
		VaadinServlet servlet = new QopUIServlet();
		return new ServletRegistrationBean<>(servlet, "/qopwebui/*");
    }
}