import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
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

//		String loc = getClass(question, 1.4, 0.8);
//		String subLoc = loc.split(":")[0];
//		
//		String num = getClass(question, 2.2, 1.4);
//		String subNum = num.split(":")[0];
//		
//		String hum = getClass(question, 1.6, 1.0);
//		String subHum = hum.split(":")[0];
//		
//		String enty = getClass(question, 4.4, 0.4);
//		String subEnty = enty.split(":")[0];
//		
//		String desc = getClass(question, 0.2, 1.0);
//		String subDesc = desc.split(":")[0];
//		
//		String[] vals = {subAbbr, subDesc, subEnty, subHum, subLoc, subNum};
//		HashSet<String> set = new HashSet<String>();
//		for(String s : vals)
//			set.add(s);
//		
//		ArrayList<String> best = new ArrayList<String>();
//		best.addAll(set);
//		
//		String ret = getClass(question, best, 3.2, 0.4 );
		if(abbr.equalsIgnoreCase(actual)){
			return "1";
		}
		return "0";
//		
//		String max = "";
//		double valMax = 0.0;
//		
//		for(String s :vals){
//			if(!mapping.containsKey(s)){
//				mapping.put(s, 0);
//			}
//			mapping.put(s, mapping.get(s) + 1);
//			if(mapping.get(s) > valMax){
//				valMax = mapping.get(s);
//				max = s;
//			}
//		}
//		
//
//		if(mapping.containsKey(actual)){
////			System.out.println("================");
////			System.out.println(actual);
////			for(String s : mapping.keySet()){
////				System.out.println("   " + s + " : " + mapping.get(s));
////			}
//			return "1";
//		}
//		else
//			return "0";

//		if(max.equalsIgnoreCase(actual)){
//			return "1";
//		}
//		else{
//			return "0";
//		}
//				return "";
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

	//	public String getClass(String question){
	public String getClass(String question, double w, double p){
		WORD_WEIGHT = w;
		POS_WEIGHT = p;
		//		question = question.replaceAll("?", "").trim();
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

			//			System.out.println(combinedVal + " - " + s);
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

		
		//Time, Location, Organization, Person, Money, Percent, Date
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
////			for(double pos = 0.0; pos <= 1.6; pos += 0.2)
////				for(double word = 0.2; word <= 5; word += 0.2){
//					Scanner reader = new Scanner(new File(train));
//					int correct = 0;
//					int total = 0;
//	//				TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
//	//				HashMap<String, Integer> total = new HashMap<String, Integer>();
//					while(reader.hasNext()){
//						String line = reader.nextLine();
//						line = line.substring(0, line.length()-1).trim();
//						int split = line.indexOf(" ");
//						String knownClass = line.substring(0, split);
//						String question = line.substring(split+1);
//						String ret = qcl.getClass(question, 1.6, 0.2);
//						String top = knownClass.split(":")[0];
//						String returned = ret.split(":")[0];
//						if(top.equalsIgnoreCase(returned)){
//							total++;
//							if(knownClass.equalsIgnoreCase(ret)){
//								correct++;
//							}
//	//						if(!counter.containsKey(top))
//	//							counter.put(top, 0);
//	//						counter.put(top, counter.get(top) + 1);
//						}
//	//					if(!total.containsKey(top))
//	//						total.put(top, 0);
//	//					total.put(top, total.get(top) + 1);
//					}
//					System.out.println("1.6" + "\t" + "0.2" + "\t" + (correct * 1.0 / total) );
//	
//	//				for(String s : counter.keySet()){
//	//					System.out.print(s + "\t");
//	//				}
//	//				System.out.println();
//	//				System.out.println(word + "\t" + pos + "\t" + (correct * 1.0 / total));
//	
				}

		@Override
		public void classifyQuestion(Question q) {
			q.atype = getClass(q.getQuestion());
			System.out.println(q.getQid() + " : " + q.getQuestion() + " : " + q.atype.name());
		}
		
		
		
//	public static void main(String [] args) throws Exception{
//		String train = "train_5500.label.txt";
//		QuestionClassLearner qcl = new QuestionClassLearner(train);
//		Scanner reader = new Scanner(new File(train));
//		int correct = 0;
//		int total = 0;
//		//					TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
//		//					HashMap<String, Integer> total = new HashMap<String, Integer>();
//		while(reader.hasNext()){
//			total++;
//			String line = reader.nextLine();
//			line = line.substring(0, line.length()-1).trim();
//			int split = line.indexOf(" ");
//			String knownClass = line.substring(0, split);
//			String question = line.substring(split+1);
//			String top = knownClass;
//			if(qcl.getClass(question, top).equalsIgnoreCase("1"))
//				correct++;
//			//						System.out.println("======");
//		}
//		System.out.println(correct * 1.0 / total);
//
//
//	}







}
