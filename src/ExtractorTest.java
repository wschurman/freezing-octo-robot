import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class ExtractorTest {
	
	public static void main(String[] args) throws Exception{

		
		Parser p = new Parser();
		p.parseAll();
		
		int avg = 0;
		for(int i = 201; i < 400; i++){
			int res = testQuestion(p, i);
			avg += res;
			System.out.println(i + " : " + res);
		}
		System.out.println("OVERALL AVERAGE: " + avg / 199.0);
		
	}
	
	
	static int testQuestion(Parser p, int qid) throws Exception{
		
		HtmlCleaner clean = new HtmlCleaner();
		TagNode root = clean.clean(new File("./train/docs/top_docs."+qid));
		root.getElementsByName("DOCNO", true);
		TagNode[] docs = root.getElementsByName("DOCNO", true);
		HashMap<String, Integer> proper = new HashMap<String, Integer>();
		int i = 1;
		for(TagNode t : docs){
			proper.put(t.getText().toString().trim(), i);
			i++;
		}
		
		Question q = p.questions.get(qid);
		(new BaselineQuestionClassifier()).classifyQuestion(q);
		DocumentSet d = (new BaselineDocumentRetriever(p.raw_documents, p.raw_word_counts, p.raw_stem_counts)).getDocuments(q);
		ArrayList<Document> ds = d.getDocs();
		int myId = 1;
		int count = 0;
		for(Document zard : ds){
			String id = zard.getID();
			Integer found = proper.get(id);
			if(found == null)
				found = -1;
			if(found > 0 && found < 6)
				count++;
			myId++;
		}
		
		return count;
	}

}
