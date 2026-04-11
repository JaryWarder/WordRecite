package example.init;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {
    private final DataSource dataSource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            boolean userExists;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    userExists = rs.getInt(1) > 0;
                }
            }
            if (userExists) {
                return;
            }

            ClassPathResource res = new ClassPathResource("db/compete_init.sql");
            if (!res.exists()) {
                return;
            }
            ScriptUtils.executeSqlScript(conn, res);
        }
    }
}
