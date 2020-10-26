package webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@SpringBootApplication
public class WebApplication {

	/*
	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WebApplication.class);
		app.setDefaultProperties(Collections
				.singletonMap("server.port", "8080"));
		app.run(args);
	}

/*
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/annotate-javaconfig").allowedOrigins("http://localhost:8080");
			}
		};
	}
*/

}
