package ws.services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bson.UuidRepresentation;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Override
	protected String getDatabaseName() {
		return "history";
	}


	@Override
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(String.format("mongodb://ws-history-mongo:27017/%s", this.getDatabaseName()));
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
			.uuidRepresentation(UuidRepresentation.STANDARD)
			.applyConnectionString(connectionString)
			.build();
		
		return MongoClients.create(mongoClientSettings);
	}
}
