package org.quetoo.installer;

import java.io.File;
import java.security.CodeSource;
import java.util.Properties;
	
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
	public static final String VERSION = "1.0.0";

	public static final String BUILD = "quetoo.installer.build";
	public static final String DIR = "quetoo.installer.dir";
	public static final String PRUNE = "quetoo.installer.prune";
	public static final String CONSOLE = "quetoo.installer.console";

	private static final Config defaults = new Config();

	private final CodeSource codeSource;
	private final CloseableHttpClient httpClient;
	private final Build build;
	private final File jar;
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

		if (properties.contains(BUILD)) {
			build = Build.getBuild(properties.getProperty(BUILD));
		} else {
			build = Build.getHostBuild();
		}
		
		if (properties.contains(DIR)) {
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
		
		final File pwd = new File(SystemUtils.USER_DIR);

		for (File file : new File[] { jar, pwd }) {			
			do {
				switch (build) {
				case x86_64_apple_darwin:
					if (StringUtils.equalsIgnoreCase(file.getName(), "Quetoo.app")) {
						return file;
					}
					break;
				default:
					if (StringUtils.containsIgnoreCase(file.getName(), "Quetoo")) {
						return file;
					}
					break;
				}
				
				file = file.getParentFile();
			} while (file != null);
		}
		
		return pwd;
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

	public Build getBuild() {
		return build;
	}

	public File getDir() {
		return dir;
	}

	public File getBin() {
		switch (build) {
			case x86_64_apple_darwin:
				return new File(getDir(), "Contents/MacOS");
			default:
				return new File(getDir(), "bin");
		}
	}


	public File getLib() {
		switch (build) {
			case x86_64_apple_darwin:
				return new File(getDir(), "Contents/MacOS/lib");
			default:
				return new File(getDir(), "lib");
		}
	}

	public File getData() {
		switch (build) {
			case x86_64_apple_darwin:
				return new File(getDir(), "Contents/Resources");
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
