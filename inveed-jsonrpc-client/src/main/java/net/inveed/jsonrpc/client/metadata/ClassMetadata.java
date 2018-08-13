package net.inveed.jsonrpc.client.metadata;

import java.lang.reflect.Method;
import java.util.Map;

import net.inveed.jsonrpc.client.ParamsType;
import net.inveed.jsonrpc.client.generator.IdGenerator;

/**
 * Metadata about a Java class
 */
public class ClassMetadata {

    
    private final ParamsType paramsType;

    
    private final IdGenerator<?> idGenerator;

    /**
     * Map of JSON-RPC 2.0 methods by rpc name
     */
    
    private final Map<Method, MethodMetadata> methods;

    public ClassMetadata( ParamsType paramsType,  IdGenerator<?> idGenerator,
                          Map<Method, MethodMetadata> methods) {
        this.paramsType = paramsType;
        this.idGenerator = idGenerator;
        this.methods = methods;
    }

    
    public ParamsType getParamsType() {
        return paramsType;
    }

    
    public IdGenerator<?> getIdGenerator() {
        return idGenerator;
    }

    
    public Map<Method, MethodMetadata> getMethods() {
        return methods;
    }
}
