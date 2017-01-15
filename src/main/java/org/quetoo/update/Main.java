package org.quetoo.update;

import static org.quetoo.update.Config.getDefaults;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quetoo Update entry point.
 * 
 * @author jdolan
 */
public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

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
				.hasArg()
				.argName(getDefaults().getPrune().toString())
				.desc("prune unknown files")
				.build();

		final Options options = new Options();

		options.addOption(arch);
		options.addOption(host);
		options.addOption(dir);
		options.addOption(prune);

		final CommandLineParser parser = new DefaultParser();

		try {
			parser.parse(options, args);
		} catch (ParseException pe) {
			new HelpFormatter().printHelp("quetoo-update", options);
			System.exit(1);
		}
		
		Properties properties = new Properties();
		
		if (arch.getValue() != null) {
			properties.setProperty(Config.ARCH, arch.getValue());
		}
		
		if (host.getValue() != null) {
			properties.setProperty(Config.HOST, host.getValue());
		}
		
		if (dir.getValue() != null) {
			properties.setProperty(Config.DIR, dir.getValue());
		}
		
		if (prune.getValue() != null) {
			properties.setProperty(Config.PRUNE, prune.getValue());
		}
		
		final Config config = new Config(properties);
		
		try {
			new Manager(config).sync();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}
}
