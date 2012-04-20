import java.io.IOException;
import java.util.HashMap;

import org.jdom2.JDOMException;


public class Runner {

	public static void main(String[] args) throws IOException, JDOMException {
		
		Parser p = new Parser();
		
		HashMap<Integer, Question> questions = p.parseQuestions();
		HashMap<Integer, DocumentSet> documents = p.parseDocs();
	}

}
