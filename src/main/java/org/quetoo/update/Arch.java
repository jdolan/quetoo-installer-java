package org.quetoo.update;

import org.apache.commons.lang3.StringUtils;

/**
 * Platform architecture detection.
 * 
 * @author jdolan
 */
public enum Arch {

	unknown, i686, x86_64;

	/**
	 * @param string The architecture name.
	 * @return The most appropriate Arch for `string`.
	 */
	public static Arch getArch(final String string) {

		if (StringUtils.equalsAny(string, "x86", "i386", "i486", "i586", "i686")) {
			return i686;
		}

		if (StringUtils.equalsAny(string, "x86_64", "amd64")) {
			return x86_64;
		}

		return unknown;
	}
}
