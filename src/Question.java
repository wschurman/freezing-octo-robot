
public class Question {
	private int qid;
	private String question;
	
	
	public Question(int id, String q) {
		this.qid = id;
		this.question = q;
	}
	
	public void setQid(int qid) {
		this.qid = qid;
	}
	public int getQid() {
		return qid;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getQuestion() {
		return question;
	}
	
	
}
