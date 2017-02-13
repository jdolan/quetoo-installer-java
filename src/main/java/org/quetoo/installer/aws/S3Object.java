package org.quetoo.installer.aws;

import static org.quetoo.installer.aws.S3.getString;

import org.w3c.dom.Node;

/**
 * An abstraction for an object residing in an {@link S3Bucket}.
 * 
 * @author jdolan
 */
public class S3Object {

	private static final String KEY = "Key";
	private static final String ETAG = "ETag";

	private final String key;
	private final String etag;

	/**
	 * Instantiates an {@link S3Object} from the given XML node.
	 * 
	 * @param node A `Contents` node of an S3 bucket listing.
	 */
	public S3Object(final Node node) {
		key = getString(node, KEY);
		etag = getString(node, ETAG).replaceAll("\"", "");
	}

	public String getKey() {
		return key;
	}
	
	public String getEtag() {
		return etag;
	}
}
