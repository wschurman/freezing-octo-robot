import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

/*
 * Represents a question, and all it's parsed components and Wikipedia insertions.
 */
public class Question {
	
	private int qid;
	private String question;
	
	private CoreMap sentence;
	private Map<Integer, CorefChain> graph;
	
	public ArrayList<CoreLabel> labels;
	public ArrayList<String> nes;
	
	public question_type qtype;
	public answer_type atype;
	
	private HashMap<String, Double> wikiContext;
	
	public static enum question_type {
		STANDARD,
	}

	public static enum answer_type {
		TIME, LOCATION, ORGANIZATION, PERSON, MONEY, PERCENT, // NER values
		
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
		wikiContext = null;
	}
	
	public HashMap<String, Double> getNouns(){
		HashMap<String, Double> important = new HashMap<String, Double>();
		for(CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.getString(TextAnnotation.class).toLowerCase();
			String pos = token.get(PartOfSpeechAnnotation.class);
//			System.out.println(word + " \t" + pos);
			if(pos.contains("NNP")){
				important.put(word, 4.0);
			}
			else if(pos.contains("NN")){
				important.put(word, 3.0);
			}
			else if(pos.contains("JJ") || pos.contains("VBN") || pos.contains("VBG")){
				System.out.println(pos + " " + word);
				important.put(word, 1.5);
			}
			else if(pos.contains("CD")){
				important.put(word, 0.5);
			}
		}
		return important;
	}
	
	public HashMap<String, Double> getWikiQuestion(){
		if(wikiContext == null){
			LinkedList<String> important = new LinkedList<String>();
			String prev = "";
			String current = null;
			for(CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.getString(TextAnnotation.class).toLowerCase();
				String pos = token.get(PartOfSpeechAnnotation.class);
				if((prev.contains("JJ") || prev.contains("NN")) && pos.contains("NN")){
					current += "_" + word.substring(0, 1).toUpperCase()+word.substring(1);
					prev = pos;
				}
				else if(Util.relevantPOS.contains(pos) && !Util.stopwords.contains(word)){
					if(current != null){
						String toAdd = current.substring(0, 1).toUpperCase()+current.substring(1);
						if(prev.contains("NN"))
							important.addFirst(toAdd);
						else
							important.addLast(toAdd);
					}
					current = word;
					prev = pos;
				}
				else{
					if(current != null){
						String toAdd = current.substring(0, 1).toUpperCase()+current.substring(1);
						if(prev.contains("NN"))
							important.addFirst(toAdd);
						else
							important.addLast(toAdd);
					}
					prev = pos;
					current = null;
				}
			}
			if(current != null){
				String toAdd = current.substring(0, 1).toUpperCase()+current.substring(1);
				if(prev.contains("NN"))
					important.addFirst(toAdd);
				else
					important.addLast(toAdd);
			}
			
			for(String s : important){
				System.out.println(s);
			}
			System.out.println("=============");
			wikiContext = WikiParser.newWikiSearch(important, question);
			RankMap<Double, String> best = new RankMap<Double, String>(wikiContext, Collections.reverseOrder());
			List<String> top = best.getOrderedValues(25);
			wikiContext.clear();
			for(String s : top){
				wikiContext.put(s, best.getValue(s));
				System.out.println(s + " - " + wikiContext.get(s));
			}
		}
		return wikiContext;
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
