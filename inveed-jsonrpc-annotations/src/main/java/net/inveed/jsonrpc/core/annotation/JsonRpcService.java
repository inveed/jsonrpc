package net.inveed.jsonrpc.core.annotation;

import java.lang.annotation.*;

/**
 * Mark a class as a JSON-RPC service
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface JsonRpcService {
	String value();
}
