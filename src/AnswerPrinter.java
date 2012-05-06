import java.io.PrintWriter;
import java.util.ArrayList;

/*
 * Class for printing answers to a file in the uploadable format.
 */
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
			pw.println(q.getQid()+" "+a.docid+" "+a.getAbridgedVersion());
			n++;
			if (n >= num) break;
		}
	}
	
}
