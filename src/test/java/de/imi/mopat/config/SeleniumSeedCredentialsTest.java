package de.imi.mopat.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.auth.PepperedBCryptPasswordEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import static org.junit.Assert.assertTrue;

public class SeleniumSeedCredentialsTest {

    @Test
    public void reviewAccountsMatchSeleniumCredentials() throws Exception {
        String sql = Files.readString(Path.of("db/installationInit.sql"));
        JsonNode secrets = new ObjectMapper().readTree(Path.of("selenium/secrets/secret.json").toFile());
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new ResourcePropertySource("classpath:mopat.properties"));
        PepperedBCryptPasswordEncoder encoder = new PepperedBCryptPasswordEncoder(10,
            environment.getProperty("de.imi.mopat.passwordPepper"));
        for (String role : new String[]{"admin", "moderator", "editor"}) {
            String username = secrets.get(role + "-username").asText();
            String password = secrets.get(role + "-password").asText();
            Matcher row = Pattern.compile("'" + Pattern.quote(username) + "'\\s*,\\s*'([^']+)'").matcher(sql);
            assertTrue("Missing seeded account: " + role, row.find());
            assertTrue("Seeded password does not match Selenium credentials: " + role,
                encoder.matches(password, row.group(1)));
        }
    }
}