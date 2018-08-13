package net.inveed.jsonrpc.core;

/**
 * Base interface for any HTTP exception
 * @author agelun
 *
 */
public interface IHttpMethodException {
	int getHttpStatusCode();
}
