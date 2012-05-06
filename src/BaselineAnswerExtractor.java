import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;


public class BaselineAnswerExtractor extends AbstractAnswerExtractor {

	private final int NUM_ANS = 5;
	
	@Override
	public ArrayList<Answer> extractAnswers(Question q, ArrayList<Answer> as) {
		
		HashMap<String, Double> wikiQuestion = q.getWikiQuestion();
		RankMap<Double, Answer> rank = new RankMap<Double, Answer>(Collections.reverseOrder());
		for(Answer ans : as){
			HashMap<String, Double> ansVector = getVector(ans);
			double sim = Util.cosineSimilarity(ansVector, wikiQuestion);
			rank.put(sim, ans);
		}
		ArrayList<Answer> toRet = new ArrayList<Answer>(rank.getOrderedValues(NUM_ANS));
		for(Answer ans : toRet){
			System.out.println(rank.getValue(ans) + " :ans: " + ans.answer);
		}
		return toRet;
	}

	private HashMap<String, Double> getVector(Answer ans){
		HashMap<String, Double> wordCounts = new HashMap<String, Double>();

		String a = ans.answer.toLowerCase().replaceAll("[^a-z0-9 ]", "");
		String words[] = a.split(" ");
		for(String word : words){
			if(Util.stopwords.contains(word))
				continue;
			if(!wordCounts.containsKey(word))
				wordCounts.put(word, 0.0);
			wordCounts.put(word, wordCounts.get(word) + 1.0);
		}

		return wordCounts;
	}
	
	private HashMap<String, Double> getVector(ArrayList<String> question){
		HashMap<String, Double> wordCounts = new HashMap<String, Double>();
		for(String s : question){
			if(!wordCounts.containsKey(s))
				wordCounts.put(s, 0.0);
			wordCounts.put(s, wordCounts.get(s) + 1.0);
		}
		return wordCounts;
	}

	private HashMap<String, Double> getVector(Question q){
		HashMap<String, Double> wordCounts = new HashMap<String, Double>();

		CoreMap sentence = q.getTaggedQuestion();
		for(CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.getString(TextAnnotation.class);
			if(!wordCounts.containsKey(word))
				wordCounts.put(word, 0.0);
			wordCounts.put(word, wordCounts.get(word) + 1.0);
		}

		return wordCounts;
	}

	private double computeCosineSimilarity(HashMap<String, Double> scores1, HashMap<String, Double> scores2){
		double s1 = 0.0;
		for(String s : scores1.keySet()){
			s1+= scores1.get(s)*scores1.get(s);
		}
		
		double s2 = 0.0;
		for(String s : scores2.keySet()){
			s2+= scores2.get(s)*scores2.get(s);
		}
		
		double sum = 0.0;
		for(String s : scores2.keySet()){
			if(scores1.containsKey((s)))
				sum+= scores2.get(s) * scores1.get(s);		
		}
		return sum / (s1 * s2);
	}

	private void normalizeVector(HashMap<String, Double> input){
		double tot = 0.0;
		for(String i : input.keySet()){
			tot += input.get(i) * input.get(i);
		}
		double total = (double) Math.sqrt(tot);

		for(String i : input.keySet()){
			double f = (double) (input.get(i) / total);
			input.put(i, f);
		}

	}

}
