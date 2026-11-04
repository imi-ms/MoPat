package de.imi.mopat.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertTrue;

public class SeleniumSeedCredentialsTest {

    @Test
    public void reviewAccountsMatchSeleniumCredentials() throws Exception {
        String sql = Files.readString(Path.of("db/installationInit.sql"));
        JsonNode secrets = new ObjectMapper().readTree(Path.of("selenium/secrets/secret.json").toFile());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        for (String role : new String[]{"moderator", "editor"}) {
            String username = secrets.get(role + "-username").asText();
            String password = secrets.get(role + "-password").asText();
            Matcher row = Pattern.compile("'" + Pattern.quote(username) + "'\\s*,\\s*'([^']+)'").matcher(sql);
            assertTrue("Missing seeded account: " + role, row.find());
            assertTrue("Seeded password does not match Selenium credentials: " + role,
                encoder.matches(password, row.group(1)));
        }
    }
}