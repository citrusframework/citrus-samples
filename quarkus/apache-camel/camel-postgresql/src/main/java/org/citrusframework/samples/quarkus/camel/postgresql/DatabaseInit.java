package org.citrusframework.samples.quarkus.camel.postgresql;

import java.sql.SQLException;

import javax.sql.DataSource;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class DatabaseInit {

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent ev) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS headlines (id INTEGER PRIMARY KEY, headline VARCHAR(255))");
        }
    }
}
