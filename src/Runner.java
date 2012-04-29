import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Runner {
	
	private static final String fname = "guesses.txt";
	
	public static void main(String[] args) throws IOException {
//		System.out.println("balls");
		Parser p = new Parser();
		p.parseAll();
		
		PrintWriter pw = new PrintWriter(new File(fname));
		AnswerPrinter ap = new AnswerPrinter(5, pw);
		
		int k = 0;
		for (int i : p.questions.keySet()) {
			Question q = p.questions.get(i);
			
			(new BaselineQuestionClassifier()).classifyQuestion(q); // writes to q
			DocumentSet d = (new BaselineDocumentRetriever(p.raw_documents, p.raw_word_counts)).getDocuments(q);
			ArrayList<Answer> as = (new BaselineFilter()).filter(q, d);
			ArrayList<Answer> finals = (new BaselineAnswerExtractor()).extractAnswers(q, as);
			
			if (finals.size() < 1) continue;
			System.out.println("Question: "+q.getQuestion()+"\nAnswer: "+finals.get(0).answer);
			
			k++;
			
			ap.printAnswers(q, finals);
			if (k > 15) break;
		}
		
		pw.flush();
		pw.close();
	}
}
