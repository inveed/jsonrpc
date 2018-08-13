package net.inveed.jsonrpc.client.generator;

/**
 * Strategy for generation request identificators
 */
public interface IdGenerator<T> {

    T generate();
}
