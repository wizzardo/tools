package com.wizzardo.tools.sql;

import org.junit.After;
import org.junit.Test;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import javax.sql.ConnectionPoolDataSource;
import java.sql.SQLException;

public class PostgresTests extends DBToolsTest {
    {
        migrationsListPath = "sql/migrations_postgres.txt";
    }

    @Override
    protected ConnectionPoolDataSource createDataSource() {
        return new SimpleConnectionPoolDataSource(new SimpleDataSource(
                new ContainerDatabaseDriver(),
                "jdbc:tc:postgresql:16.2-alpine:///databasename"
        ));
    }

    @After
    public void cleanup() {
        service.withDB(c -> {
            c.prepareCall("delete from artist").executeUpdate();
            c.prepareCall("ALTER SEQUENCE artist_id_seq RESTART").executeUpdate();
            c.prepareCall("ALTER SEQUENCE song_id_seq RESTART").executeUpdate();
            c.prepareCall("delete from album").executeUpdate();
            c.prepareCall("ALTER SEQUENCE album_id_seq RESTART").executeUpdate();
            return null;
        });
    }

    @Test
    public void test_inner_select_2() throws SQLException {
    }
}
