import java.io.PrintWriter;
import java.util.ArrayList;


public class AnswerPrinter {
	
	private int num = 5;
	private PrintWriter pw;
	
	public AnswerPrinter(PrintWriter pw) {
		this.pw = pw;
	}
	
	public AnswerPrinter(int n, PrintWriter pw) {
		this.num = n;
		this.pw = pw;
	}
	
	public void printAnswers(Question q, ArrayList<Answer> as) {
		int n = 0;
		for (Answer a : as) {
			pw.println(q.getQid()+" "+a.docid+" "+a.answer);
			n++;
			if (n >= 5) break;
		}
	}
	
}
