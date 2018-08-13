package net.inveed.jsonrpc.server.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.inveed.jsonrpc.core.domain.*;
import net.inveed.jsonrpc.server.IJsonRpcServiceProvider;
import net.inveed.rest.jpa.jackson.JsonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonRpcRequestHandler {

	// Error messages
	private static final ErrorMessage PARSE_ERROR = new ErrorMessage(-32700, "Cannot parse JSON-RPC request.", null);
	private static final ErrorMessage INVALID_REQUEST = new ErrorMessage(-32600, "Invalid Request", null);
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonRpcRequestHandler.class);

	private final JsonConfiguration mapper;
	private final IJsonRpcServiceProvider serviceProvider;

	/**
	 * Init JSON-RPC server
	 * 
	 * @param mapper			JSON Configuration
	 * @param serviceProvider	Service Provider
	 */
	public JsonRpcRequestHandler(JsonConfiguration mapper, IJsonRpcServiceProvider serviceProvider) {
		this.mapper = mapper;
		this.serviceProvider = serviceProvider;
	}

	/**
	 * Handles a JSON-RPC request(single or batch), delegates processing to the
	 * service, and returns a JSON-RPC response.
	 * 
	 * @param inputStream HTTP Input Stream
	 * @return {@link Response} or list of responses
	 */
	public Object handle(InputStream inputStream) {
		JsonNode rootRequest;
		try {
			rootRequest = mapper.getMapper().readTree(inputStream);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Request : {}", mapper.getMapper().writeValueAsString(rootRequest));
			}
		} catch (IOException e) {
			LOG.error(PARSE_ERROR.getMessage(), e);
			return new ErrorResponse(PARSE_ERROR, 400);
		}
		return this.handle(rootRequest);
	}

	/**
	 * 
	 * @param 	rootRequest Request
	 * @return {@link Response} or list of responses
	 */
	public Object handle(JsonNode rootRequest) {
		// Check if a single request or a batch
		if (rootRequest.isObject()) {
			Response response = handleSingleRequest(rootRequest);
			return isNotification(rootRequest, response) ? null : response;
		} else if (rootRequest.isArray() && rootRequest.size() > 0) {
			List<Response> responses = new ArrayList<>();
			for (JsonNode request : (ArrayNode) rootRequest) {
				if (!request.isObject()) {
					responses.add(new ErrorResponse(INVALID_REQUEST, 400));
				}
				Response response = handleSingleRequest(request);
				if (!isNotification(request, response)) {
					responses.add(response);
				}
			}
			return responses.size() > 0 ? responses.toArray(new Response[0]) : null;
		}

		LOG.error("Invalid JSON-RPC request: " + rootRequest);
		return new ErrorResponse(INVALID_REQUEST, 400);
	}

	/**
	 * Check if request is a "notification request" according to the standard.
	 *
	 * @param requestNode
	 *            a request in a JSON tree format
	 * @param response
	 *            a response in a Java object format
	 * @return {@code true} if a request is a "notification request"
	 */
	private static boolean isNotification(JsonNode requestNode, Response response) {
		// Notification request doesn't have "id" field
		if (requestNode.get("id") == null) {
			if (response instanceof SuccessResponse) {
				return true;
			} else if (response instanceof ErrorResponse) {
				// Notification request should be a valid JSON-RPC request.
				// So if we get "Parse error" or "Invalid request"
				// we can't consider the request as a notification
				long errorCode = ((ErrorResponse) response).getError().getCode();
				if (errorCode != PARSE_ERROR.getCode() && errorCode != INVALID_REQUEST.getCode()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Wrapper around a single JSON-RPC request. Checks that a request is valid
	 * JSON-RPC object and handle runtime errors in the request processing.
	 *
	 * @param requestNode
	 *            JSON-RPC request as a JSON tree
	 * @param service
	 *            service object
	 * @return JSON-RPC response as a Java object
	 */
	private Response handleSingleRequest(JsonNode requestNode) {
		Request request;

		try {
			request = mapper.getMapper().convertValue(requestNode, Request.class);
		} catch (Exception e) {
			LOG.error("Invalid JSON-RPC request: " + requestNode, e);
			return new ErrorResponse(INVALID_REQUEST, 400);
		}

		JsonRpcMethodInvocationHandler handler = new JsonRpcMethodInvocationHandler(
				this.mapper, 
				request,
				this.serviceProvider);
		return handler.handle();
	}
}
