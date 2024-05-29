package de.imi.mopat.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * A simplistic wrapper for the standard BCryptPasswordEncoder that add a pepper value of the former
 * ruby implementation.
 */
public class PepperedBCryptPasswordEncoder extends BCryptPasswordEncoder {

    private final String PEPPER = "AdP5ktlaIVaon53yJg8zEZSnFr33Dinil69ZtZMTWXubKMUEpfyNvOgWLdwNLhedY3WT5TVcqgg";

    /**
     * Simply call the according constructor of BCryptPasswordEncoder.
     *
     * @param strength Rounds for BCrypt encoder.
     */
    public PepperedBCryptPasswordEncoder(final int strength) {
        super(strength);
    }

    /**
     * Add the pepper before calling BCryptPasswordEncoder.
     *
     * @param rawPassword The original password.
     * @return Encoded password.
     */
    @Override
    public String encode(final CharSequence rawPassword) {
        return super.encode(rawPassword + PEPPER);
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
        return super.matches(rawPassword + PEPPER, encodedPassword);
    }
}
