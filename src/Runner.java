import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jdom2.JDOMException;


public class Runner {
	
	private HashMap<Integer, Question> questions;
	
	public static void main(String[] args) throws IOException, JDOMException {
		
		Parser p = new Parser();
		p.parseAll();
		
		for (int i : p.questions.keySet()) {
			Question q = p.questions.get(i);
			
			// (new AbstractQuestionClassifier()).classifyQuestion(q); // writes to q
			// DocumentSet d = (new AbstractDocumentRetriever()).getDocuments(q);
			// ArrayList<Answer> as = (new AbstractFilter()).filter(q, d);
			// ArrayList<Answer> finals = (new AbstractAnswerExtractor()).extractAnswers(q, as);
		}
	}

}
