import java.util.List;
import java.util.Map;
import java.util.Properties;


import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class Question {
	private int qid;
	private String question;
	
	private CoreMap sentence;
	private Map<Integer, CorefChain> graph;
	
	public question_type qtype;
	public answer_type atype;
	
	public enum question_type {
		STANDARD,
	}
	
	public enum answer_type {
		STANDARD,
	}
	
	public Question(int id, String q) {
		this.setQid(id);
		this.setQuestion(q);
		
		tagQuestion();
	}
	
	private void tagQuestion() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(question);
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		for(CoreMap sentence: sentences) {
			this.setTaggedQuestion(sentence);
			break;
		}

		this.setGraph(document.get(CorefChainAnnotation.class));
	}
	
	
	
	/* Getters and Setters */
	
	public void setQid(int qid) {
		this.qid = qid;
	}
	public int getQid() {
		return qid;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getQuestion() {
		return question;
	}

	public void setTaggedQuestion(CoreMap sentence) {
		this.sentence = sentence;
	}

	public CoreMap getTaggedQuestion() {
		return sentence;
	}

	public void setGraph(Map<Integer, CorefChain> graph) {
		this.graph = graph;
	}

	public Map<Integer, CorefChain> getGraph() {
		return graph;
	}
	
	
}
