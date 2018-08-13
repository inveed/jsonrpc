package net.inveed.jsonrpc.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Representation of a JSON-RPC error response
 */
public class ErrorResponse extends Response {
	@JsonProperty("error")
	private final ErrorMessage error;

	@JsonCreator
	public ErrorResponse(@JsonProperty("id") ValueNode id,
					     @JsonProperty("error") ErrorMessage error) {
		super(id, 500);
		this.error = error;
	}

	public ErrorResponse(ValueNode id, ErrorMessage error, int httpCode) {
		super(id, httpCode);
		this.error = error;
	}

	public ErrorResponse(ErrorMessage error, int httpCode) {
		super(NullNode.getInstance(), httpCode);
		this.error = error;
	}

	public ErrorMessage getError() {
		return error;
	}
}
