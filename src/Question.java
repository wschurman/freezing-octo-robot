import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Question {
	
	private int qid;
	private String question;
	
	private CoreMap sentence;
	private Map<Integer, CorefChain> graph;
	
	public ArrayList<CoreLabel> labels;
	public ArrayList<String> nes;
	
	public question_type qtype;
	public answer_type atype;
	
	public static enum question_type {
		STANDARD,
	}

	public static enum answer_type {
		STANDARD,
	}
	
	public Question(int id, String q) {
		this.setQid(id);
		this.setQuestion(q);
		
		nes = new ArrayList<String>();
		labels = new ArrayList<CoreLabel>();
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
	
	public ArrayList<CoreLabel> getLabels() {
		return labels;
	}
	
	public ArrayList<String> getNes() {
		return nes;
	}
	
}
