package at.qop.qopwebui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import at.qop.qoplib.LookupSessionBeans;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration.class)
@ComponentScan(basePackages = "at.qop")
@ServletComponentScan 
public class QopApplication implements CommandLineRunner {
	
    @Autowired
    private ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		SpringApplication.run(QopApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LookupSessionBeans.applicationContextStatic = applicationContext;
		
	}

}
