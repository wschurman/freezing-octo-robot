import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Runner {
	
	public static void main(String[] args) throws IOException {
//		System.out.println("balls");
		Parser p = new Parser();
		p.parseAll();
		System.out.println("Am i going in there...? " + p.questions.size());
		int k = 0;
		for (int i : p.questions.keySet()) {
			Question q = p.questions.get(i);
			
			(new BaselineQuestionClassifier()).classifyQuestion(q); // writes to q
			DocumentSet d = (new BaselineDocumentRetriever(p.raw_documents, p.raw_word_counts)).getDocuments(q);
			System.out.println("I am about to filter a bitch");
			ArrayList<Answer> as = (new BaselineFilter()).filter(q, d);
			System.out.println("Shit bro..you filter that?");
			ArrayList<Answer> finals = (new BaselineAnswerExtractor()).extractAnswers(q, as);
			
			System.out.println("Question: "+q.getQuestion()+"\nAnswer: "+finals.get(0).answer);
			
			k++;
			
			if (k > 10) break;
		}
	}
}
