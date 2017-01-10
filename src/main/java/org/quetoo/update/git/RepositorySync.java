package org.quetoo.update.git;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.quetoo.update.Sync;

/**
 * 
 * @author jdolan
 *
 */
public class RepositorySync implements Sync {
	
	/**
	 * A builder for creating {@link RepositorySync} instances.
	 */
	public static class Builder {
		
		private String repositoryUri;
		private File destination;
		private Listener listener;
		
		public Builder withRepositoryUri(final String repositoryUri) {
			this.repositoryUri = repositoryUri;
			return this;
		}
		
		public Builder withDestination(final File destination) {
			this.destination = destination;
			return this;
		}

		public Builder withDestination(final String destination) {
			this.destination = new File(destination);
			return this;
		}

		public Builder withListener(final Listener listener) {
			this.listener = listener;
			return this;
		}

		public RepositorySync build() {
			return new RepositorySync(this);
		}
	}
	
	private final String repositoryUri;
	private final File destination;
	private final Listener listener;
	
	/**
	 * Instantiates a {@link RepositorySync} with the given Builder.
	 * 
	 * @param builder The Builder.
	 */
	public RepositorySync(final Builder builder) {
		
		this.repositoryUri = builder.repositoryUri;
		this.destination = builder.destination;
		this.listener = builder.listener;
	}
	
	private Git cloneOrOpen() throws IOException {
		
		File gitdir = new File(destination, ".git");
		
		if (gitdir.isDirectory()) {
			return Git.open(destination);
		}
		
		CloneCommand command = new CloneCommand()
				.setURI(repositoryUri)
				.setDirectory(destination);
		
		try {
			return command.call();
		} catch (Throwable t) {
			throw new IOException(t);
		}
	}
	
	@Override
	public Set<File> sync() throws IOException {
		
		FileUtils.forceMkdir(destination);
		
		Git git = cloneOrOpen();
		
		git.close();
		
		return new HashSet<>();
	}
}
