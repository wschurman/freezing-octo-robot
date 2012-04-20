import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class Parser {
	
	public static final String question_filename = "train/questions.txt";
	public static final String answers_filename = "train/answers.txt";
	public static final String docs_directory = "train/docs/";
	
	public ArrayList<Question> parseQuestions() throws IOException {
		
		System.out.println("Parsing Questions...");
		ArrayList<Question> qs = new ArrayList<Question>();
		
		CleanerProperties props = new CleanerProperties();

		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		props.setUseEmptyElementTags(true);
		props.setAllowHtmlInsideAttributes(true);

		TagNode tagNode = new HtmlCleaner(props).clean(
		    new File(question_filename)
		);
		
		TagNode[] questions = tagNode.getElementsByName("top", false);
		
		for(TagNode e : questions){
			
			TagNode numnode = e.getElementsByName("num", false)[0];
			TagNode descnode = numnode.getElementsByName("desc", false)[0];
			
			String desc = descnode.getText().toString().replace("Description:", "").trim();
			numnode.removeChild(descnode);
			int num = Integer.parseInt(numnode.getText().toString().replace("Number:", "").trim());
			
			qs.add(new Question(num, desc));
		}
		
		return qs;
	}
	
	public void parseDocs() {
		
	}
}
