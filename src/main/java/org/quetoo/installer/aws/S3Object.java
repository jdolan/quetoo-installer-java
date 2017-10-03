package org.quetoo.installer.aws;

import static org.quetoo.installer.aws.S3.getLong;
import static org.quetoo.installer.aws.S3.getString;

import org.quetoo.installer.Asset;
import org.quetoo.installer.Sync;
import org.w3c.dom.Node;

/**
 * An abstraction for an object residing in an {@link S3Bucket}.
 * 
 * @author jdolan
 */
public class S3Object implements Asset {

	private static final String KEY = "Key";
	private static final String ETAG = "ETag";
	private static final String SIZE = "Size";

	private final S3BucketSync s3BucketSync;
	private final String key;
	private final String etag;
	private final long size;

	/**
	 * Instantiates an {@link S3Object} from the given XML node.
	 * 
	 * @param s3BucketSync The {@link S3BucketSync}.
	 * @param node A `Contents` node of an S3 bucket listing.
	 */
	public S3Object(final S3BucketSync s3BucketSync, final Node node) {
		this.s3BucketSync = s3BucketSync;

		key = getString(node, KEY);
		etag = getString(node, ETAG).replaceAll("\"", "");
		size = getLong(node, SIZE);
	}

	@Override
	public Sync source() {
		return getS3BucketSync();
	}

	@Override
	public String name() {
		return getKey();
	}

	@Override
	public long size() {
		return getSize();
	}

	@Override
	public boolean isDirectory() {
		return key.endsWith("/");
	}

	@Override
	public String toString() {
		return key;
	}

	public S3BucketSync getS3BucketSync() {
		return s3BucketSync;
	}

	public String getKey() {
		return key;
	}

	public String getEtag() {
		return etag;
	}

	public long getSize() {
		return size;
	}
}
