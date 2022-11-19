package org.quetoo.installer;

import static org.quetoo.installer.Config.getDefaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * Quetoo Update entry point.
 * 
 * @author jdolan
 */
public class Main {
	
	/**
	 * Program entry point.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String args[]) {

		final Option build = Option.builder("b")
				.longOpt("build")
				.hasArg()
				.argName(getDefaults().getBuild().toString())
				.desc("the build name (architecture and host)")
				.build();

		final Option dir = Option.builder("d")
				.longOpt("dir")
				.hasArg()
				.argName(getDefaults().getDir().toString())
				.desc("the target directory")
				.build();

		final Option prune = Option.builder("p")
				.longOpt("prune")
				.hasArg()
				.optionalArg(true)
				.desc("prune unknown files")
				.build();

		final Option console = Option.builder("c")
				.longOpt("console")
				.hasArg()
				.optionalArg(true)
				.desc("do not create the user interface")
				.build();

		final Options options = new Options();

		options.addOption(build);
		options.addOption(dir);
		options.addOption(prune);
		options.addOption(console);

		final Properties properties = new Properties();

		try {
			final CommandLine commandLine = new DefaultParser().parse(options, args);
			commandLine.iterator().forEachRemaining(opt -> {
				if (commandLine.hasOption(opt.getOpt())) {
					String key = "quetoo.installer." + opt.getLongOpt();
					String value = commandLine.getOptionValue(opt.getOpt());
					if (value == null) {
						value = "true";
					}
					properties.setProperty(key, value);
				}
			});
		} catch (ParseException pe) {
			new HelpFormatter().printHelp("quetoo-installer", options);
			System.exit(1);
		}

		final Config config = new Config(properties);
		
		if (config.shouldRelaunch()) {			
			try {
				final File tempFile = Files.createTempFile("quetoo-installer",".jar").toFile();
				FileUtils.copyFile(config.getJar(), tempFile);

				new ProcessBuilder().inheritIO().command(new String[] {
						SystemUtils.JAVA_HOME + "/bin/java",
						"-jar",
						tempFile.getAbsolutePath(),
						"--build",
						config.getBuild().toString(),
						"--dir",
						config.getDir().getAbsolutePath(),
						"--prune",
						config.getPrune().toString(),
						"--console",
						config.getConsole().toString()
				}).start();
			} catch (IOException ioe) {
				ioe.printStackTrace(System.err);
				System.exit(2);
			}

			System.exit(0);
		}
		
		if (config.getConsole()) {
			new Console(new Manager(config));
		} else {
			new Frame(new Manager(config));
		}
	}
}
