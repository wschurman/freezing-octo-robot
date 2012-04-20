import java.io.IOException;
import java.util.HashMap;

import org.jdom2.JDOMException;


public class Runner {

	public static void main(String[] args) throws IOException, JDOMException {
		
		Parser p = new Parser();
		
		HashMap<Integer, Question> questions = p.parseQuestions();
		
		try {
			HashMap<Integer, DocumentSet> documents = p.parseDocs();
		} catch (Exception e) {
			System.out.println("Error, probably no train/docs directory, download it from http://cl.ly/2c3q3D3Y0T2K2i0o2B0V");
		}
	}

}
