package ws.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bson.UuidRepresentation;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;



@Configuration
// @EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Override
	protected String getDatabaseName() {
		return "basket";
	}


	@Override
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(String.format("mongodb://ws-basket-mongo:27017/%s", this.getDatabaseName()));
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
			.uuidRepresentation(UuidRepresentation.STANDARD)
			.applyConnectionString(connectionString)
			.build();
		
		return MongoClients.create(mongoClientSettings);
	}
}
