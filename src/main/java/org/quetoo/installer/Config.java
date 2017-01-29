package org.quetoo.installer;

import java.io.File;
import java.security.CodeSource;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * The configuration container.
 * 
 * @author jdolan
 */
public class Config {

	public static final String NAME = "Quetoo Installer";
	public static final String VERSION = "1.0 BETA";

	public static final String ARCH = "quetoo.update.arch";
	public static final String HOST = "quetoo.update.host";
	public static final String DIR = "quetoo.update.dir";
	public static final String PRUNE = "quetoo.update.prune";
	public static final String CONSOLE = "quetoo.update.console";

	private static final Config defaults = new Config();

	private final CodeSource codeSource;
	private final File jar;
	private final CloseableHttpClient httpClient;
	private final Arch arch;
	private final Host host;
	private final File dir;
	private final Boolean prune;
	private final Boolean console;

	/**
	 * Default constructor.
	 */
	public Config() {
		this(new Properties(System.getProperties()));
	}

	/**
	 * Instantiates a Config with the specified Properties.
	 * 
	 * @param properties The Properties to initialize with.
	 */
	public Config(final Properties properties) {

		httpClient = HttpClients.createDefault();

		codeSource = getClass().getProtectionDomain().getCodeSource();
		jar = FileUtils.toFile(codeSource.getLocation());

		arch = Arch.getArch(properties.getProperty(ARCH, SystemUtils.OS_ARCH));
		host = Host.getHost(properties.getProperty(HOST, SystemUtils.OS_NAME));

		if (properties.getProperty(DIR) != null) {
			dir = new File(properties.getProperty(DIR));
		} else {
			dir = resolveDir();
		}

		prune = Boolean.parseBoolean(properties.getProperty(PRUNE, "false"));
		console = Boolean.parseBoolean(properties.getProperty(CONSOLE, "false"));
	}
	
	/**
	 * @return The most appropriate default destination directory.
	 */
	private File resolveDir() {

		if (jar != null) {
			switch (host) {
			case apple_darwin:
				final String path = jar.getAbsolutePath();
				
				if (path.contains("Quetoo.app")) {
					return new File(path.replaceFirst("Quetoo\\.app.*", ""));
				}
				
				break;
			default:
				final File parent = jar.getParentFile();
				
				if (parent.getName().equalsIgnoreCase("lib")) {
					return parent.getParentFile();
				}
			}
		}

		return new File(SystemUtils.USER_DIR);
	}
	
	/**
	 * @return True if the executable jar resides within the destination directory.
	 */
	public Boolean shouldRelaunch() {
		File file = getJar(), lib = getLib();
		while (file != null) {
			if (file.equals(lib)) {
				return true;
			}
			file = file.getParentFile();
		}
		return false;
	}

	public CodeSource getCodeSource() {
		return codeSource;
	}

	public File getJar() {
		return jar;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public Arch getArch() {
		return arch;
	}

	public Host getHost() {
		return host;
	}

	public String getArchHostPrefix() {
		return arch.toString() + "-" + host.toString().replace('_', '-');
	}

	public File getDir() {
		return dir;
	}

	public File getBin() {
		switch (getHost()) {
			case apple_darwin:
				return new File(getDir(), "Quetoo.app/Contents/MacOS");
			default:
				return new File(getDir(), "bin");
		}
	}

	public File getEtc() {
		switch (getHost()) {
			case apple_darwin:
				return new File(getDir(), "Quetoo.app/Contents/MacOS/etc");
			default:
				return new File(getDir(), "etc");
		}
	}

	public File getLib() {
		switch (getHost()) {
			case apple_darwin:
				return new File(getDir(), "Quetoo.app/Contents/MacOS/lib");
			default:
				return new File(getDir(), "lib");
		}
	}

	public File getShare() {
		switch (getHost()) {
			case apple_darwin:
				return new File(getDir(), "Quetoo.app/Contents/Resources");
			default:
				return new File(getDir(), "share");
		}
	}

	public Boolean getPrune() {
		return prune;
	}

	public Boolean getConsole() {
		return console;
	}

	public static Config getDefaults() {
		return defaults;
	}
}
