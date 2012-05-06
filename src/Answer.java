
public class Answer {
	public String answer;
	public String docid;
	
	public Answer(String s, String d) {
		answer = s;
		docid = d;
	}
	
	/*
	 * Trim answer to 10 words (close to 50 bytes)
	 */
	public String getAbridgedVersion() {
		String[] sp = answer.split(" ");
		
		String ret = "";
		
		int k = 0;
		for (String s : sp) {
			
			ret += s + " ";
			
			k++;
			if (k >= 10) break;
		}
		
		return ret;
	}
}
