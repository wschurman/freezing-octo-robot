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
import java.util.Properties;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class Parser {
	
	public static final String question_filename = "train/questions.txt";
	public static final String answers_filename = "train/answers.txt";
	public static final String docs_directory = "train/docs";
	
	public HashMap<Integer, Question> questions;
	public HashMap<Integer, DocumentSet> documents_by_qid;
	
	public HashMap<String, Document> raw_documents;
	public HashMap<String, HashMap<String, Integer>> raw_word_counts;
	
	public HashMap<String, HashMap<String, Integer>> raw_stem_counts;
	
	private CleanerProperties props = new CleanerProperties();
	
	public Parser() {
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		props.setUseEmptyElementTags(true);
		props.setAllowHtmlInsideAttributes(true);
	}
	
	public void parseAll() throws IOException {
		questions = parseQuestions();
		documents_by_qid = parseDocs();
		
		tagQuestions();
		
		raw_documents = convertToRawDocuments(documents_by_qid);
		
		tagDocumentsAndBuildIndices();
		
		System.out.println("SIZE: " + raw_documents.size());
	}
	
	private HashMap<String, Document> convertToRawDocuments(HashMap<Integer, DocumentSet> docs_by_qid) {
		HashMap<String, Document> ret = new HashMap<String, Document>();
		
		for (int i : docs_by_qid.keySet()) {
			DocumentSet curr = docs_by_qid.get(i);
			
			for (Document c : curr.getDocs()) {
				ret.put(c.getID(), c);
			}
		}
		
		return ret;
	}

	private HashMap<Integer, Question> parseQuestions() throws IOException {
		
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
	
	
	private void tagQuestions() {
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		for (int i : questions.keySet()) {
			Question q = questions.get(i);
			
			Annotation document = new Annotation(q.getQuestion());
			pipeline.annotate(document);

			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			
			for(CoreMap sentence: sentences) {
				q.setTaggedQuestion(sentence);
				
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					q.nes.add(token.get(NamedEntityTagAnnotation.class));
					q.labels.add(token);
				}
				
				break;
			}

			q.setGraph(document.get(CorefChainAnnotation.class));
		}
		
		
	}
	
	private HashMap<Integer, DocumentSet> parseDocs() throws FileNotFoundException, IOException {
		
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
	
	private DocumentSet parseFile(File file) throws IOException {
		
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
			}
			
			documents.add(new Document(data));
		}
	
		return new DocumentSet(qid, documents);
	}
	
	private void tagDocumentsAndBuildIndices() {
		raw_word_counts = new HashMap<String, HashMap<String, Integer>>();
		raw_stem_counts = new HashMap<String, HashMap<String, Integer>>();
		
		for (String s : raw_documents.keySet()) {
			Document doc = raw_documents.get(s);
			
			if (doc.getText() != null) {
				String[] content = doc.getText().toLowerCase().replaceAll("\'", "").replaceAll("[^a-z0-9 ]", " ").replaceAll("( )+", " ").split(" ");
				
				for (String q : content) {
					String stemmed = Util.stemmer.stem(q);
					incrementCount(doc, q, stemmed);
				}
			}
		}
		
		System.out.println("BaselineDocumentRetriever counts size: "+raw_word_counts.size());
	}
	
	private void incrementCount(Document d, String real, String stem) {
		if (!raw_word_counts.containsKey(real)) {
			raw_word_counts.put(real, new HashMap<String, Integer>());
		}
		if(!raw_stem_counts.containsKey(stem)){
			raw_stem_counts.put(stem, new HashMap<String, Integer>());
		}
		
		HashMap<String, Integer> curr = raw_word_counts.get(real);
		HashMap<String, Integer> stemCurr = raw_stem_counts.get(stem);

		
		
		if (!curr.containsKey(d.getID())) {
			curr.put(d.getID(), 0);
		}
		
		curr.put(d.getID(), curr.get(d.getID()) + 1);
		
		if (!stemCurr.containsKey(d.getID())) {
			stemCurr.put(d.getID(), 0);
		}
		
		stemCurr.put(d.getID(), stemCurr.get(d.getID()) + 1);
	}
}
