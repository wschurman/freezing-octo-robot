
public class Question {
	private int qid;
	private String question;
	
	public question_type qtype;
	public answer_type atype;
	
	public enum question_type {
		STANDARD,
	}
	
	public enum answer_type {
		STANDARD,
	}
	
	public Question(int id, String q) {
		this.setQid(id);
		this.setQuestion(q);
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
