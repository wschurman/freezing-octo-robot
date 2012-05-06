import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class MLQuestionClassifier extends AbstractQuestionClassifier{

	private String file;
	private StanfordCoreNLP pipeline;
	private HashMap<String, TrainedQuestionClass> superclassesWord;
	private HashMap<String, TrainedQuestionClass> superclassesPOS;

	private  double WORD_WEIGHT = 2;
	private  double POS_WEIGHT = 1;

	public MLQuestionClassifier(String trainFile){
		file = trainFile;
		pipeline = null;
		superclassesWord = new HashMap<String, TrainedQuestionClass>();
		superclassesPOS = new HashMap<String, TrainedQuestionClass>();

		try {
			System.out.println("Starting");
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos");
			pipeline = new StanfordCoreNLP(props);
			System.out.println("Parsing training");
			parseTraining();
		} catch (Exception e) {
			System.out.println("Failed to load training file: " + file);
			e.printStackTrace();
		}
	}

	private void parseTraining() throws Exception{
		Scanner reader = new Scanner(new File(file));

		while(reader.hasNext()){
			String line = reader.nextLine();
			line = line.substring(0, line.length()-1).trim();
			int split = line.indexOf(" ");


			String knownClass = line.substring(0, split);
			String[] superSub = knownClass.split(":");
			String superClass = superSub[0];
			String subClass = superSub[1];


			String question = line.substring(split+1);

			Annotation a = new Annotation(question);

			pipeline.annotate(a);

			List<CoreMap> sentences = a.get(SentencesAnnotation.class);
			HashMap<String, Double> localWord = new HashMap<String, Double>();
			HashMap<String, Double> localPOS = new HashMap<String, Double>();
			for(CoreMap sentence: sentences) {
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					String word = token.get(TextAnnotation.class).toLowerCase();
					String pos = token.get(PartOfSpeechAnnotation.class).toLowerCase();
					if(!localWord.containsKey(word))
						localWord.put(word, 0.0);
					if(!localPOS.containsKey(pos))
						localPOS.put(pos, 0.0);

					localWord.put(word, localWord.get(word) + 1);
					localPOS.put(pos, localPOS.get(pos) + 1);
				}
			}
			if(!superclassesWord.containsKey(superClass))
				superclassesWord.put(superClass, new TrainedQuestionClass(superClass));
			if(!superclassesPOS.containsKey(superClass))
				superclassesPOS.put(superClass, new TrainedQuestionClass(superClass));
			superclassesWord.get(superClass).addClassExamples(subClass, localWord);
			superclassesPOS.get(superClass).addClassExamples(subClass, localPOS);
		}
	}

	public String getClass(String question, String actual){
		TreeMap<String, Integer> mapping = new TreeMap<String, Integer>();
		
		String abbr = getClass(question, 0.2, 1.6);
		String subAbbr = abbr.split(":")[0];

		if(abbr.equalsIgnoreCase(actual)){
			return "1";
		}
		return "0";
	}
	
	public String getClass(String question, List<String> superclass, double w, double p){
		
		TreeMap<Double, LinkedList<String>> superclassSim = new TreeMap<Double, LinkedList<String>>(Collections.reverseOrder());
		
		Annotation a = new Annotation(question);

		pipeline.annotate(a);
		List<CoreMap> sentences = a.get(SentencesAnnotation.class);
		HashMap<String, Double> localWord = new HashMap<String, Double>();
		HashMap<String, Double> localPOS = new HashMap<String, Double>();
		for(CoreMap sentence: sentences) {
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class).toLowerCase();
				String pos = token.get(PartOfSpeechAnnotation.class).toLowerCase();
				if(!localWord.containsKey(word))
					localWord.put(word, 0.0);
				if(!localPOS.containsKey(pos))
					localPOS.put(pos, 0.0);

				localWord.put(word, localWord.get(word) + 1);
				localPOS.put(pos, localPOS.get(pos) + 1);
			}
		}
		
		String bestSuper = "";
		LinkedList<String> bestSub = null;
		double bestVal = -1000.0;

		for(String s : superclass){

			ArrayList<TrainedQuestionClass> wordSubs = superclassesWord.get(s).getSubclasses();
			ArrayList<TrainedQuestionClass> posSubs = superclassesPOS.get(s).getSubclasses();


			for(int i = 0; i < wordSubs.size(); i++){
				TrainedQuestionClass wordVec = wordSubs.get(i);
				TrainedQuestionClass posVec = posSubs.get(i);

				if(!wordVec.getClassification().equals(posVec.getClassification()))
					System.out.println("Failed assumption check");

				double wordSim = Util.cosineSimilarity(localWord, wordVec.getVector());
				double posSim = Util.cosineSimilarity(localPOS, posVec.getVector());

				double combinedVal = wordSim * WORD_WEIGHT + posSim * POS_WEIGHT;

				if(!superclassSim.containsKey(combinedVal))
					superclassSim.put(combinedVal, new LinkedList<String>());
				//				System.out.println(combinedVal + " - " + wordVec.getClassification());
				superclassSim.get(combinedVal).add(wordVec.getClassification());
			}

			double subclassBest = superclassSim.firstKey(); 
			LinkedList<String> bestSubs = superclassSim.get(subclassBest);
			if(subclassBest > bestVal){
				bestSub = bestSubs;
				bestSuper = s;
				bestVal = subclassBest;
			}
		}

		return bestSuper + ":" + bestSub.get(0);
	}

	public String getClass(String question, double w, double p){
		WORD_WEIGHT = w;
		POS_WEIGHT = p;
		Annotation a = new Annotation(question);

		pipeline.annotate(a);
		List<CoreMap> sentences = a.get(SentencesAnnotation.class);
		HashMap<String, Double> localWord = new HashMap<String, Double>();
		HashMap<String, Double> localPOS = new HashMap<String, Double>();
		for(CoreMap sentence: sentences) {
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class).toLowerCase();
				String pos = token.get(PartOfSpeechAnnotation.class).toLowerCase();
				if(!localWord.containsKey(word))
					localWord.put(word, 0.0);
				if(!localPOS.containsKey(pos))
					localPOS.put(pos, 0.0);

				localWord.put(word, localWord.get(word) + 1);
				localPOS.put(pos, localPOS.get(pos) + 1);
			}
		}

		TreeMap<Double, LinkedList<String>> superclassSim = new TreeMap<Double, LinkedList<String>>(Collections.reverseOrder());


		for(String s : superclassesWord.keySet()){
			double wordSim = Util.cosineSimilarity(localWord, superclassesWord.get(s).getVector());
			double posSim = Util.cosineSimilarity(localPOS, superclassesPOS.get(s).getVector());

			double combinedVal = wordSim * WORD_WEIGHT + posSim * POS_WEIGHT;

			if(!superclassSim.containsKey(combinedVal))
				superclassSim.put(combinedVal, new LinkedList<String>());

			superclassSim.get(combinedVal).add(s);
		}

		double best = superclassSim.firstKey();
		LinkedList<String> mostProbableSuperclasses = superclassSim.get(best);
		superclassSim.clear();


		String bestSuper = "";
		LinkedList<String> bestSub = null;
		double bestVal = -1000.0;


		for(String s : mostProbableSuperclasses){

			ArrayList<TrainedQuestionClass> wordSubs = superclassesWord.get(s).getSubclasses();
			ArrayList<TrainedQuestionClass> posSubs = superclassesPOS.get(s).getSubclasses();


			for(int i = 0; i < wordSubs.size(); i++){
				TrainedQuestionClass wordVec = wordSubs.get(i);
				TrainedQuestionClass posVec = posSubs.get(i);

				if(!wordVec.getClassification().equals(posVec.getClassification()))
					System.out.println("Failed assumption check");

				double wordSim = Util.cosineSimilarity(localWord, wordVec.getVector());
				double posSim = Util.cosineSimilarity(localPOS, posVec.getVector());

				double combinedVal = wordSim * WORD_WEIGHT + posSim * POS_WEIGHT;

				if(!superclassSim.containsKey(combinedVal))
					superclassSim.put(combinedVal, new LinkedList<String>());
				//				System.out.println(combinedVal + " - " + wordVec.getClassification());
				superclassSim.get(combinedVal).add(wordVec.getClassification());
			}

			double subclassBest = superclassSim.firstKey(); 
			LinkedList<String> bestSubs = superclassSim.get(subclassBest);
			if(subclassBest > bestVal){
				bestSub = bestSubs;
				bestSuper = s;
				bestVal = subclassBest;
			}
		}

		return bestSuper + ":" + bestSub.get(0);

	}
	
	public Question.answer_type getClass(String question){
		question = question.toLowerCase();
		
		if(question.contains("time ") || question.contains("hour") || question.contains("minute") || question.contains("second") || question.contains("year"))
			return Question.answer_type.TIME;
		
		if(question.contains("when "))
			return Question.answer_type.TIME;
		
		if(question.contains("who "))
			return Question.answer_type.PERSON;
		
		String ret = getClass(question, 1.6, 0.2);
		String returned = ret.split(":")[0];

		if(returned.equals("HUM"))
			return Question.answer_type.PERSON;
		else if(returned.equals("LOC"))
			return Question.answer_type.LOCATION;
		else if(returned.equals("ENTY"))
			return Question.answer_type.ORGANIZATION;
		else if(ret.contains("date"))
			return Question.answer_type.TIME;
		else if(ret.contains("money"))
			return Question.answer_type.MONEY;
		else if(ret.contains("perc"))
			return Question.answer_type.PERCENT;
		else if(ret.contains("period"))
			return Question.answer_type.TIME;
		else if(returned.equals("NUM"))
			return Question.answer_type.NUMBER;
		else{
			System.out.println("massive failure: " + returned);
			return Question.answer_type.ORGANIZATION;
		}
		
	}

	public static void main (String[] args) throws Exception{
		String train = "train_5500.label.txt";
		MLQuestionClassifier qcl = new MLQuestionClassifier(train);
		System.out.println(qcl.getClass("When did Geraldine Ferraro run for vice president?").name());

	}

	@Override
	public void classifyQuestion(Question q) {
		q.atype = getClass(q.getQuestion());
		System.out.println(q.getQid() + " : " + q.getQuestion() + " : " + q.atype.name());
	}
	
}
