package org.quetoo.installer;

import static org.junit.Assert.assertEquals;
import static org.quetoo.installer.Arch.getArch;

import org.junit.Test;

/**
 * Unit tests for the {@link Arch} class.
 * 
 * @author jdolan
 */
public class ArchTest {

	@Test
	public void arch() {
		assertEquals(Arch.i686, getArch("x86"));
		assertEquals(Arch.i686, getArch("i386"));
		assertEquals(Arch.i686, getArch("i686"));
		assertEquals(Arch.x86_64, getArch("x86_64"));
		assertEquals(Arch.x86_64, getArch("amd64"));
	}
}
