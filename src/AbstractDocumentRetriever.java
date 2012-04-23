import java.util.HashMap;


public abstract class AbstractDocumentRetriever {
	
	// extend this class to make a document retrieval method
	protected HashMap<String, Document> ds;
	
	public AbstractDocumentRetriever(HashMap<String, Document> ds) {
		this.ds = ds;
	}
	
	public abstract DocumentSet getDocuments(Question q);
	
}
