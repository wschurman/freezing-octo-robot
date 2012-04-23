import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Runner {
	
	public static void main(String[] args) throws IOException {
		
		Parser p = new Parser();
		p.parseAll();
		
		for (int i : p.questions.keySet()) {
			Question q = p.questions.get(i);
			
			(new BaselineQuestionClassifier()).classifyQuestion(q); // writes to q
			DocumentSet d = (new BaselineDocumentRetriever(p.raw_documents)).getDocuments(q);
			ArrayList<Answer> as = (new BaselineFilter()).filter(q, d);
			ArrayList<Answer> finals = (new BaselineAnswerExtractor()).extractAnswers(q, as);
			
			System.out.println("Question: "+q.getQuestion()+"\nAnswer: "+finals.get(0).answer);
			break;
		}
	}

}
