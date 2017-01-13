package org.quetoo.update;

import static org.junit.Assert.assertEquals;
import static org.quetoo.update.Host.getHost;

import org.junit.Test;

/**
 * Unit tests for the {@link Host} class.
 * 
 * @author jdolan
 */
public class HostTest {

	@Test
	public void host() {
		assertEquals(Host.apple_darwin, getHost("Mac OS X"));
		assertEquals(Host.pc_linux, getHost("Linux"));
		assertEquals(Host.w64_mingw32, getHost("Windows XP"));
		assertEquals(Host.msvc, getHost("Windows 7"));
		assertEquals(Host.msvc, getHost("Windows 8"));
		assertEquals(Host.msvc, getHost("Windows 10"));
	}
}
