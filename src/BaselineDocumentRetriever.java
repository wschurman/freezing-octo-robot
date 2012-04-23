import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;


public class BaselineDocumentRetriever extends AbstractDocumentRetriever {
	
	private HashMap<String, HashMap<String, Integer>> counts;
	
	public BaselineDocumentRetriever(HashMap<String, Document> ds) {
		super(ds);
		buildIndex();
	}
	
	private void buildIndex() {
		
		counts = new HashMap<String, HashMap<String, Integer>>();
		
		for (String k : this.ds.keySet()) {
			Document doc = this.ds.get(k);
			
			if (doc.getText() != null) {
				String[] content = doc.getText().trim().split(" ");
				
				for (String q : content) {
					incrementCount(doc, q);
				}
			}
		}
		
		System.out.println("BaselineDocumentRetriever counts size: "+counts.size());
		
	}
	
	private void incrementCount(Document d, String s) {
		if (!counts.containsKey(s)) {
			counts.put(s, new HashMap<String, Integer>());
		}
		
		HashMap<String, Integer> curr = counts.get(s);
		
		if (!curr.containsKey(d.getID())) {
			curr.put(d.getID(), 0);
		}
		
		curr.put(d.getID(), curr.get(d.getID()) + 1);
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
		
		TreeMap<Integer, ArrayList<String>> ds = new TreeMap<Integer, ArrayList<String>>();
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
			
			if (cnt > 4) break;
			cnt++;
		}
		
		return new DocumentSet(reta);
	}

}
