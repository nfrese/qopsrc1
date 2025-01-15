package at.qop.qopwebui;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.vaadin.server.VaadinServlet;

import at.qop.qoplib.Config;
import at.qop.qopwebui.QopUI.QopUIServlet;
import at.qop.qopwebui.admin.AdminUI;

import org.apache.catalina.Context;

@Configuration
@EnableTransactionManagement
//@EnableJpaRepositories(entityManagerFactoryRef = "someEntityManagerFactory", transactionManagerRef = "someTransactionManager", basePackages = {
//"com.example.*" })
@EntityScan(basePackages = "at.qop")
public class DBConfigurationBean {

//    @Bean
//    public LocalSessionFactoryBean sessionFactory() {
//        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource());
//        sessionFactory.setPackagesToScan(new String[]
//          {"at.qop" });
//        sessionFactory.setHibernateProperties(hibernateProperties());
//
//        return sessionFactory;
//    }

    @Bean
    public DataSource dataSource() {
    	
    	Config cfgFile = Config.read();
    	
    	String host = cfgFile.getDbHost();
    	String db = cfgFile.getDb();
    	String schema = cfgFile.getDbSchema();
    	String user = cfgFile.getDbUserName();
    	String password = cfgFile.getDbPasswd();
    	
    	int port =cfgFile.getPort();
    	
    	DataSource dataSource = DataSourceBuilder.create()
    			.url("jdbc:postgresql://"+host+":"+port+"/"+db+"")
    			.username(user).password(password).build();

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
	
	@Bean(name="springBootServletRegistrationBean")
    public ServletRegistrationBean<?> servletRegistrationBean2() {
		VaadinServlet servlet = new AdminUI.AdminUIServlet();
		return new ServletRegistrationBean<>(servlet, "/qopwebui/admin/*");
    }
}