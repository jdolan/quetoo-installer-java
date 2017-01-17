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
		assertEquals(Host.pc_windows, getHost("Windows XP"));
		assertEquals(Host.pc_windows, getHost("Windows 7"));
		assertEquals(Host.pc_windows, getHost("Windows 8"));
		assertEquals(Host.pc_windows, getHost("Windows 10"));
		assertEquals(Host.w64_mingw32, getHost("mingw"));
	}
}
