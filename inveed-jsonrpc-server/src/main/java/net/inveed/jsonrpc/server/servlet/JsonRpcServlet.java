package net.inveed.jsonrpc.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.google.common.base.Joiner;

import net.inveed.jsonrpc.core.annotation.JsonRpcRequestScope;
import net.inveed.jsonrpc.core.domain.Response;
import net.inveed.jsonrpc.server.HK2JsonRpcServiceProvider;
import net.inveed.jsonrpc.server.IJsonRpcRequestContext;
import net.inveed.jsonrpc.server.JsonRpcRequestScopeSingleton;
import net.inveed.rest.jpa.jackson.JsonConfiguration;

public class JsonRpcServlet extends HttpServlet {
	private static final long serialVersionUID = -4715104121483183130L;

	private final JsonConfiguration jsonConfig;
	private final HK2JsonRpcServiceProvider serviceLocator;
	
	public JsonRpcServlet() {
		ServiceLocator sl = ServiceLocatorUtilities.createAndPopulateServiceLocator();
		this.jsonConfig = new JsonConfiguration();
		this.serviceLocator = new HK2JsonRpcServiceProvider();
		this.serviceLocator.setServiceLocator(sl);
		ServiceLocatorUtilities.bind(sl, new JsonRpcRequestScopeSingleton.Binder());
		ServiceLocatorUtilities.bind(sl, new AbstractBinder() {
			@Override
			protected void configure() {
				bind(JsonRpcRequestContext.class).to(IJsonRpcRequestContext.class).proxy(false).proxyForSameScope(false).in(JsonRpcRequestScope.class);
			}
		});
	}
	
	public ServiceLocator getServiceLocator() {
		return this.serviceLocator.getServiceLocator();
	}
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setCORSHeaders(req, resp);
		super.doOptions(req, resp);
	}
	private void setCORSHeaders(HttpServletRequest req, HttpServletResponse resp) {
		String origin = req.getHeader(Headers.HEADER_ORIGIN);
		if (origin == null) {
			origin = "*";
		}
		origin = origin.trim();
		if (origin.length() == 0) {
			origin = "*";
		}
		resp.setHeader(Headers.HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		resp.setHeader(Headers.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		resp.setHeader(Headers.HEADER_ACCESS_CONTROL_ALLOW_METHODS, 
				Joiner.on(", ").join(
						Headers.METHOD_GET, 
						Headers.METHOD_POST, 
						Headers.METHOD_DELETE));			
		resp.setHeader(Headers.HEADER_ACCESS_CONTROL_ALLOW_HEADERS, 
				Joiner.on(", ").join(
						Headers.HEADER_X_REQUEST_WITH,
						Headers.HEADER_CONTENT_TYPE, 
						Headers.HEADER_X_AUTH_TOKEN,
						Headers.HEADER_COOKIE));
	}
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		JsonRpcRequestScopeSingleton st = this.serviceLocator.getServiceLocator().getService(JsonRpcRequestScopeSingleton.class);
		st.runInScope(new Runnable() {
			@Override
			public void run() {
				getRpcServiceLocator().register(new AbstractBinder() {
					@Override
					protected void configure() {
						bind(req).to(HttpServletRequest.class);
					}
				});
				IJsonRpcRequestContext ctx = serviceLocator.getServiceLocator().getService(IJsonRpcRequestContext.class);
				ctx.setHttpServletRequest(req);
				ctx.setHttpServletResponse(resp);
				
				try {
					final JsonRpcRequestHandler srv = new JsonRpcRequestHandler(jsonConfig, serviceLocator);
					Object response = srv.handle(req.getInputStream());
					
					if (response instanceof Response) {
						resp.setStatus(((Response) response).getHttpStatusCode());
					} else if (resp instanceof List<?>) {
						int status = 200;
						for (Object o : (List<?>) resp) {
							if (o instanceof Response) {
								Response r = (Response) o;
								if (r.getHttpStatusCode() == 200) {
									continue;
								}
								if (r.getHttpStatusCode() == 403) {
									status = 403;
									break;
								}
								if (r.getHttpStatusCode() == 500) {
									status = r.getHttpStatusCode();
									break;
								}
								status = Math.max(r.getHttpStatusCode(), status);
							}
						}
						resp.setStatus(status);
					}
					
					setCORSHeaders(req, resp);
					jsonConfig.getMapper().writer().writeValue(resp.getOutputStream(), response);
					resp.getOutputStream().flush();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						ctx.close();
					} catch (IOException e) {
					}
				}
			}
		});
		
	}
	
	public HK2JsonRpcServiceProvider getRpcServiceLocator() {
		return this.serviceLocator;
	}
}