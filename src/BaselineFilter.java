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
	
	private StanfordCoreNLP pipeline;
	
	public BaselineFilter() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		pipeline = new StanfordCoreNLP(props);
	}
	
	@Override
	public ArrayList<Answer> filter(Question q, DocumentSet ds) {
		
		this.q = q;
		
		ArrayList<Answer> ret = new ArrayList<Answer>();
		
		for (Document d : ds.getDocs()) {
			ret.addAll(extractAnswers(d));
			break;
		}
		return ret;
	}
	
	public ArrayList<Answer> filter(ArrayList<Answer> ans, Question q){
		ArrayList<Answer> toRet = new ArrayList<Answer>();
		for(Answer a : ans){
			Annotation document = new Annotation(a.answer);
			pipeline.annotate(document);
			
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			CoreMap sentence = sentences.get(0);
			
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String ne = token.get(NamedEntityTagAnnotation.class);
				if(ne.equals("DATE"))
					ne = "TIME";
				if (ne.equalsIgnoreCase(q.atype.name())) {
					toRet.add(a);
					break;
				}
			}
			
		}
		return toRet;

	}
	
	private ArrayList<Answer> extractAnswers(Document d) {
		ArrayList<Answer> as = new ArrayList<Answer>();
		
		Annotation document = new Annotation(d.getText());
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			boolean add = false;
			
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String ne = token.get(NamedEntityTagAnnotation.class);
				if (ne.equalsIgnoreCase(q.atype.name())) {
					add = true;
					break;
				}
			}
			if (add) {
				as.add(new Answer(sentence.toString().replaceAll("\n", ""), d.getID()));
			}
		}
		
		return as;
		
	}
}
