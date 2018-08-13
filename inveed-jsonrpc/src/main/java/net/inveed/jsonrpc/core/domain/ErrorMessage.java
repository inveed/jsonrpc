package net.inveed.jsonrpc.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.inveed.commons.INumberedException;

/**
 * Representation of a JSON-RPC error message
 */
public class ErrorMessage {
	public static final class ExtendedAttributes {
		public String code;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public Object[] args;

		public ExtendedAttributes(INumberedException e) {
			this.code = e.getCode().getCode();
			this.args = e.getArgs();
		}
	}

	@JsonProperty("code")
	private final long code;

	@JsonProperty("message")
	private final String message;

	@JsonProperty("extended")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final ExtendedAttributes extended;

	public ErrorMessage(@JsonProperty("code") 		long code,
						@JsonProperty("message") 	String message) {
		this(code, message, null);
	}

	public ErrorMessage(long code, String message, ExtendedAttributes extended) {
		this.code = code;
		this.message = message;
		this.extended = extended;
	}
	
	public long getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ErrorMessage{code='" + code + "', message=" + message + "}";
	}
}
