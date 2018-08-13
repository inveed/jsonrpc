package net.inveed.jsonrpc.server;

import java.lang.reflect.InvocationTargetException;

public class ThrowablesUtil {
	public static Throwable getRootCause(Throwable t) {
		while (t instanceof InvocationTargetException) {
			t = t.getCause();
		}
		return t;
	}
}
