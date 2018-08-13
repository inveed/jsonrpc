package net.inveed.jsonrpc.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Base representation of a JSON-RPC response (success or error)
 */
public class Response {

	private static final String VERSION = "2.0";

	@JsonProperty("jsonrpc")
	private final String jsonrpc;

	@JsonProperty("id")
	private final ValueNode id;
	
	@JsonIgnore
	private final int httpStatusCode;

	public Response(ValueNode id, int httpCode) {
		this.id = id;
		jsonrpc = VERSION;
		this.httpStatusCode = httpCode;
	}

	public Response(ValueNode id, String jsonrpc, int httpCode) {
		this.id = id;
		this.jsonrpc = jsonrpc;
		this.httpStatusCode = httpCode;
	}

	public String getJsonrpc() {
		return jsonrpc;
	}

	public ValueNode getId() {
		return id;
	}
	
	@JsonIgnore
	public int getHttpStatusCode() {
		return this.httpStatusCode;
	}
}
