package net.inveed.jsonrpc.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;


/**
 * Representation of a JSON-RPC request
 */
public class Request {
    private final String protocolVersion;    
    private final String method;
    private final JsonNode params;
    private final ValueNode id;

    public Request(@JsonProperty("jsonrpc") String jsonrpc,
                   @JsonProperty("method")  String method,
                   @JsonProperty("params")  JsonNode params,
                   @JsonProperty("id")  	ValueNode id) {
        this.protocolVersion = jsonrpc;
        this.method = method;
        this.id = id;
        this.params = params;
    }

    
    public String getProtocolVersion() {
        return protocolVersion;
    }

    
    public String getMethod() {
        return method;
    }

    
    public ValueNode getId() {
        return id;
    }

    
    public JsonNode getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Request{jsonrpc=" + protocolVersion + ", method=" + method + ", id=" + id + ", params=" + params + "}";
    }
}
