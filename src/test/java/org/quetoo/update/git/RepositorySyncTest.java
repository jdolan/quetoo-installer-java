package org.quetoo.update.git;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.quetoo.update.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for the {@link RepositorySync} class.
 * 
 * @author jdolan
 */
public class RepositorySyncTest implements Sync.Listener {

	private RepositorySync repositorySync;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void before() {

		final String tmp = FileUtils.getTempDirectoryPath();

		final File destination = new File(FilenameUtils.concat(tmp, "quetoo-update"));
		log.info("Syncing to desination {}", destination);

		repositorySync = new RepositorySync.Builder()
				.withRepositoryUri("https://github.com/jdolan/quetoo-update.git")
				.withDestination(destination)
				.withListener(this)
				.build();

		FileUtils.deleteQuietly(destination);
	}

	@Override
	public void onCountObjects(int count) {
		log.debug("onCountObjects {}", count);
	}

	@Override
	public void onSyncObject(String name) {
		log.debug("onSyncObject {}", name);
	}

	@Override
	public void onRemoveFile(File file) {
		log.debug("onRemoveFile {}", file);
	}

	@Test
	public void sync() throws IOException {

		Set<File> files = repositorySync.sync();

		assertNotNull(files);
	}
}
