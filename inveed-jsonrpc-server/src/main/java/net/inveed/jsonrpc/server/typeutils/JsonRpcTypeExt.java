package net.inveed.jsonrpc.server.typeutils;

import java.util.HashMap;
import java.util.List;

import net.inveed.jsonrpc.core.annotation.JsonRpcService;
import net.inveed.typeutils.BeanTypeDesc;
import net.inveed.typeutils.MethodMetadata;
import net.inveed.typeutils.ext.IBeanTypeExtension;

public class JsonRpcTypeExt<T> implements IBeanTypeExtension<T> {
	private static final HashMap<String, BeanTypeDesc<?>> services = new HashMap<>();
		
	private final BeanTypeDesc<T> type;
	private String serviceName;
	public JsonRpcTypeExt(BeanTypeDesc<T> beanType) {
		this.type = beanType;
		
		JsonRpcService svcAnnotation = beanType.getAnnotation(JsonRpcService.class);
		if (svcAnnotation != null) {
			this.serviceName = svcAnnotation.value().trim().toLowerCase();
			if (this.serviceName.length() == 0) {
				this.serviceName = null;
				return;
			}
			services.put(this.serviceName, beanType);
		}
	}
	
	public static final BeanTypeDesc<?> getService(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		name = name.trim().toLowerCase();
		if (name.length() == 0) {
			return null;
		}
		return services.get(name);
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	public String getServiceName() {
		return this.serviceName;
	}
	
	@Override
	public BeanTypeDesc<T> getBeanType() {
		return this.type;
	}

	@Override
	public void initialize() {
		for (List<MethodMetadata> mml : this.getBeanType().getDeclaredMethods().values()) {
			for (MethodMetadata mm : mml) {
				JsonRpcMethodExt me = new JsonRpcMethodExt(mm, this);
				if (me.isValid()) {
					mm.registerExtension(me);
					me.initialize();
				}
			}
		}
	}

}
