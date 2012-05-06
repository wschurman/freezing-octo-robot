import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class Runner {
	
	private static final String fname = "guesses.txt";
	
	public static void main(String[] args) throws IOException {
		Parser p = new Parser();
		p.parseAll();
		
		PrintWriter pw = new PrintWriter(new File(fname));
		AnswerPrinter ap = new AnswerPrinter(5, pw);
		
		int k = 0;
		ProgressTracker pt = new ProgressTracker(p.questions.size());
		Thread t = new Thread(pt);
		t.start();
		MLQuestionClassifier classify = new MLQuestionClassifier("train_5500.label.txt");
		WikiFilter wikiFilter = new WikiFilter();
		BaselineFilter baselineFilter = new BaselineFilter();
		for (int i : p.questions.keySet()) {
			Question q = p.questions.get(i);
			
			System.out.println("******CLASSIFYING QUESTION********");
			classify.classifyQuestion(q); // writes to q
			System.out.println("******RETRIEVING DOCUMENTS********");
			DocumentSet d = (new BaselineDocumentRetriever(p.raw_documents, p.raw_word_counts, p.raw_stem_counts)).getDocuments(q);
			System.out.println("******WIKI FILTER********");
			ArrayList<Answer> as = wikiFilter.filter(q, d);
			System.out.println("******NER FILTER********");
			ArrayList<Answer> nerFilter = baselineFilter.filter(as, q);
			System.out.println("******EXTRACTING ANSWERS********");
			ArrayList<Answer> finals = (new BaselineAnswerExtractor()).extractAnswers(q, nerFilter);
			
			if (finals.size() < 1) continue;
			System.out.println("Question: "+q.getQuestion()+"\nAnswer: "+finals.get(0).answer);
			
			k++;
			pt.updateCompletion(k);
			ap.printAnswers(q, finals);
			if (k > 2) break;
		}
		
		pw.flush();
		pw.close();
	}
}
