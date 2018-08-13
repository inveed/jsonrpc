package net.inveed.jsonrpc.server;

public class StringsUtil {

	public static boolean isNullOrEmpty(String s) {
		if (s == null) {
			return true;
		}
		s = s.trim();
		if (s.length() == 0) {
			return true;
		}
		return false;
	}
}
