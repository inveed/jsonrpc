package net.inveed.jsonrpc.client.generator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Abstract generator of secure random identifiers
 */
abstract class SecureRandomIdGenerator<T> implements IdGenerator<T> {

    private static final String SHA_1_PRNG = "SHA1PRNG";

    
    protected final SecureRandom secureRandom;

    protected SecureRandomIdGenerator() {
        secureRandom = initSecureRandom();
    }

    
    private static SecureRandom initSecureRandom() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SHA_1_PRNG);
            secureRandom.nextBytes(new byte[]{}); // Important to seed immediately after creation
            return secureRandom;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
