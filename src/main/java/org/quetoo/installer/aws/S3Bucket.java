package org.quetoo.installer.aws;

import static org.quetoo.installer.aws.S3.getChildNodes;
import static org.quetoo.installer.aws.S3.getString;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

/**
 * An abstraction for the parsed XML contents of an AWS S3 bucket.
 * 
 * @author jdolan
 */
public class S3Bucket implements Iterable<S3Object> {

	private static final String NAME = "Name";
	private static final String CONTENTS = "Contents";

	private final S3BucketSync s3BucketSync;
	private final String name;
	private final List<S3Object> objects;

	/**
	 * Instantiates a new {@link S3Bucket} from the given XML document.
	 * 
	 * @param s3BucketSync The {@link S3BucketSync}.
	 * @param doc A parsed S3 bucket listing (e.g. `http://quetoo.s3.amazonaws.com/`).
	 */
	public S3Bucket(final S3BucketSync s3BucketSync, final Document doc) {
		this.s3BucketSync = s3BucketSync;

		name = getString(doc.getDocumentElement(), NAME);

		objects = getChildNodes(doc.getDocumentElement(), CONTENTS)
				.map(node -> new S3Object(s3BucketSync, node))
				.collect(Collectors.toList());
	}

	@Override
	public Iterator<S3Object> iterator() {
		return objects.iterator();
	}

	public S3BucketSync getS3BucketSync() {
		return s3BucketSync;
	}

	public String getName() {
		return name;
	}

	public List<S3Object> getObjects() {
		return objects;
	}
}
