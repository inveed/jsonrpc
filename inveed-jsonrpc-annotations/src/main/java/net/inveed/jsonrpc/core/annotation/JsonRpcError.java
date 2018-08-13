package net.inveed.jsonrpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an exception as a JSON-RPC error (with error code available)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcError {

    /**
     * JSON-RPC error code (should be in a range [-32099, -32000])
     *
     * @return error code
     */
    int code() default 0;
    
    int httpCode() default 0;

    /**
     * JSON-RPC error message.
     * If empty then the exception message will be used
     *
     * @return error message
     */
    String message() default "";
}
