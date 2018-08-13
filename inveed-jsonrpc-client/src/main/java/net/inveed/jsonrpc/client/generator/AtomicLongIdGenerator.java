package net.inveed.jsonrpc.client.generator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Return id from an atomic long counter
 * It's the most reliable and straightforward way to generate identifiers
 */
public class AtomicLongIdGenerator implements IdGenerator<Long> {

    private final AtomicLong counter = new AtomicLong(0L);

    @Override
    public Long generate() {
        return counter.incrementAndGet();
    }
}
