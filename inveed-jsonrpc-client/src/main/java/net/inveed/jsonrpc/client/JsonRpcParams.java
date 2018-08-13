package net.inveed.jsonrpc.client;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface JsonRpcParams {

    ParamsType value() default ParamsType.MAP;
}
