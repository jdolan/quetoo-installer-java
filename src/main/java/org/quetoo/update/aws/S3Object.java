package org.quetoo.update.aws;

import static org.quetoo.update.aws.S3.getInstant;
import static org.quetoo.update.aws.S3.getString;

import java.time.Instant;

import org.w3c.dom.Node;

/**
 * An abstraction for an object residing in an {@link S3Bucket}.
 * 
 * @author jdolan
 */
public class S3Object {

	private static final String KEY = "Key";
	private static final String LAST_MODIFIED = "LastModified";

	private final String key;
	private final Instant lastModified;

	/**
	 * Instantiates an {@link S3Object} from the given XML node.
	 * 
	 * @param node A `Contents` node of an S3 bucket listing.
	 */
	public S3Object(final Node node) {
		key = getString(node, KEY);
		lastModified = getInstant(node, LAST_MODIFIED);
	}
	
	public String getKey() {
		return key;
	}

	public Instant getLastModified() {
		return lastModified;
	}
	
	public long getLastModifiedTime() {
		return lastModified.toEpochMilli();
	}	
}
