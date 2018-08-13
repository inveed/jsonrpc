package net.inveed.jsonrpc.server;

import java.util.HashMap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.inveed.jsonrpc.server.typeutils.JsonRpcTypeExt;
import net.inveed.typeutils.BeanTypeDesc;

public class HK2JsonRpcServiceProvider implements IJsonRpcServiceProvider {
	private static final Logger LOG = LoggerFactory.getLogger(HK2JsonRpcServiceProvider.class);
	
	private ServiceLocator locator;
	private final HashMap<String, Class<?>> services = new HashMap<>();

	@Override
	public Object getService(String requestMethod) {
		String[] ma = requestMethod.split("#");
		if (ma.length != 2) {
			return null;
		}
		
		BeanTypeDesc<?> retType = JsonRpcTypeExt.getService(ma[0]);
		
		if (this.getServiceLocator() == null) {
			return retType.newInstance();
		}
		
		return this.getServiceLocator().getService(retType.getType());
	}

	@Override
	public String getMethodName(String requestMethod) {
		String[] ma = requestMethod.split("#");
		if (ma.length != 2) {
			return null;
		}
		return ma[1];
	}
	public ServiceLocator getServiceLocator() {
		return locator;
	}
	public void setServiceLocator(ServiceLocator locator) {
		this.locator = locator;
	}
	
	public void register(Binder binder) {
		ServiceLocatorUtilities.bind(this.getServiceLocator(), binder);
	}
	
	public void registerService(String name, Class<?> service) {
		assert(name != null);
		this.services.put(name, service);
		ServiceLocatorUtilities.addClasses(this.getServiceLocator(), service);
	}

	public void registerService(BeanTypeDesc<?> service) {
		@SuppressWarnings("unchecked")
		JsonRpcTypeExt<?> rpcService = service.getExtension(JsonRpcTypeExt.class);
		if (rpcService == null) {
			LOG.error("Cannor register bean '" + service.getFullName() + "' as a JSON-RPC service: Type wasn't annotated as a service.");
			return;
		}
		if (rpcService.getServiceName() == null) {
			LOG.error("Cannor register bean '" + service.getFullName() + "' as a JSON-RPC service: Service name not set.");
			return;
		}
		this.registerService(rpcService.getServiceName(), service.getType());;
	}
}