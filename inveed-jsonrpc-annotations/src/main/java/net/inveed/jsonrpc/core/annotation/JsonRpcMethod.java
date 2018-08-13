package net.inveed.jsonrpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as allowed for calling from the web.
 * Makes sense only for public non-static methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcMethod {

    /**
     * Method RPC name. By default the actual method name is used.
     *
     * @return method RPC name
     */
    String value() default "";
}
