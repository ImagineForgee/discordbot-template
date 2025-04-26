package com.example.discordbot.managers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final MongoClient client;
    private final MongoDatabase database;
    private final ObjectMapper objectMapper;

    private DatabaseManager() {
        String mongoUrl = ConfigManager.getInstance().getString("database.mongoUrl");
        String databaseName = ConfigManager.getInstance().getString("database.name");

        ConnectionString mongoconn = new ConnectionString(mongoUrl);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(mongoconn)
                .applyToSslSettings(builder -> {
                    builder.enabled(true);
                    builder.invalidHostNameAllowed(true);
                })
                .build();

        this.client = MongoClients.create(mongoUrl);
        this.database = client.getDatabase(databaseName);
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public <T> JacksonMongoCollection<T> getCollection(
            String collectionName,
            Class<T> entityClass) {
        return JacksonMongoCollection.builder()
                .build(client, database.getName(), collectionName, entityClass, UuidRepresentation.STANDARD);
    }

    public void shutdown() {
        client.close();
    }
}
