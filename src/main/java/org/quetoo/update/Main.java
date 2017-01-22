package org.quetoo.update;

import static org.quetoo.update.Config.getDefaults;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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

		final Option arch = Option.builder("a")
				.longOpt("arch")
				.hasArg()
				.argName(getDefaults().getArch().toString())
				.desc("the architecture name")
				.build();

		final Option host = Option.builder("h")
				.longOpt("host")
				.hasArg()
				.argName(getDefaults().getHost().toString())
				.desc("the host name")
				.build();

		final Option dir = Option.builder("d")
				.longOpt("dir")
				.hasArg()
				.argName(getDefaults().getDir().toString())
				.desc("the target directory")
				.build();

		final Option prune = Option.builder("p")
				.longOpt("prune")
				.desc("prune unknown files")
				.build();

		final Option nogui = Option.builder("c")
				.longOpt("console")
				.desc("do not create the user interface")
				.build();

		final Options options = new Options();

		options.addOption(arch);
		options.addOption(host);
		options.addOption(dir);
		options.addOption(prune);
		options.addOption(nogui);

		final Properties properties = new Properties();

		try {
			final CommandLine commandLine = new DefaultParser().parse(options, args);
			commandLine.iterator().forEachRemaining(opt -> {
				if (commandLine.hasOption(opt.getOpt())) {
					final String value, key = "quetoo.update." + opt.getLongOpt();
					if (opt.hasArg()) {
						value = commandLine.getOptionValue(opt.getOpt());
					} else {
						value = "true";
					}
					properties.setProperty(key, value);
				}
			});
		} catch (ParseException pe) {
			new HelpFormatter().printHelp("quetoo-update", options);
			System.exit(1);
		}

		final Config config = new Config(properties);

		if (config.getConsole()) {
			new Console(new Manager(config));
		} else {
			new Frame(new Manager(config));
		}
	}
}
