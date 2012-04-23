import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;


public class BaselineDocumentRetriever extends AbstractDocumentRetriever {
	
	private HashMap<String, HashMap<String, Integer>> counts;
	
	public BaselineDocumentRetriever(HashMap<String, Document> ds, HashMap<String, HashMap<String, Integer>> counts) {
		super(ds);
		this.counts = counts;
	}
	
	@Override
	public DocumentSet getDocuments(Question q) {
		
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		
		for (CoreLabel token: q.getTaggedQuestion().get(TokensAnnotation.class)) {
			
			String word = token.get(TextAnnotation.class);
			
			HashMap<String, Integer> wc = counts.get(word);
			for (String s : wc.keySet()) {
				int ins = results.containsKey(s) ? results.get(s) + wc.get(s) : wc.get(s);
				results.put(s, ins);
			}
		}
		
		TreeMap<Integer, ArrayList<String>> ds = new TreeMap<Integer, ArrayList<String>>(Collections.reverseOrder());
		for (String s : results.keySet()) {
			if (!ds.containsKey(results.get(s))) {
				ds.put(results.get(s), new ArrayList<String>());
			}
			ds.get(results.get(s)).add(s);
		}
		
		results.clear();
		for (int i : ds.keySet()) {
			for (String s : ds.get(i)) {
				results.put(s, i);
			}
		}
		
		ArrayList<Document> reta = new ArrayList<Document>();
		int cnt = 0;
		for (String s : results.keySet()) {
			
			reta.add(this.ds.get(s));
			//System.out.println(this.ds.get(s).getText());
			if (cnt > 40) break;
			cnt++;
		}
		
		return new DocumentSet(reta);
	}

}
