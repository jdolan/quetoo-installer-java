package org.quetoo.installer;

import org.apache.commons.lang3.SystemUtils;

/**
 * Available Quetoo builds to install.
 * 
 * @author jdolan
 */
public enum Build {
	
	x86_64_apple_darwin,

	i686_pc_linux,
	x86_64_pc_linux,
	
	i686_pc_windows,
	x86_64_pc_windows,
	
	i686_w64_mingw32,
	x86_64_w64_mingw32;
	
	@Override
	public String toString() {
		switch (this) {
			case x86_64_apple_darwin:
				return "x86_64-apple-darwin";
		
			case i686_pc_linux:
				return "i686-pc-linux";
			case x86_64_pc_linux:
				return "x86_64-pc-linux";
				
			case i686_pc_windows:
				return "i686-pc-windows";
			case x86_64_pc_windows:
				return "x86_64-pc-windows";
				
			case i686_w64_mingw32:
				return "i686-w64-mingw32";
			case x86_64_w64_mingw32:
				return "x86_64-w64-mingw32";
				
			default:
				throw new RuntimeException("Unsupported value: " + this);
		}
	}
	
	public static Build getBuild(final String string) {
		
		switch (string) {
			case "x86_64-apple-darwin":
				return x86_64_apple_darwin;

			case "i686-pc-linux":
				return i686_pc_linux;
			case "x86_64-pc-linux":
				return x86_64_pc_linux;

			case "i686-pc-windows":
				return i686_pc_windows;
			case "x86_64-pc-windows":
				return x86_64_pc_windows;

			case "i686-w64-mingw32":
				return i686_w64_mingw32;
			case "x86_64-w64-mingw32":
				return x86_64_w64_mingw32;

			default:
				throw new RuntimeException("Unsupported value: " + string);
		}
	}
	
	public static Build getHostBuild() {
		
		if (SystemUtils.IS_OS_MAC) {
			return x86_64_apple_darwin;
			
		} else if (SystemUtils.IS_OS_LINUX) {
			switch (SystemUtils.OS_ARCH) {
				case "amd64":
				case "x86_64":
					return x86_64_pc_linux;
				default:
					return i686_pc_linux;
			}
		}
		
		else if (SystemUtils.IS_OS_WINDOWS_10) {
			switch (SystemUtils.OS_ARCH) {
				case "amd64":
				case "x86_64":
					return x86_64_pc_windows;
				default:
					return i686_pc_linux;
			}
		}
		
		else if (SystemUtils.IS_OS_WINDOWS) {
			switch (SystemUtils.OS_ARCH) {
				case "amd64":
				case "x86_64":
					return x86_64_w64_mingw32;
				default:
					return i686_w64_mingw32;
			}
		}
		
		throw new RuntimeException("Failed to detect host build: " + SystemUtils.OS_ARCH + "-" + SystemUtils.OS_NAME);
	}
}
