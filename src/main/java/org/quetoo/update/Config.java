package org.quetoo.update;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * The configuration container.
 * 
 * @author jdolan
 */
public class Config {
	
	public static final String NAME = "Quetoo Update";
	public static final String VERSION = "1.0-SNAPSHOT";
	
	public static final String ARCH = "quetoo.update.arch";
	public static final String HOST = "quetoo.update.host";
	public static final String DIR = "quetoo.update.dir";
	public static final String PRUNE = "quetoo.update.prune";
	public static final String GUI = "quetoo.update.gui";
	
	private static final Config defaults = new Config();

	private final CloseableHttpClient httpClient;
	private final Arch arch;
	private final Host host;
	private final File dir;
	private final Boolean prune;
	private final Boolean gui;

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
		
		arch = Arch.getArch(properties.getProperty(ARCH, SystemUtils.OS_ARCH));
		host = Host.getHost(properties.getProperty(HOST, SystemUtils.OS_NAME));
		dir = new File(properties.getProperty(DIR, SystemUtils.USER_DIR));
		prune = Boolean.parseBoolean(properties.getProperty(PRUNE, "false"));
		gui = Boolean.parseBoolean(properties.getProperty(GUI, "false"));
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
		return arch.toString() + "-" + host.toString();
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
	
	public Boolean getGui() {
		return gui;
	}
	
	public static Config getDefaults() {
		return defaults;
	}
}
