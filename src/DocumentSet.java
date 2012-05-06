import java.util.ArrayList;


/*
 * A set of documents.
 */
public class DocumentSet {
	
	private int qid;
	private ArrayList<Document> docs;
	
	public DocumentSet(ArrayList<Document> docs) {
		this.setDocs(docs);
	}
	
	public DocumentSet(int qid, ArrayList<Document> docs) {
		this.setQid(qid);
		this.setDocs(docs);
	}

	public void setQid(int qid) {
		this.qid = qid;
	}

	public int getQid() {
		return qid;
	}

	public void setDocs(ArrayList<Document> docs) {
		this.docs = docs;
	}

	public ArrayList<Document> getDocs() {
		return docs;
	}
	
}
