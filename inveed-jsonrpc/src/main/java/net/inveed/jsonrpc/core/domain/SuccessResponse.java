package net.inveed.jsonrpc.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Representation of a successful JSON-RPC response
 */
public class SuccessResponse extends Response {

    
    @JsonProperty("result")
    private final Object result;

    public SuccessResponse(@JsonProperty("id")  ValueNode id,
                           @JsonProperty("result")  JsonNode result) {
        super(id, 200);
        this.result = result;
    }

    
    public Object getResult() {
        return result;
    }
}
