package net.inveed.jsonrpc.server.typeutils;

import net.inveed.jsonrpc.core.annotation.JsonRpcParam;
import net.inveed.commons.reflection.ParameterMetadata;
import net.inveed.commons.reflection.ext.IParameterExtension;


public class JsonRpcMethodParamExt implements IParameterExtension {

	private ParameterMetadata param;
	private JsonRpcMethodExt method;
	private String name;
	private boolean required;
	
	public JsonRpcMethodParamExt(ParameterMetadata p, JsonRpcMethodExt jsonRpcMethodExt) {
		this.param = p;
		this.method = jsonRpcMethodExt;
		JsonRpcParam pa = this.param.getAnnotation(JsonRpcParam.class);
		
		if (pa != null) {
			this.required = pa.required();
			this.name = pa.value();
		} else {
			this.name = p.getName();
			this.required = p.getType().isPrimitive();
		}
	}

	public boolean isRequired() {
		return this.required;
	}
	
	public String getName() {
		return this.name;
	}
	
	public JsonRpcMethodExt getMethodExt() {
		return this.method;
	}
}
