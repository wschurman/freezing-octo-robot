import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * Class written to enable Map sorted by value.
 */
public class RankMap<V extends Number, O> {
	
	TreeMap<V, LinkedList<O>> map;
	int size;
	
	public RankMap(){
		map = new TreeMap<V, LinkedList<O>>();
		size = 0;
	}
	
	public RankMap(Comparator<Object> comp){
		map = new TreeMap<V, LinkedList<O>>(comp);
		size = 0;
	}
	
	public RankMap(Map<O, V> m){
		map = new TreeMap<V, LinkedList<O>>();
		for(O obj : m.keySet()){
			put(m.get(obj), obj);
		}
		size = m.size();
	}
	
	public RankMap(Map<O, V> m, Comparator<Object> comp){
		map = new TreeMap<V, LinkedList<O>>(comp);
		for(O obj : m.keySet()){
			put(m.get(obj), obj);
		}
		size = m.size();
	}
	
	public void put(V val, O obj){
		if(!map.containsKey(val)){
			map.put(val, new LinkedList<O>());
		}
		map.get(val).add(obj);
		size++;
	}
	
	public void lowToHigh(){
		TreeMap<V, LinkedList<O>> map2 = new TreeMap<V, LinkedList<O>>();
		for(V val : map.keySet()){
			map2.put(val, map.get(val));
		}
		map = map2;
	}
	
	public void highToLow(){
		TreeMap<V, LinkedList<O>> map2 = new TreeMap<V, LinkedList<O>>(Collections.reverseOrder());
		for(V val : map.keySet()){
			map2.put(val, map.get(val));
		}
		map = map2;
	}
	
	public LinkedList<O> get(V val){
		return map.get(val);
	}
	
	public Set<V> keySet(){
		return map.keySet();
	}
	
	public List<O> getOrderedValues(){
		ArrayList<O> l = new ArrayList<O>();
		for(V val : map.keySet()){
			l.addAll(map.get(val));
		}
		return l;
	}
	
	public List<O> getOrderedValues(int n){
		List<O> l = getOrderedValues();
		int max = Math.min(n, l.size());
		return l.subList(0, max);
	}
	
	public V getValue(O obj){
		for(V val : map.keySet()){
			LinkedList<O> set = map.get(val);
			if(set.contains(obj))
				return val;
		}
		return null;
	}
	
	public void removeObject(O obj){
		for(V val : map.keySet()){
			LinkedList<O> set = map.get(val);
			if(set.contains(obj)){
				set.remove(obj);
				size--;
				return;
			}
		}
	}
	
	public int objectSize(){
		return size;
	}
	
	public int keySize(){
		return map.keySet().size();
	}
	
	public static void main(String[] args){
		RankMap<Double, String> map = new RankMap<Double, String>();
		map.put(0.225, "david");
		map.put(0.335, "schurman");
		map.put(1.234, "cat");
		map.put(0.004, "coolio");
		map.put(0.225, "david round 2");
		map.put(0.1, "last one");
		
		List<String> bestToWorst = map.getOrderedValues(8);
		for(String s : bestToWorst)
			System.out.println(s);
		
		map.highToLow();
		System.out.println();
		
		System.out.println(map.getValue("david"));
		
	}

}
