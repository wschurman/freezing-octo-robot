import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class WikiFilter extends AbstractFilter {

	private Question q;
	
	private StanfordCoreNLP pipeline;
	
	private final int TOP_DOCS = 7;
	private final int GROUPING = 1;
	
	public WikiFilter() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}
	
	@Override
	public ArrayList<Answer> filter(Question q, DocumentSet ds) {
		System.out.println("SIZE: " + ds.getDocs().size());
		this.q = q;
		
		ArrayList<Answer> ret = new ArrayList<Answer>();
		RankMap<Double, Document> docs = new RankMap<Double, Document>(Collections.reverseOrder());
		for (Document d : ds.getDocs()) {
			double sim = filterSim(q, d);
			docs.put(sim, d);
		}
		List<Document> bestDocs = docs.getOrderedValues(15);
				
		for (Document d : bestDocs) {
			ret.addAll(extractAnswers(d));
		}
		
		return ret;
	}
	
	private double filterSim(Question q, Document d){
		HashMap<String, Double> questionVector = q.getWikiQuestion();
		HashMap<String, Double> docVector = getDocumentVector(d);
		double sim = Util.cosineSimilarity(questionVector, docVector);
		
		return sim;
	}
	
	private HashMap<String, Double> getDocumentVector(Document d){
		HashMap<String, Double> dVec = new HashMap<String, Double>();
		String text = d.getText().toLowerCase();
		String[] words = text.split(" ");
		for(String s : words){
			String a = s.replaceAll("[^a-z0-9]", "");
			if(!dVec.containsKey(a) && !Util.stopwords.contains(a)){
				dVec.put(a, 0.0);
			}
			else if(dVec.containsKey(a))
				dVec.put(a, dVec.get(a) + 1.0);
		}
		return dVec;
	}
	
	private ArrayList<Answer> extractAnswers(Document d) {
		ArrayList<Answer> as = new ArrayList<Answer>();
		
		Annotation document = new Annotation(d.getText());
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		int count = 0;
		String nextAnswer = "";
		for(CoreMap sentence: sentences) {
			if(count == GROUPING){
				as.add(new Answer(nextAnswer, d.getID()));
				nextAnswer = "";
				count = 0;
			}
			nextAnswer += sentence.toString().replaceAll("\n", "");
			count++;
		}
		if(nextAnswer.length() > 3)
			as.add(new Answer(nextAnswer, d.getID()));
		
		return as;
		
	}

}
