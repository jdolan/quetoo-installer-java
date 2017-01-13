package org.quetoo.update;

import org.apache.commons.lang3.StringUtils;

/**
 * Platform host detection.
 * 
 * @author jdolan
 */
public enum Host {

	unknown, apple_darwin, pc_linux, w64_mingw32, msvc;

	@Override
	public String toString() {
		return super.toString().replace('_', '-');
	}

	/**
	 * @param string The host name.
	 * @return The most appropriate Host for `string`.
	 */
	public static Host getHost(final String string) {

		if (StringUtils.containsIgnoreCase(string, "mac os x")) {
			return apple_darwin;
		}

		if (StringUtils.containsIgnoreCase(string, "linux")) {
			return pc_linux;
		}

		if (StringUtils.containsIgnoreCase(string, "windows xp")) {
			return w64_mingw32;
		}

		if (StringUtils.containsIgnoreCase(string, "windows")) {
			return msvc;
		}

		return unknown;
	}	
}
