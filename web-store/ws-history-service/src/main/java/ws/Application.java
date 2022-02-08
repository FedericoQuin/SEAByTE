package ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import ws.services.HistoryService;



@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableMongoRepositories("ws.repository")
public class Application {
	
	public static void main(String[] args) {
		System.setProperty("spring.config.name", "history");
		System.setProperty("registration.server.hostname", "localhost");
		SpringApplication.run(Application.class, args);
	}


	@Bean HistoryService historyService() {
		return new HistoryService();
	}
}
