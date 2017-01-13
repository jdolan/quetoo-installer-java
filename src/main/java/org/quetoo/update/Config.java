package org.quetoo.update;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * The configuration container.
 * 
 * @author jdolan
 */
public class Config {

	private final AmazonS3Client amazonS3Client;
	private final CloseableHttpClient httpClient;
	private final Arch arch;
	private final Host host;
	private final File dir;

	/**
	 * Default constructor.
	 */
	public Config() {

		amazonS3Client = new AmazonS3Client();
		amazonS3Client.setRegion(Region.getRegion(Regions.US_EAST_1));

		httpClient = HttpClients.createDefault();

		arch = Arch.getArch(System.getProperty("QUETOO_UPDATE_ARCH", SystemUtils.OS_ARCH));
		assert arch != Arch.unknown;

		host = Host.getHost(System.getProperty("QUETOO_UPDATE_HOST", SystemUtils.OS_NAME));
		assert host != Host.unknown;
		
		String dir = System.getProperty("QUETOO_UPDATE_DIR");
		if (dir != null) {
			this.dir = new File(dir);
		} else {
			this.dir = null;
		}
		assert this.dir != null;
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
	
	public File getDir(final String path) {
		return new File(getDir(), path);
	}
}
