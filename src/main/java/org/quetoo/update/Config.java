package org.quetoo.update;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * The configuration container.
 * 
 * @author jdolan
 */
public class Config {
	
	public static final String ARCH = "quetoo.update.arch";
	public static final String HOST = "quetoo.update.host";
	public static final String DIR = "quetoo.update.dir";
	public static final String PRUNE = "quetoo.update.prune";
	
	private static final Config defaults = new Config();

	private final AmazonS3Client amazonS3Client;
	private final CloseableHttpClient httpClient;
	private final Arch arch;
	private final Host host;
	private final File dir;
	private final Boolean prune;

	/**
	 * Default constructor.
	 */
	public Config() {
		this(new Properties(System.getProperties()));
	}
	
	public Config(final Properties properties) {
		
		amazonS3Client = new AmazonS3Client();

		httpClient = HttpClients.createDefault();

		arch = Arch.getArch(properties.getProperty(ARCH, SystemUtils.OS_ARCH));
		host = Host.getHost(properties.getProperty(HOST, SystemUtils.OS_NAME));
		dir = new File(properties.getProperty(DIR, getDefaultDir()));
		prune = Boolean.parseBoolean(properties.getProperty(PRUNE, "false"));
	}

	public AmazonS3Client getAmazonS3Client() {
		return amazonS3Client;
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
	
	public static String getDefaultDir() {
		return FileUtils.toFile(ClassLoader.getSystemResource(".")).getAbsolutePath();
	}
	
	public static Config getDefaults() {
		return defaults;
	}
}
