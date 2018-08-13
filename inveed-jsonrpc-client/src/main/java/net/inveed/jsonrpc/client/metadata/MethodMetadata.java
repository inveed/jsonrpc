package net.inveed.jsonrpc.client.metadata;

import java.util.Map;

import net.inveed.jsonrpc.client.ParamsType;

/**
 * Metadata about a Java method
 */
public class MethodMetadata {

    
    private final String name;

    
    private final ParamsType paramsType;

    /**
     * Map of method params by RPC name
     */
    
    private final Map<String, ParameterMetadata> params;

    public MethodMetadata(String name, ParamsType paramsType,  Map<String, ParameterMetadata> params) {
        this.params = params;
        this.name = name;
        this.paramsType = paramsType;
    }
    
    public Map<String, ParameterMetadata> getParams() {
        return params;
    }

    
    public String getName() {
        return name;
    }

    
    public ParamsType getParamsType() {
        return paramsType;
    }

    @Override
    public String toString() {
        return "MethodMetadata{" +
                " params=" + params +
                '}';
    }
}
