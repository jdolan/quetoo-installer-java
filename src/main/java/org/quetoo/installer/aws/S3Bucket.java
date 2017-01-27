package org.quetoo.installer.aws;

import static org.quetoo.installer.aws.S3.getChildNodes;
import static org.quetoo.installer.aws.S3.getString;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An abstraction for the parsed XML contents of an AWS S3 bucket.
 * 
 * @author jdolan
 */
public class S3Bucket implements Iterable<S3Object> {

	private static final String NAME = "Name";
	private static final String CONTENTS = "Contents";

	private final String name;
	private final List<S3Object> objects;

	/**
	 * Instantiates a new {@link S3Bucket} from the given XML document.
	 * 
	 * @param doc A parsed S3 bucket listing (e.g. `http://quetoo.s3.amazonaws.com/`).
	 */
	public S3Bucket(final Document doc) {
		name = getString(doc.getDocumentElement(), NAME);

		final Stream<Node> contents = getChildNodes(doc.getDocumentElement(), CONTENTS);
		objects = contents.map(S3Object::new).collect(Collectors.toList());
	}

	@Override
	public Iterator<S3Object> iterator() {
		return objects.iterator();
	}

	public String getName() {
		return name;
	}

	public List<S3Object> getObjects() {
		return objects;
	}
}
