import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


public class Util {
	
	static final HashSet<String> stopwords = new HashSet<String>();
	static final HashSet<String> relevantPOS = new HashSet<String>();
	static final HashSet<String> errorLog = new HashSet<String>();
	static final LovinsStemmer stemmer = new LovinsStemmer();
//	static final Paice stemmer = new Paice("stemrules.txt", "/p");
	static PrintWriter errorLogger;
	static{
		try {
			relevantPOS.add("NN");
			relevantPOS.add("NNS");
			relevantPOS.add("NNP");
			relevantPOS.add("NNPS");
			relevantPOS.add("JJ");
			relevantPOS.add("JJS");
			relevantPOS.add("CD");
			relevantPOS.add("VBN");
			relevantPOS.add("VBG");
			relevantPOS.add("VBP");
			relevantPOS.add("VBD");
			
			Scanner read = new Scanner(new File("./wiki/error.log"));
			while(read.hasNext()){
				errorLog.add(read.nextLine());
			}
			
			errorLogger = new PrintWriter(new FileWriter(new File("./wiki/error.log"), true));
			
			Scanner reader = new Scanner(new File("stopwords.dat"));
			stopwords.add(" ");
			stopwords.add("");
			while(reader.hasNext())
				stopwords.add(reader.nextLine());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void appendSiteError(String site){
		errorLogger.append(site);
		errorLogger.flush();
	}
	
	static double getLength(HashMap<String, Double> v){
		double length = 0.0;
		for(String s : v.keySet()){
			double val = v.get(s);
			length += val * val;
		}
		length = Math.sqrt(length);
		return length;
	}
	
	static HashMap<String, Double> normalize(HashMap<String, Double> vals){
		double length = getLength(vals);
		
		HashMap<String, Double> fresh = new HashMap<String, Double>();
		for(String s : vals.keySet()){
			fresh.put(s, vals.get(s) / length);
		}
		return fresh;
	}
	
	static double cosineSimilarity(HashMap<String, Double> v1, HashMap<String, Double> v2){
		double lenOne = getLength(v1);
		double lenTwo = getLength(v2);
		double sim = 0.0;
		for(String s : v1.keySet()){
			if(v2.containsKey(s)){
				sim += v1.get(s) * v2.get(s);
			}
		}
		return sim / (lenOne * lenTwo);
	}

}
