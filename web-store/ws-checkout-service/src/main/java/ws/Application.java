package ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import ws.services.CheckoutService;



@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
@EnableDiscoveryClient
public class Application {
	
	public static void main(String[] args) {
		System.setProperty("spring.config.name", "checkout");
		System.setProperty("registration.server.hostname", "localhost");
		// java.security.Security.setProperty("networkaddress.cache.ttl", "10");
		SpringApplication.run(Application.class, args);
	}


	@Bean CheckoutService checkoutService() {
		return new CheckoutService();
	}
}
