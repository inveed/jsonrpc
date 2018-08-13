package net.inveed.jsonrpc.client.exception;

import net.inveed.jsonrpc.core.domain.ErrorMessage;

/**
 * Represents JSON-RPC error returned by a server
 */
public class JsonRpcException extends RuntimeException {
	private static final long serialVersionUID = 6081364022365368038L;
	/**
     * Actual error message
     */
    
    private ErrorMessage errorMessage;

    public JsonRpcException( ErrorMessage errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
    }

    
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
