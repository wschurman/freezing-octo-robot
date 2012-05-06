import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;


public class BaselineDocumentRetriever extends AbstractDocumentRetriever {

	private HashMap<String, HashMap<String, Integer>> counts;
	private HashMap<String, HashMap<String, Integer>> stem_counts;

	public BaselineDocumentRetriever(HashMap<String, Document> ds, HashMap<String, HashMap<String, Integer>> counts, HashMap<String, HashMap<String, Integer>> s_counts) {
		super(ds);
		this.counts = counts;
		stem_counts = s_counts;
	}

	@Override
	public DocumentSet getDocuments(Question q) {

		HashMap<String, Integer> word_results = new HashMap<String, Integer>();
		HashMap<String, Integer> stem_results = new HashMap<String, Integer>();
		for (CoreLabel token: q.getTaggedQuestion().get(TokensAnnotation.class)) {

			String word = token.get(TextAnnotation.class).toLowerCase();
			String pos = token.get(PartOfSpeechAnnotation.class);

			if(!Util.relevantPOS.contains(pos))
				continue;

			String stemmed = Util.stemmer.stem(word);

			System.out.println("Checking index for: " + word + " " + pos + " stemmed: " + stemmed);

			HashMap<String, Integer> wc = counts.get(word);
			HashMap<String, Integer> sc = stem_counts.get(stemmed);

			if(wc != null){
				for (String s : wc.keySet()) {
					int ins = -1;
					if(pos.contains("NNP") || pos.contains("JJS")){
						ins = word_results.containsKey(s) ? word_results.get(s) + 20*wc.get(s) : 20*wc.get(s);
					}
					else if(pos.contains("NN")){
						ins = word_results.containsKey(s) ? word_results.get(s) + 5*wc.get(s) : 5*wc.get(s);
					}
					else{
						ins = word_results.containsKey(s) ? word_results.get(s) + wc.get(s) : wc.get(s);
					}
					word_results.put(s, ins);
				}
			}
			if(sc == null)
				continue;

			for (String s : sc.keySet()) {
				int ins = -1;
				if(pos.contains("NNP") || pos.contains("JJS")){
					ins = stem_results.containsKey(s) ? stem_results.get(s) + 20*sc.get(s) : 20*sc.get(s);
				}
				else if(pos.contains("NN")){
					ins = stem_results.containsKey(s) ? stem_results.get(s) + 5*sc.get(s) : 5*sc.get(s);
				}
				else{
					ins = stem_results.containsKey(s) ? stem_results.get(s) + sc.get(s) : sc.get(s);
				}
				stem_results.put(s, ins);
			}
		}
		System.out.println();

		RankMap<Integer, String> ds = new RankMap<Integer, String>(Collections.reverseOrder());

		for (String s : stem_results.keySet()) {
			if(word_results.containsKey(s)){
				word_results.put(s, word_results.get(s) + stem_results.get(s));
			}
			else
				word_results.put(s, stem_results.get(s));
		}
		for(String s : word_results.keySet())
			ds.put(word_results.get(s), s);
		
		word_results.clear();
		stem_results.clear();

		List<String> best = ds.getOrderedValues(50);


		ArrayList<Document> reta = new ArrayList<Document>();
		int ind = 1;
		for (String s : best) {
			ind++;
			reta.add(this.ds.get(s));
		}

		return new DocumentSet(reta);
	}

}
