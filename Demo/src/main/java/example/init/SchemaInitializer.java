package example.init;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final DataSource dataSource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (!tableExists(conn, "user")) {
                ClassPathResource res = new ClassPathResource("db/compete_init.sql");
                if (res.exists()) {
                    ScriptUtils.executeSqlScript(conn, res);
                }
                return;
            }

            boolean needPatch = false;
            needPatch |= !tableExists(conn, "word_book");
            needPatch |= !tableExists(conn, "private_books");
            needPatch |= !tableExists(conn, "daily");
            needPatch |= !tableExists(conn, "user_word_progress");
            needPatch |= !tableExists(conn, "user_word_event");

            if (!needPatch) {
                return;
            }

            Path patch = resolveProjectSql("update_missing_tables.sql");
            FileSystemResource res = new FileSystemResource(patch.toFile());
            if (!res.exists()) {
                log.warn("Missing patch sql file: {}", patch);
                return;
            }
            ScriptUtils.executeSqlScript(conn, new EncodedResource(res, StandardCharsets.UTF_8));
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
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
}
