import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.util.CoreMap;

/*
 * Class to hold a single instance of the document.
 * Represented by a hash map of tag to inner value.
 */
public class Document {
	
	public HashMap<String, String> content;
	private List<CoreMap> sentences;
	
	public Document(HashMap<String, String> con) {
		this.content = con;
	}
	
	public String getText() {
		return content.get("text");
	}
	
	public String getID() {
		return content.get("docno");
	}

	public void setSentences(List<CoreMap> sentences) {
		this.sentences = sentences;
	}

	public List<CoreMap> getSentences() {
		return sentences;
	}
}
