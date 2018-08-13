package net.inveed.jsonrpc.server.typeutils;

import java.util.HashMap;

import net.inveed.jsonrpc.core.annotation.JsonRpcMethod;
import net.inveed.typeutils.MethodMetadata;
import net.inveed.typeutils.ParameterMetadata;
import net.inveed.typeutils.ext.IMethodExtension;


public class JsonRpcMethodExt implements IMethodExtension {
	private final MethodMetadata method;
	private final JsonRpcTypeExt<?> typeExt;
	private final String name;
	
	private HashMap<String, ParameterMetadata> namedParams = new HashMap<>();
	
	public JsonRpcMethodExt(MethodMetadata mm, JsonRpcTypeExt<?> jsonRpcTypeExt) {
		this.method = mm;
		this.typeExt = jsonRpcTypeExt;
		
		JsonRpcMethod ma = mm.getAnnotation(JsonRpcMethod.class);
		if (ma == null) {
			this.name = null;
		} else {
			if (ma.value() == null || "".equals(ma.value())) {
				this.name = mm.getName();
			} else {
				this.name = ma.value();
			}
		}
	}
	
	public void initialize() {
		for (ParameterMetadata p : this.method.getParams()) {
			JsonRpcMethodParamExt pe = new JsonRpcMethodParamExt(p, this);
			p.registerExtension(pe);
			
			if (pe.getName() != null) {
				this.namedParams.put(pe.getName().trim(), p);
			}
		}
	}
	
	public ParameterMetadata getNamedParam(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		name = name.trim();
		return this.namedParams.get(name);
	}
	
	public MethodMetadata getMethod() {
		return this.method;
	}
	
	public JsonRpcTypeExt<?> getTypeExt() {
		return this.typeExt;
	}
	
	public boolean isValid() {
		return this.name != null;
	}

}
