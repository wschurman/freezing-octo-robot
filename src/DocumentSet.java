import java.util.ArrayList;



public class DocumentSet {
	
	private int qid;
	private ArrayList<Document> docs;
	
	public DocumentSet(int qid, ArrayList<Document> docs) {
		this.setQid(qid);
		this.docs = docs;
	}

	public void setQid(int qid) {
		this.qid = qid;
	}

	public int getQid() {
		return qid;
	}
	
}
