package net.inveed.jsonrpc.server;

public interface IJsonRpcServiceProvider {

	String getMethodName(String requestMethod);

	Object getService(String requestMethod);

}
