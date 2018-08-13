package net.inveed.jsonrpc.client.exception;

import java.util.Map;

import net.inveed.jsonrpc.core.domain.ErrorMessage;

/**
 * Exception that occurs when batch JSON-RPC request is not completely successful
 */
public class JsonRpcBatchException extends RuntimeException {
	private static final long serialVersionUID = 5046805856090906208L;

	/**
     * Succeeded requests
     */
    
    private Map<?, ?> successes;

    /**
     * Failed requests
     */
    
    private Map<?, ErrorMessage> errors;

    public JsonRpcBatchException(String message,  Map<?, ?> successes,  Map<?, ErrorMessage> errors) {
        super(message);
        this.successes = successes;
        this.errors = errors;
    }

    
    public Map<?, ?> getSuccesses() {
        return successes;
    }

    
    public Map<?, ErrorMessage> getErrors() {
        return errors;
    }
}
