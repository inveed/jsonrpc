package net.inveed.jsonrpc.server.servlet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import net.inveed.commons.INumberedException;
import net.inveed.commons.utils.ReflectionUtils;
import net.inveed.jsonrpc.core.IHttpMethodException;
import net.inveed.jsonrpc.core.annotation.JsonRpcError;
import net.inveed.jsonrpc.core.domain.*;
import net.inveed.jsonrpc.core.domain.ErrorMessage.ExtendedAttributes;
import net.inveed.jsonrpc.server.IJsonRpcServiceProvider;
import net.inveed.jsonrpc.server.StringsUtil;
import net.inveed.jsonrpc.server.ThrowablesUtil;
import net.inveed.jsonrpc.server.typeutils.JsonRpcMethodExt;
import net.inveed.jsonrpc.server.typeutils.JsonRpcMethodParamExt;
import net.inveed.rest.jpa.jackson.JsonConfiguration;
import net.inveed.commons.reflection.BeanTypeDesc;
import net.inveed.commons.reflection.JavaTypeDesc;
import net.inveed.commons.reflection.JavaTypeRegistry;
import net.inveed.commons.reflection.MethodMetadata;
import net.inveed.commons.reflection.ParameterMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public class JsonRpcMethodInvocationHandler {

	// Error messages
	private static final ErrorMessage METHOD_NOT_FOUND = new ErrorMessage(-32601, "Method not found");
	private static final ErrorMessage INVALID_REQUEST = new ErrorMessage(-32600, "Invalid Request");
	

	private static final int MIN_SERVER_ERROR_CODE = -32099;
	private static final int MAX_SERVER_ERROR_CODE = -32000;

	
	private static final Logger LOG = LoggerFactory.getLogger(JsonRpcMethodInvocationHandler.class);
	private static final String VERSION = "2.0";

	private final JsonConfiguration jsonConf;
	private final ObjectMapper mapper;
	private final IJsonRpcServiceProvider serviceProvider;
	private final Request request;

	private HashMap<String, JsonNode> managementParams = new HashMap<>();

	/**
	 * 
	 * @param jsonConf – JSON Configuration for request deserialization
	 * @param request  – JSON-RPC Request
	 * @param serviceProvider Service Provider
	 */
	public JsonRpcMethodInvocationHandler(JsonConfiguration jsonConf, 
										  Request request,
										  IJsonRpcServiceProvider serviceProvider) {
		this.mapper = jsonConf.getMapper();
		this.jsonConf = jsonConf;
		this.serviceProvider = serviceProvider;
		this.request = request;
	}
	
	/**
	 * Handles a runtime exception. If root exception is marked with
	 * {@link JsonRpcError} annotation, it will be converted to appropriate error
	 * message. Otherwise "Internal error" message will be returned.
	 *
	 * @param request
	 *            JSON-RPC request as a Java object
	 * @param e
	 *            invocation exception
	 * @return JSON-RPC error response
	 */
	private ErrorResponse handleError(Throwable ex) {
		Throwable rootCause = ThrowablesUtil.getRootCause(ex);
		
		int httpCode = 500;
		long jsonRpcCode = -32603;
		String message = rootCause.getMessage();
		ExtendedAttributes ea = null;
		
		JsonRpcError jsonRpcErrorAnnotation = rootCause.getClass().getAnnotation(JsonRpcError.class);
		if (jsonRpcErrorAnnotation != null) {
			jsonRpcCode = (jsonRpcErrorAnnotation.code() == 0) ? jsonRpcCode : jsonRpcErrorAnnotation.code();
			message = StringsUtil.isNullOrEmpty(jsonRpcErrorAnnotation.message()) ? rootCause.getMessage() : jsonRpcErrorAnnotation.message();
			httpCode = (jsonRpcErrorAnnotation.httpCode() > 0) ? httpCode : jsonRpcErrorAnnotation.httpCode();
			
			if (jsonRpcCode < MIN_SERVER_ERROR_CODE || jsonRpcCode > MAX_SERVER_ERROR_CODE) {
				LOG.warn("Error code=" + jsonRpcCode + " not in a range [-32099;-32000]");
			}
			if (StringsUtil.isNullOrEmpty(message)) {
				LOG.warn("Error message should not be empty");
			} 
		}
		
		if (rootCause instanceof IHttpMethodException) {
			httpCode =  ((IHttpMethodException) rootCause).getHttpStatusCode();
		}
		
		if (rootCause instanceof INumberedException) {
			INumberedException ne = (INumberedException) rootCause;
			ea = new ExtendedAttributes(ne);
			jsonRpcCode = ne.getCode().getLongValue();
		}

		return new ErrorResponse(request.getId(), new ErrorMessage(jsonRpcCode, message, ea), httpCode);
	}

	/**
	 * Performs single JSON-RPC request and return JSON-RPC response
	 *
	 * @return JSON-RPC response as a Java object
	 */
	public Response handle() {
		ValueNode id     = request.getId();
		JsonNode  params = request.getParams();
		
		ContainerNode<?> notNullParams = this.filterParams((ContainerNode<?>) params);

		LOG.debug("Handling JSON-RPC request for method {} with {} params", request.getMethod(), notNullParams.size());

		if (request.getProtocolVersion() == null || request.getMethod() == null) {
			LOG.error("Not a JSON-RPC request: " + request);
			return new ErrorResponse(id, INVALID_REQUEST);
		}

		if (!request.getProtocolVersion().equals(VERSION)) {
			LOG.error("Not a JSON-RPC 2.0 request: " + request);
			return new ErrorResponse(id, INVALID_REQUEST);
		}

		if (!params.isObject() && !params.isArray() && !params.isNull()) {
			LOG.error("Params of request: '" + request + "' should be an object, an array or null");
			return new ErrorResponse(id, INVALID_REQUEST);
		}

		Object serviceInstance = serviceProvider.getService(request.getMethod());
		if (serviceInstance == null) {
			LOG.warn("Cannot find service for method {}", request.getMethod());
			return new ErrorResponse(id, METHOD_NOT_FOUND);
		}
		LOG.debug("Got service object with type {}", serviceInstance.getClass().getName());

		String methodName = serviceProvider.getMethodName(request.getMethod());
		if (methodName == null) {
			LOG.warn("Cannot find service#method for method {}", request.getMethod());
			return new ErrorResponse(id, METHOD_NOT_FOUND);
		}

		JavaTypeDesc<?> javaType = JavaTypeRegistry.getType(serviceInstance.getClass());
		if (!(javaType instanceof BeanTypeDesc<?>)) {
			LOG.warn(serviceInstance.getClass() + " is not available as a JSON-RPC 2.0 service");
			return new ErrorResponse(id, METHOD_NOT_FOUND);
		}

		BeanTypeDesc<?> classMetadata = (BeanTypeDesc<?>) javaType;
		List<MethodMetadata> methodsForName = classMetadata.getMethods(methodName, null);
		if (methodsForName == null || methodsForName.size() == 0) {
			LOG.error("Unable find a method: '" + methodName + "' in a " + serviceInstance.getClass());
			return new ErrorResponse(id, METHOD_NOT_FOUND);
		}

		LOG.debug("Found {} methods with name {}", methodsForName.size(), methodName);
		MethodMetadata selectedMethod = null;
		Object[] methodParams = null;

		for (MethodMetadata method : methodsForName) {
			JsonRpcMethodExt me = method.getExtension(JsonRpcMethodExt.class);
			if (me == null) {
				// Method wasn't annotated for JSON-RPC invocation
				continue;
			}

			List<ParameterMetadata> requiredParams = method.getParams();
		
			//TODO: тут бы надо учитывать точность совпадения параметров запроса с параметрами метода.
		
			methodParams = convertToMethodParams(notNullParams, requiredParams);
			if (methodParams != null) {
				selectedMethod = method;
				break;
			}
		}

		if (selectedMethod == null) {
			LOG.error("Cannot find method '{}' with required params in a {}", methodName, serviceInstance.getClass());
			return new ErrorResponse(id, METHOD_NOT_FOUND);
		}

		// METHOD INVOCATION
		Object result;
		try {
			result = selectedMethod.getMethod().invoke(serviceInstance, methodParams);
		} catch (Throwable t) {
			return this.handleError(t);
		}
		if (result == null) {
			return new SuccessResponse(id, null);
		}
		// END: METHOD INVOCATION
		
		// JSON Serialization deep
		int deep = 0;
		JsonNode deepParam = this.managementParams.get("#deep");
		if (deepParam != null) {
			deep = deepParam.asInt();
		}
		
		// Serializing result
		JsonNode retNode = jsonConf.serializeToNode(result, deep);
		return new SuccessResponse(id, retNode);
	}

	private ContainerNode<?> filterParams(ContainerNode<?> params) {
		if (params == null) {
			return this.mapper.createObjectNode();
		}
		if (params.isNull()) {
			return this.mapper.createObjectNode();
		}
		if (params.isArray()) {
			return params;
		}

		if (params.isObject()) {
			HashMap<String, JsonNode> serviceParams = new HashMap<>();
			Iterator<Entry<String, JsonNode>> fields = params.fields();
			ObjectNode ret = this.mapper.createObjectNode();
			while (fields.hasNext()) {
				Entry<String, JsonNode> e = fields.next();
				if (e.getKey().startsWith("#") || e.getKey().startsWith(".")) {
					serviceParams.put(e.getKey(), e.getValue());
				} else {
					ret.set(e.getKey(), e.getValue());
				}
			}
			return ret;
		}
		return this.mapper.createObjectNode();
	}

	/**
	 * Converts JSON params to java params in the appropriate order of the invoked
	 * method
	 *
	 * @param params
	 *            json params (map or array)
	 * @param method
	 *            invoked method metadata
	 * @return array of java objects for passing to the method
	 */
	private Object[] convertToMethodParams(ContainerNode<?> params, List<ParameterMetadata> requiredParams) {
		int methodParamsSize = requiredParams.size();
		int jsonParamsSize = params.size();
		// Check amount arguments
		if (jsonParamsSize > methodParamsSize) {
			return null;
		}

		Object[] methodParams = new Object[methodParamsSize];
		int processed = 0;
		for (int index = 0; index < requiredParams.size(); index++) {
			ParameterMetadata param = requiredParams.get(index);
			Class<?> parameterType = param.getType();
			String name;
			JsonRpcMethodParamExt pext = param.getExtension(JsonRpcMethodParamExt.class);
			if (pext != null) {
				name = pext.getName();
			} else {
				name = param.getName();
			}

			JsonNode jsonNode;
			if (name != null) {
				jsonNode = params.isObject() ? params.get(name) : params.get(index);
			} else {
				if (params.isObject()) {
					jsonNode = null;
				} else {
					jsonNode = params.get(index);
				}
			}
			// Handle omitted value
			if (jsonNode == null || jsonNode.isNull()) {
				if (pext != null) {
					if (!pext.isRequired()) {
						methodParams[index] = getDefaultValue(parameterType);
						if (jsonNode != null) {
							processed++;
						}
						continue;
					} else {
						return null;
						// throw new IllegalArgumentException("Mandatory parameter '" + name + "' of a
						// method is not set");
					}
				} else {
					methodParams[index] = getDefaultValue(parameterType);
					if (jsonNode != null) {
						processed++;
					}
					continue;
				}
			}

			// Convert JSON object to an actual Java object
			try {
				if (param.getType() == String.class && jsonNode.isObject()) {
					methodParams[index] = this.mapper.writeValueAsString(jsonNode);
					processed++;
					continue;
				}
				JsonParser jsonParser = this.mapper.treeAsTokens(jsonNode);
				if (param.getType() == JsonParser.class) {
					methodParams[index] = jsonParser;
				} else {
					JavaType javaType = this.mapper.getTypeFactory().constructType(param.getGenericType());
					methodParams[index] = this.mapper.readValue(jsonParser, javaType);
				}
				processed++;
			} catch (IOException e) {
				return null;
			}
		}

		// Check that some unprocessed parameters were not passed
		if (processed < jsonParamsSize) {
			return null;
		}

		return methodParams;
	}

	private Object getDefaultValue(Class<?> type) {
		if (type == Optional.class) {
			// If it's Guava optional then handle it as an absent value
			return Optional.empty();
		} else if (type.isPrimitive()) {
			// If parameter is a primitive set the appropriate default value
			return ReflectionUtils.defaultValue(type);
		}
		return null;
	}

}
