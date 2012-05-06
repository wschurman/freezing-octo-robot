import java.util.ArrayList;
import java.util.HashMap;


public class TrainedQuestionClass {
	
	private String classification;
	
	private ArrayList<TrainedQuestionClass> subclasses;
	private HashMap<String, Double> vector;
	
	public TrainedQuestionClass(String c){
		this.classification = c;
		subclasses = new ArrayList<TrainedQuestionClass>();
	}
	
	public String getClassification(){
		return classification;
	}
	
	public HashMap<String, Double> getVector(){
		if(vector != null)
			return vector;
		
		if(subclasses.size() == 0)
			return null;
		
		HashMap<String, Double> combined = new HashMap<String, Double>();
		for(TrainedQuestionClass t : subclasses){
			HashMap<String, Double> sub = t.getVector();
			for(String s : sub.keySet()){
				if(!combined.containsKey(s))
					combined.put(s, 0.0);
				combined.put(s, combined.get(s) + sub.get(s));
			}
		}
		vector = combined;
		return combined;
	}
	
	public HashMap<String, Double> getClassVector(String sub){
		for(TrainedQuestionClass t : subclasses){
			if(t.getClassification().equalsIgnoreCase(sub)){
				return t.getVector();
			}
		}
		return null;
	}
	
	public void addClassExamples(String sub, HashMap<String, Double> vec){
		for(TrainedQuestionClass t : subclasses){
			if(t.getClassification().equalsIgnoreCase(sub)){
				t.addExamples(vec);
				return;
			}
		}
		TrainedQuestionClass t = new TrainedQuestionClass(sub);
		t.addExamples(vec);
		subclasses.add(t);
	}
	
	public void addExamples(HashMap<String, Double> vec){
		if(vector == null)
			vector = vec;
		else{
			for(String s : vec.keySet()){
				if(!vector.containsKey(s))
					vector.put(s, 0.0);
				vector.put(s, vector.get(s) + vec.get(s));
			}
		}
	}
	
	public ArrayList<TrainedQuestionClass> getSubclasses(){
		return subclasses;
	}
	
	

}
