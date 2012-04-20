import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class Parser {
	
	public static final String question_filename = "train/questions.txt";
	public static final String answers_filename = "train/answers.txt";
	public static final String docs_directory = "train/docs";
	
	public HashMap<Integer, Question> parseQuestions() throws IOException {
		
		System.out.println("Parsing Questions...");
		HashMap<Integer, Question> qs = new HashMap<Integer, Question>();
		
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
			
			qs.put(num, new Question(num, desc));
		}
		
		return qs;
	}
	
	public HashMap<Integer, DocumentSet> parseDocs() throws FileNotFoundException, IOException {
		
		HashMap<Integer, DocumentSet> set = new HashMap<Integer, DocumentSet>();
		
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".");
		    }
		};
		
		File dir = new File(docs_directory);
		
		File[] files = dir.listFiles(filter);
		
		for (File f : files) {
			DocumentSet s = parseFile(f);
			set.put(s.getQid(), s);
		}
		
		return set;
	}
	
	private DocumentSet parseFile(File file) throws FileNotFoundException, IOException {
		
		
		
		//DocumentSet s = new DocumentSet();
	}
}
