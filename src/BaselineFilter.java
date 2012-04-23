import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class BaselineFilter extends AbstractFilter {

	private Question q;
	
	@Override
	public ArrayList<Answer> filter(Question q, DocumentSet ds) {
		
		this.q = q;
		
		ArrayList<Answer> ret = new ArrayList<Answer>();
		
		for (Document d : ds.getDocs()) {
			ret.addAll(extractAnswers(d));
		}
		System.out.println("REALLY???: " + ret.size());
		return ret;
	}
	
	private ArrayList<Answer> extractAnswers(Document d) {
		ArrayList<Answer> as = new ArrayList<Answer>();
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(d.getText());
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		System.out.println("WHAAAA: " + sentences.size());
		for(CoreMap sentence: sentences) {
			as.add(new Answer(sentence.toString()));
		}
		
		return as;
		
	}
}
