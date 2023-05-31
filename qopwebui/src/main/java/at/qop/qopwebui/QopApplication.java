package at.qop.qopwebui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration.class)
@ComponentScan(basePackages = "at.qop")
public class QopApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(QopApplication.class, args);
	}

}
