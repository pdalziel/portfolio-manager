package uk.co.pm.internal.database;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

public class PortfolioManagerDB {

    private EmbeddedDatabase embeddedDb;

    public void init() {
        embeddedDb = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .addScript("classpath:schema.sql")
                .build();

    }

    public DataSource getDataSource() {
        return embeddedDb;
    }

    public void shutdown() {
        embeddedDb.shutdown();
    }
}
