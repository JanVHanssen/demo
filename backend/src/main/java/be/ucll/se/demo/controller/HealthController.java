package be.ucll.se.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/health/database")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            response.put("status", "Connected");
            response.put("url", conn.getMetaData().getURL());
            response.put("driver", conn.getMetaData().getDriverName());

            // Check tables
            List<String> tables = new ArrayList<>();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            response.put("tables", tables);

            // Check if flyway_schema_history exists
            response.put("flywayTableExists", tables.contains("flyway_schema_history"));
            response.put("notificationsTableExists", tables.contains("notifications"));

            return response;

        } catch (Exception e) {
            response.put("status", "Error");
            response.put("error", e.getMessage());
            return response;
        }
    }

    @GetMapping("/api/health/flyway")
    public Map<String, Object> checkFlyway() {
        Map<String, Object> response = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // Check flyway_schema_history table
            if (tableExists(conn, "flyway_schema_history")) {
                List<Map<String, Object>> migrations = new ArrayList<>();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT version, description, type, script, checksum, installed_on, execution_time, success " +
                                "FROM flyway_schema_history ORDER BY installed_rank");

                while (rs.next()) {
                    Map<String, Object> migration = new HashMap<>();
                    migration.put("version", rs.getString("version"));
                    migration.put("description", rs.getString("description"));
                    migration.put("type", rs.getString("type"));
                    migration.put("script", rs.getString("script"));
                    migration.put("installed_on", rs.getTimestamp("installed_on"));
                    migration.put("success", rs.getBoolean("success"));
                    migrations.add(migration);
                }

                response.put("migrations", migrations);
                response.put("migrationsCount", migrations.size());
            } else {
                response.put("error", "flyway_schema_history table not found - Flyway not initialized");
            }

        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[] { "TABLE" });
        return rs.next();
    }
}