package com.jovine360.talenthub.identity.application.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * TOTP-based MFA for admin accounts.
 * Uses {@link TimeBasedOneTimePasswordGenerator} (java-otp 0.4.0, RFC 6238).
 *
 * <p>A user's MFA secret is generated once on enrolment and stored (encrypted at rest)
 * on their user record. On each login the client supplies a 6-digit code that this
 * service verifies against a ±1-step window to tolerate clock skew.</p>
 */
@Service
public class AdminMfaService {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int CODE_DIGITS = 6;
    /** Accept codes generated within one step (30 s) of the current time. */
    private static final int STEP_TOLERANCE = 1;

    private final TimeBasedOneTimePasswordGenerator totpGenerator;

    public AdminMfaService() throws NoSuchAlgorithmException {
        this.totpGenerator = new TimeBasedOneTimePasswordGenerator(
                Duration.ofSeconds(30), CODE_DIGITS, ALGORITHM);
    }

    /**
     * Generates a new random TOTP secret encoded as Base64.
     * Store this value for the user; present it once as a QR code seed.
     */
    public String generateSecret() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(160); // 160-bit key → 20 bytes (standard for TOTP)
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Returns the TOTP code valid at the current instant for the given Base64-encoded secret.
     */
    public String generateCurrentCode(String base64Secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey key = decodeSecret(base64Secret);
        int code = totpGenerator.generateOneTimePassword(key, Instant.now());
        return String.format("%0" + CODE_DIGITS + "d", code);
    }

    /**
     * Returns {@code true} if the supplied code matches within the configured time window.
     *
     * @param base64Secret the stored Base64-encoded TOTP secret
     * @param code         the 6-digit code submitted by the user
     */
    public boolean verifyCode(String base64Secret, String code) {
        if (code == null || code.isBlank()) return false;
        try {
            SecretKey key = decodeSecret(base64Secret);
            Instant now = Instant.now();
            Duration step = totpGenerator.getTimeStep();
            for (int delta = -STEP_TOLERANCE; delta <= STEP_TOLERANCE; delta++) {
                Instant t = now.plus(step.multipliedBy(delta));
                int expected = totpGenerator.generateOneTimePassword(key, t);
                if (String.format("%0" + CODE_DIGITS + "d", expected).equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private SecretKey decodeSecret(String base64Secret) {
        byte[] raw = Base64.getDecoder().decode(base64Secret);
        return new SecretKeySpec(raw, ALGORITHM);
    }
}
