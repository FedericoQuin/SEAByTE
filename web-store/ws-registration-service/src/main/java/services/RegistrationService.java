package services;

import java.net.UnknownHostException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;



@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class
})
@EnableEurekaServer
public class RegistrationService {
	public static void main(String[] args) throws UnknownHostException {
		System.setProperty("spring.config.name", "registration");
		new SpringApplicationBuilder(RegistrationService.class).run(args);
	}
}
