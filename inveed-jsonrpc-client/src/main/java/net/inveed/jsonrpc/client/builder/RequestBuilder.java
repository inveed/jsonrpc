package net.inveed.jsonrpc.client.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.type.SimpleType;

import net.inveed.jsonrpc.client.Transport;
import net.inveed.jsonrpc.client.exception.JsonRpcException;
import net.inveed.jsonrpc.core.domain.ErrorMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Type-safe builder of JSON-RPC requests.
 * 
 * It introduces fluent API to build a request, set an expected response type and perform the request.
 * Builder is immutable: every mutation creates a new object, so it's safe to use in multi-threaded environment.
 * 
 * It delegates JSON processing to Jackson {@link ObjectMapper} and actual request performing to {@link net.inveed.jsonrpc.client.Transport}.
 */
public class RequestBuilder<T> extends AbstractBuilder {

    /**
     * JSON-RPC request method
     */
    
    private final String method;

    /**
     * JSON-RPC request id
     */
    
    private final ValueNode id;

    /**
     * JSON-RPC request params as a map
     */
    
    private final ObjectNode objectParams;

    /**
     * JSON-RPC request params as an array
     */
    
    private final ArrayNode arrayParams;

    /**
     * Generic type for representing expected response type
     */
    
    private final JavaType javaType;

    /**
     * Creates a new default request builder without actual parameters
     *
     * @param transport transport for request performing
     * @param mapper    mapper for JSON processing
     */
    @SuppressWarnings("deprecation")
	public RequestBuilder( Transport transport,  ObjectMapper mapper) {
        super(transport, mapper);
        id = NullNode.instance;
        objectParams = mapper.createObjectNode();
        arrayParams = mapper.createArrayNode();
        method = "";
        javaType = SimpleType.construct(Object.class);
    }

    /**
     * Creates new builder as part of a chain of builders to a full-initialized type-safe builder
     *
     * @param transport    new transport
     * @param mapper       new mapper
     * @param method       new method
     * @param id           new id
     * @param objectParams new object params
     * @param arrayParams  new array params
     * @param javaType     new response type
     */
    private RequestBuilder( Transport transport,  ObjectMapper mapper,  String method,
                            ValueNode id,  ObjectNode objectParams,  ArrayNode arrayParams,
                            JavaType javaType) {
        super(transport, mapper);
        this.method = method;
        this.id = id;
        this.objectParams = objectParams;
        this.arrayParams = arrayParams;
        this.javaType = javaType;
    }

    /**
     * Sets a request id as a long value
     *
     * @param id a  request id
     * @return new builder
     */
    
    public RequestBuilder<T> id( Long id) {
        return new RequestBuilder<T>(transport, mapper, method, new LongNode(id), objectParams, arrayParams, javaType);
    }

    /**
     * Sets a request id as an integer value
     *
     * @param id a request id
     * @return new builder
     */
    
    public RequestBuilder<T> id( Integer id) {
        return new RequestBuilder<T>(transport, mapper, method, new IntNode(id), objectParams, arrayParams, javaType);
    }

    /**
     * Sets a request id as a string value
     *
     * @param id a request id
     * @return new builder
     */
    
    public RequestBuilder<T> id( String id) {
        return new RequestBuilder<T>(transport, mapper, method, new TextNode(id), objectParams, arrayParams, javaType);
    }

    /**
     * Sets a request method
     *
     * @param method a request method
     * @return new builder
     */
    
    public RequestBuilder<T> method( String method) {
        return new RequestBuilder<T>(transport, mapper, method, id, objectParams, arrayParams, javaType);
    }

    /**
     * Adds a new parameter to current request parameters.
     * 
     * @param name  parameter name
     * @param value parameter value
     * @return new builder
     */
    
    public RequestBuilder<T> param( String name,  Object value) {
        ObjectNode newObjectParams = objectParams.deepCopy();
        newObjectParams.set(name, mapper.valueToTree(value));
        return new RequestBuilder<T>(transport, mapper, method, id, newObjectParams, arrayParams, javaType);
    }

    /**
     * Sets request parameters to request parameters.
     * Parameters are interpreted according to its positions.
     *
     * @param values array of parameters
     * @return new builder
     */
    
    public RequestBuilder<T> params( Object... values) {
        return new RequestBuilder<T>(transport, mapper, method, id, objectParams, arrayParams(values), javaType);
    }

    /**
     * Sets expected return type. This method is suitable for non-generic types
     *
     * @param responseType expected return type
     * @param <NT>         new return type
     * @return new builder
     */
    
    @SuppressWarnings("deprecation")
	public <NT> RequestBuilder<NT> returnAs( Class<NT> responseType) {
        return new RequestBuilder<NT>(transport, mapper, method, id, objectParams, arrayParams,
                SimpleType.construct(responseType));
    }

    /**
     * Sets expected return type as a list of objects
     *
     * @param elementType type of elements of a list
     * @param <E>         generic list type
     * @return new builder
     */
    
    public <E> RequestBuilder<List<E>> returnAsList( Class<E> elementType) {
        return new RequestBuilder<List<E>>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    /**
     * Sets expected return type as a set of objects
     *
     * @param elementType type of elements of a set
     * @param <E>         generic set type
     * @return new builder
     */
    
    public <E> RequestBuilder<Set<E>> returnAsSet( Class<E> elementType) {
        return new RequestBuilder<Set<E>>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructCollectionType(Set.class, elementType));
    }	


    /**
     * Sets expected return type as a collection of objects.
     * This method is suitable for non-standard collections like {@link java.util.Queue}
     *  
     * @param collectionType generic collection type 
     * @param elementType    type of elements of a collection
     * @param <E>			 type of elements of a collection
     * @return new builder
     */
    public <E> RequestBuilder<Collection<E>> returnAsCollection( Class<? extends Collection<?>> collectionType,
                                                                 Class<E> elementType) {
        return new RequestBuilder<Collection<E>>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructCollectionType(collectionType, elementType));
    }

    /**
     * Sets expected return type as an array
     *
     * @param elementType type of elements of an array
     * @param <E>         generic array type
     * @return new builder
     */
    
    public <E> RequestBuilder<E[]> returnAsArray(Class<E> elementType) {
        return new RequestBuilder<E[]>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructArrayType(elementType));
    }

    /**
     * Sets expected return type as a map of objects.
     * Because JSON type system the map should have strings as keys.
     *
     * @param mapClass  expected map interface or implementation,
     *                  e.g. {@link java.util.Map}, {@link java.util.HashMap}.
     *                  {@link java.util.LinkedHashMap}, {@link java.util.SortedMap}
     * @param valueType map value type
     * @param <V>       generic map value type
     * @return new builder
     */
    
    public <V> RequestBuilder<Map<String, V>> returnAsMap( Class<? extends Map<?,?>> mapClass,
                                                           Class<V> valueType) {
        return new RequestBuilder<Map<String, V>>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructMapType(mapClass, String.class, valueType));
    }

    /**
     * Sets expected return type as a generic type, e.g. Guava Optional.
     *
     * @param tr   type reference
     * @param <NT> a generic type
     * @return new builder
     */
    
    public <NT> RequestBuilder<NT> returnAs( TypeReference<NT> tr) {
        return new RequestBuilder<NT>(transport, mapper, method, id, objectParams, arrayParams,
                mapper.getTypeFactory().constructType(tr.getType()));
    }

    /**
     * Execute a request through {@link Transport} and convert a not null response to an expected type
     *
     * @return expected not null response
     * @throws JsonRpcException      in case of JSON-RPC error, returned by the server
     * @throws IllegalStateException if the response is null
     */
    
    public T execute() {
        T result = executeAndConvert();
        if (result == null) {
            throw new IllegalStateException("Response is null. Use 'executeNullable' if this is acceptable");
        }
        return result;
    }

    /**
     * Execute a request through {@link Transport} and convert a nullable response to an expected type
     *
     * @return expected response
     * @throws JsonRpcException in case of JSON-RPC error,  returned by the server
     */
    
    public T executeNullable() {
        return executeAndConvert();
    }

   
    private T executeAndConvert() {
        String textResponse = executeRequest();

        try {
            JsonNode responseNode = mapper.readTree(textResponse);
            JsonNode result = responseNode.get(RESULT);
            JsonNode error = responseNode.get(ERROR);
            JsonNode version = responseNode.get(JSONRPC);
            JsonNode id = responseNode.get(ID);

            if (version == null) {
                throw new IllegalStateException("Not a JSON-RPC response: " + responseNode);
            }
            if (!version.asText().equals(VERSION_2_0)) {
                throw new IllegalStateException("Bad protocol version in a response: " + responseNode);
            }
            if (id == null) {
                throw new IllegalStateException("Unspecified id in a response: " + responseNode);
            }

            if (error == null) {
                if (result != null) {
                    return mapper.convertValue(result, javaType);
                } else {
                    throw new IllegalStateException("Neither result or error is set in a response: " + responseNode);
                }
            } else {
                ErrorMessage errorMessage = mapper.treeToValue(error, ErrorMessage.class);
                throw new JsonRpcException(errorMessage);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable parse a JSON response: " + textResponse, e);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error during a response processing", e);
        }
    }

    String executeRequest() {
        ObjectNode requestNode = request(id, method, params());
        String textRequest;
        String textResponse;
        try {
            textRequest = mapper.writeValueAsString(requestNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable convert " + requestNode + " to JSON", e);
        }
        try {
            textResponse = transport.pass(textRequest);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error during a request processing", e);
        }
        return textResponse;
    }

    
    private JsonNode params() {
        if (objectParams.size() > 0) {
            if (arrayParams.size() > 0) {
                throw new IllegalArgumentException("Both object and array params are set");
            }
            return objectParams;
        }
        return arrayParams;
    }
}
