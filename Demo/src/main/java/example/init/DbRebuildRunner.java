package example.init;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "db.rebuild", havingValue = "true")
public class DbRebuildRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DbRebuildRunner.class);

    private final Environment env;

    public DbRebuildRunner(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Missing datasource properties");
        }

        ParsedJdbc parsed = parseJdbcUrl(url);
        String serverUrl = "jdbc:mysql://" + parsed.hostPort + "/?" + parsed.params;
        String dbName = parsed.dbName;

        log.warn("DB rebuild enabled. Dropping and recreating database: {}", dbName);
        try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
                Statement st = conn.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS `" + dbName + "`");
            st.execute("CREATE DATABASE `" + dbName + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + parsed.hostPort + "/" + dbName + "?" + parsed.params, username, password)) {
            ClassPathResource schema = new ClassPathResource("db/compete_init.sql");
            ScriptUtils.executeSqlScript(conn, schema);

            executePrefer(conn, resolveProjectSql("CET6_patched_v2.sql"), resolveProjectSql("CET6_new.sql"));
            executePrefer(conn, resolveProjectSql("GRE_patched_v2.sql"), resolveProjectSql("GRE_new.sql"));
            executePrefer(conn, resolveProjectSql("TOEFL_patched_v2.sql"), resolveProjectSql("TOEFL_new.sql"));

            try (PreparedStatement ps = conn.prepareStatement("SELECT title, num FROM word_book ORDER BY title");
                    ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append(":").append(rs.getInt(2)).append(" ");
                }
                log.info("word_book => {}", sb.toString().trim());
            }

            logTableCount(conn, "CET6WordBook");
            logTableCount(conn, "GREWordBook");
            logTableCount(conn, "TOEFLWordBook");
        }
    }

    private static Path resolveProjectSql(String fileName) {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path direct = userDir.resolve("DB");
        if (Files.exists(direct)) {
            return direct.resolve(fileName);
        }
        Path parent = userDir.getParent();
        if (parent != null) {
            Path parentDb = parent.resolve("DB");
            if (Files.exists(parentDb)) {
                return parentDb.resolve(fileName);
            }
        }
        return userDir.resolve(fileName);
    }

    private static void executePrefer(Connection conn, Path primary, Path fallback) throws Exception {
        if (executeIfExists(conn, primary)) {
            return;
        }
        executeIfExists(conn, fallback);
    }

    private static boolean executeIfExists(Connection conn, Path filePath) throws Exception {
        FileSystemResource res = new FileSystemResource(filePath.toFile());
        if (!res.exists()) {
            log.warn("SQL file not found, skip: {}", filePath);
            return false;
        }
        log.warn("Importing SQL: {}", filePath);
        ScriptUtils.executeSqlScript(conn, new EncodedResource(res, StandardCharsets.UTF_8));
        return true;
    }

    private static void logTableCount(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            log.info("{} => {}", tableName, rs.getInt(1));
        } catch (Exception e) {
            log.warn("Count query failed for table: {}", tableName, e);
        }
    }

    private static ParsedJdbc parseJdbcUrl(String url) {
        Pattern p = Pattern.compile("^jdbc:mysql://([^/]+)/([^?]+)\\?(.*)$");
        Matcher m = p.matcher(url);
        if (!m.find()) {
            throw new IllegalArgumentException("Unsupported jdbc url: " + url);
        }
        return new ParsedJdbc(m.group(1), m.group(2), m.group(3));
    }

    private record ParsedJdbc(String hostPort, String dbName, String params) {
    }
}
