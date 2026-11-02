package de.imi.mopat.auth;

import java.nio.charset.StandardCharsets;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * A simplistic wrapper for the standard BCryptPasswordEncoder that add a pepper value of the former
 * ruby implementation.
 */
public class PepperedBCryptPasswordEncoder extends BCryptPasswordEncoder {
    private final String PEPPER;

    /**
     * Simply call the according constructor of BCryptPasswordEncoder.
     *
     * @param strength Rounds for BCrypt encoder.
     */
    public PepperedBCryptPasswordEncoder(final int strength, String pepper) {
        super(strength);
        this.PEPPER = pepper;
    }

    private String truncate(String rawPassword) {
        byte[] bytes = rawPassword.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 72) {
            bytes = java.util.Arrays.copyOf(bytes, 72);
        }
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Add the pepper before calling BCryptPasswordEncoder.
     *
     * @param rawPassword The original password.
     * @return Encoded password.
     */
    @Override
    public String encode(final CharSequence rawPassword) {
        return super.encode(truncate(rawPassword + PEPPER));
    }

    /**
     * Add the pepper to the raw password and call BCryptPasswordEncoder.
     *
     * @param rawPassword     The original password.
     * @param encodedPassword The encoded password.
     * @return Whether passwords match.
     */
    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return super.matches(truncate(rawPassword + PEPPER), encodedPassword);
    }
}
