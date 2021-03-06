package net.inveed.jsonrpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Because Java doesn't retain information about method names in a class file and
 * therefore this information is not available in runtime, this annotation MUST
 * be placed on all the method parameters.
 * 
 * Otherwise {@link IllegalArgumentException} will be generated in runtime and
 * an error message will be returned to a client.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcParam {

    /**
     * RPC method parameter name. <b>MUST</b> be specified.
     *
     * @return parameter name
     */
    public String value();

    /**
     * Whether parameter is required
     *
     * If {@code false}, it means that a client isn't forced to pass this parameter to the method.
     * If the client doesn't provide the parameter, {@code null} value is used for complex types
     * and an appropriate default value for primitives.
     * @return whether parameter is required
     */
    public boolean required() default true;
}
