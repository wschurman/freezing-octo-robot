import java.io.IOException;
import java.util.HashMap;

import org.jdom2.JDOMException;


public class Runner {
	
	private HashMap<Integer, Question> questions;
	
	public static void main(String[] args) throws IOException, JDOMException {
		
		Parser p = new Parser();
		p.parseAll();
		
	}

}
