package net.inveed.jsonrpc.client.generator;

/**
 * Generate secure random positive long identifiers
 */
public class SecureRandomLongIdGenerator extends SecureRandomIdGenerator<Long> {

    @Override
    public Long generate() {
        return secureRandom.nextLong() >>> 1;
    }
}
