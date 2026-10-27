package com.bookmyshow.dao.jdbc;

import com.bookmyshow.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection settings and schema bootstrap for the JDBC DAOs.
 *
 * Defaults to an embedded H2 database file in ~/.bookmyseat so the app runs with
 * only a JDK installed. Point it at PostgreSQL with system properties
 * (or the matching environment variables):
 *
 *   -Dbookmyseat.db.url=jdbc:postgresql://localhost:5432/bookmyseat   (BOOKMYSEAT_DB_URL)
 *   -Dbookmyseat.db.user=postgres                                     (BOOKMYSEAT_DB_USER)
 *   -Dbookmyseat.db.password=secret                                   (BOOKMYSEAT_DB_PASSWORD)
 */
public class Database {

    private static final String H2_OPTIONS = ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Reads the connection settings from system properties / environment, falling back to embedded H2. */
    public static Database fromEnvironment() {
        String url = setting("bookmyseat.db.url", "BOOKMYSEAT_DB_URL");
        if (url == null || url.isBlank()) {
            Path file = Path.of(System.getProperty("user.home"), ".bookmyseat", "bookmyseat");
            return h2File(file);
        }
        return new Database(url,
                setting("bookmyseat.db.user", "BOOKMYSEAT_DB_USER"),
                setting("bookmyseat.db.password", "BOOKMYSEAT_DB_PASSWORD"));
    }

    /** An H2 database stored in the given file (without the .mv.db extension). */
    public static Database h2File(Path file) {
        return new Database("jdbc:h2:file:" + file.toAbsolutePath() + H2_OPTIONS, "sa", "");
    }

    /** A private in-memory H2 database, used by tests. */
    public static Database h2InMemory(String name) {
        return new Database("jdbc:h2:mem:" + name + H2_OPTIONS, "sa", "");
    }

    public String getUrl() { return url; }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Creates any missing tables. Safe to call on every start. */
    public void initSchema() {
        String script = readSchemaScript();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : splitStatements(script)) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialise database schema at " + url, e);
        }
    }

    private static String readSchemaScript() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found on the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read schema.sql", e);
        }
    }

    private static String[] splitStatements(String script) {
        StringBuilder withoutComments = new StringBuilder();
        for (String line : script.split("\\R")) {
            if (!line.trim().startsWith("--")) {
                withoutComments.append(line).append('\n');
            }
        }
        return java.util.Arrays.stream(withoutComments.toString().split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static String setting(String property, String envVar) {
        String value = System.getProperty(property);
        return value != null ? value : System.getenv(envVar);
    }
}
