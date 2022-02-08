package services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import controller.WebController;


@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
@EnableDiscoveryClient
public class WebService {
	public static final String WEB_URL = "http://webshop";


	public static void main(String[] args) {
		System.setProperty("spring.config.name", "web-server");
		System.setProperty("registration.server.hostname", "localhost");

		SpringApplication.run(WebService.class, args);
		Logger.getLogger(WebService.class.getName()).setLevel(Level.ALL);;
	}



	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public WebController webController() {
		return new WebController();
	}

}
