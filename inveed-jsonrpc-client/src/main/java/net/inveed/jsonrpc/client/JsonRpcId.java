package net.inveed.jsonrpc.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.inveed.jsonrpc.client.generator.IdGenerator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcId {

    Class<? extends IdGenerator<?>> value();
}
