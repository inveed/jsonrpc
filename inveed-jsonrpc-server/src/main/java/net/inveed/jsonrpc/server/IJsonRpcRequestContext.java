package net.inveed.jsonrpc.server;

import java.io.Closeable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IJsonRpcRequestContext extends Closeable {
	public void registerCloseable(Closeable o);
	public void setHttpServletRequest(HttpServletRequest req);
	public HttpServletRequest getHttpServletRequest();
	public void setHttpServletResponse(HttpServletResponse resp);
	public HttpServletResponse getHttpServletResponse();
}
