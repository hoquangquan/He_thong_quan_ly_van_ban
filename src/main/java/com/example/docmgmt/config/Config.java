package com.example.docmgmt.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import javax.sql.DataSource;

public final class Config implements AutoCloseable {
    public final DataSource dataSource;
    public final MongoClient mongoClient;
    public final String mongoDb;
    public final String mongoBucket;

    private Config(DataSource ds, MongoClient mc, String db, String bucket) {
        this.dataSource = ds;
        this.mongoClient = mc;
        this.mongoDb = db;
        this.mongoBucket = bucket;
    }

    public static Config fromEnv() {
        String pgUrl = getenv("PG_URL", "jdbc:postgresql://localhost:5432/docmgmt");
        String pgUser = getenv("PG_USER", "postgres");
        String pgPass = getenv("PG_PASS", "quangquan0203");

        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(pgUrl);
        hc.setUsername(pgUser);
        hc.setPassword(pgPass);
        hc.setMaximumPoolSize(5);
        HikariDataSource ds = new HikariDataSource(hc);

        String mongoUri = getenv("MONGO_URI", "mongodb://localhost:27017");
        String mongoDb = getenv("MONGO_DB", "docmgmt");
        String bucket = getenv("MONGO_BUCKET", "files");
        MongoClient mc = MongoClients.create(mongoUri);

        return new Config(ds, mc, mongoDb, bucket);
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return v == null || v.isBlank() ? def : v;
    }

    @Override
    public void close() {
        if (dataSource instanceof HikariDataSource h) {
            h.close();
        }
        mongoClient.close();
    }
}

