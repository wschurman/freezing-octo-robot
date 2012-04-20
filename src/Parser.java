import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.jdom2.JDOMException;


public class Parser {
	
	public static final String question_filename = "train/questions.txt";
	public static final String answers_filename = "train/answers.txt";
	public static final String docs_directory = "train/docs";
	
	private CleanerProperties props = new CleanerProperties();
	
	public Parser() {
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		props.setUseEmptyElementTags(true);
		props.setAllowHtmlInsideAttributes(true);
	}
	
	public HashMap<Integer, Question> parseQuestions() throws IOException {
		
		System.out.println("Parsing Questions...");
		HashMap<Integer, Question> qs = new HashMap<Integer, Question>();
		

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

		System.out.println("Done.");
		return qs;
	}
	
	public HashMap<Integer, DocumentSet> parseDocs() throws FileNotFoundException, IOException, JDOMException {
		
		System.out.println("Parsing Documents...");
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
		
		System.out.println("Done.");
		return set;
	}
	
	private static String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}
	
	private DocumentSet parseFile(File file) throws JDOMException, IOException {
		
		String fname = file.getName();
		int qid = Integer.parseInt(fname.replaceAll( "[^\\d]", "" ));
		
		ArrayList<Document> documents = new ArrayList<Document>();
		
		String x = "<root>"+readFile(file)+"</root>";
		
		TagNode tagNode = new HtmlCleaner(props).clean(x);
		TagNode[] docs = tagNode.getElementsByName("doc", false);
		
		for(TagNode e : docs){
			
			HashMap<String, String> data = new HashMap<String, String>();
			
			TagNode[] childs = e.getAllElements(false);
			for (TagNode f : childs) {
				data.put(f.getName(), f.getText().toString().trim());
				//System.out.println(f.getName() + ", " + f.getText().toString().trim());
			}
			
			documents.add(new Document(data));
		}
	
		return new DocumentSet(qid, documents);
	}
}
