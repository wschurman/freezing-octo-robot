import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
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
		TIME, LOCATION, ORGANIZATION, PERSON, MONEY, PERCENT, DATE, // NER values
		
		STANDARD, NUMBER, PLACE, //standard categories
		ABBREVIATION,
		DEFINITION, DESCRIPTION, REASON,
		ANIMAL, BODY, COLOR, CURRENCY, EVENT, FOOD, LANGUAGE, LETTER, OTHER, PLANT, PRODUCT,
		RELIGION, SPORT, SYMBOL, TECHNIQUE, VEHICLE, WORD,
		GROUP, INDIVIDUAL, TITLE,
		CITY, COUNTRY, STATE, 
		CODE, COUNT, DISTANCE, ORDER, PERIOD,
		SPEED, TEMPERATURE, SIZE, WEIGHT
	}
	
	public Question(int id, String q) {
		this.setQid(id);
		this.setQuestion(q);
		
		nes = new ArrayList<String>();
		labels = new ArrayList<CoreLabel>();
	}
	
	public ArrayList<String> getNouns(){
		ArrayList<String> important = new ArrayList<String>();
		for(CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.getString(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			if(pos.contains("NN"))
				important.add(word);
		}
		return important;
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
