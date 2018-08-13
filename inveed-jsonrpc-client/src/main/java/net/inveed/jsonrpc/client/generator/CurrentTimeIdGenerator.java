package net.inveed.jsonrpc.client.generator;

/**
 * Return current time as id.
 * Not reliable if you need to guarantee uniqueness of request ids
 */
public class CurrentTimeIdGenerator implements IdGenerator<Long> {
    @Override
    public Long generate() {
        return System.currentTimeMillis();
    }
}
