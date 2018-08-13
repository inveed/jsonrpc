package net.inveed.jsonrpc.server.servlet;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.inveed.jsonrpc.core.annotation.JsonRpcRequestScope;
import net.inveed.jsonrpc.server.IJsonRpcRequestContext;

@JsonRpcRequestScope
public class JsonRpcRequestContext implements IJsonRpcRequestContext, Closeable {
	private final ArrayList<Closeable> closeables = new ArrayList<>();
	private HttpServletRequest  httpServletRequest;
	private HttpServletResponse httpServletResponse;
	
	@Override
	public void registerCloseable(Closeable o) {
		this.closeables.add(o);
	}
	
	@Override
	public void close() {
		for (Closeable c : this.closeables) {
			try {
				c.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void setHttpServletRequest(HttpServletRequest req) {
		this.httpServletRequest = req;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return this.httpServletRequest;
	}

	@Override
	public void setHttpServletResponse(HttpServletResponse resp) {
		this.httpServletResponse = resp;
	}

	@Override
	public HttpServletResponse getHttpServletResponse() {
		return this.httpServletResponse;
	}
}
