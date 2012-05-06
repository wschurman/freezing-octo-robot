import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class WikiParser {
	
	private static String searchSite = "http://en.wikipedia.org/w/index.php?title=Special%3ASearch&profile=default&fulltext=Search&search=";
	private static String targetSite = "http://en.wikipedia.org/wiki/";
	
	private final static int THROTTLE = 1000;
	
	static List<String> searchBroadSites(List<String> nouns){
		LinkedList<String> toRet = new LinkedList<String>();
		String together = "";
		for(int i = 0; i < nouns.size()-1; i++){
			together += nouns.get(i) + "_";
		}
		together += nouns.get(nouns.size() - 1);
		
		String fullURL = searchSite + together;
		File f = new File("./wiki/" + together + "-search.wiki");
		boolean exists = f.exists();
		String siteInfo = getSiteInfo(fullURL, f);
		
		HtmlCleaner clean = new HtmlCleaner();
		TagNode node = clean.clean(siteInfo);
		
		TagNode[] results = node.getElementsByAttValue("class", "searchresult", true, false);
		TagNode[] headers = node.getElementsByAttValue("class", "mw-search-result-heading", true, false);
		
		int max = Math.min(3, results.length);
		for(int i = 0; i < max; i++){
			TagNode res = results[i];
			TagNode header = headers[i];
			TagNode[] href = header.getElementsByName("a", false);
			TagNode anchor = href[0];
			String link = anchor.getAttributeByName("href");
			String url = link.substring(link.lastIndexOf("/") + 1);
			toRet.add(url);
		}
		return toRet;
		
	}
	
	static String searchAllSites(List<String> nouns){
		String together = "";
		for(int i = 0; i < nouns.size()-1; i++){
			together += nouns.get(i) + "_";
		}
		together += nouns.get(nouns.size() - 1);
		
		String fullURL = searchSite + together;
		File f = new File("./wiki/" + together + "-search.wiki");
		boolean exists = f.exists();
		String siteInfo = getSiteInfo(fullURL, f);
		
		HtmlCleaner clean = new HtmlCleaner();
		TagNode node = clean.clean(siteInfo);
		TagNode[] results = node.getElementsByAttValue("class", "searchresult", true, false);
		TagNode[] headers = node.getElementsByAttValue("class", "mw-search-result-heading", true, false);
		int max = Math.min(3, results.length);
		String all = "";
		for(int i = 0; i < max; i++){
			TagNode res = results[i];
			TagNode header = headers[i];
			all += header.getText() + " " + res.getText();
		}
		System.out.println(all);
		try {
			if(!exists && !Util.errorLog.contains(f.getName())){
				System.err.println("Throttling " + THROTTLE + " ms");
				Thread.sleep(THROTTLE);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return all;
	}
	
	static String searchTargetSite(List<String> nouns, String question){
		HashSet<String> qWords = new HashSet<String>();
		String[] brokenDown = question.split(" ");
		for(String s : brokenDown){
			if(!Util.stopwords.contains(s))
				qWords.add(s);
		}
		String targetInfo = searchTargetSite(nouns);
		String[] sentences = targetInfo.split("[.?!]");
		RankMap<Integer, String> sentencePerformance = new RankMap<Integer, String>(Collections.reverseOrder());
		for(String sentence : sentences){
			String[] words = sentence.split(" ");
			int count = 0;
			for(String word : words){
				if(qWords.contains(word)){
					count++;
				}
			}
			sentencePerformance.put(count, sentence);
		}
		List<String> best = sentencePerformance.getOrderedValues(5);
		String together = "";
		for(String s : best)
			together += s + " ";
		
		
		return together;
	}
	
	static String searchTargetSite(List<String> nouns){
		String relevantInfo = "";
		for(String s : nouns){
			String site = targetSite + s;
			File f = new File("./wiki/"+s+"-target.wiki");
			boolean exists = f.exists();
			
			String siteInfo = getSiteInfo(site, f);
			if(siteInfo.contains("does not have an article with this exact name") || siteInfo.length() < 5){
				continue;
			}

			HtmlCleaner clean = new HtmlCleaner();
			TagNode node = clean.clean(siteInfo);
			
			TagNode content = node.getElementsByAttValue("id", "mw-content-text", true, false)[0];
			
			String info = content.getText().toString().replaceAll("\n", " ").replaceAll("( )+", " ");
			relevantInfo += info + " ";
			try {
				if(!exists && !Util.errorLog.contains(f.getName())){
					System.err.println("Throttling " + THROTTLE + " ms");
					Thread.sleep(THROTTLE);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return relevantInfo;
	}
	
	private static String getSiteInfo(String url, File f){
		try{
			if(f.exists()){
				Scanner reader = new Scanner(f);
				String site = "";
				while(reader.hasNext()){
					site += reader.nextLine() + " ";
				}
				System.err.println("Read from cache: /wiki/" + f.getName());
				return site;
			}
			else if(Util.errorLog.contains(f.getName())){
				System.err.println("Invalid URL...continuing");
				return "";
			}
			
			URL site = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));
			String allData = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				allData += inputLine + "\n";
			}
			in.close();
			
			PrintWriter pw = new PrintWriter(f);
			pw.println(allData);
			pw.flush();
			pw.close();
			System.err.println("Wrote local: /wiki/" + f.getName());
			return allData;
		}
		catch(Exception e){
			System.out.println("Failed to parse: " + url);
			System.err.println("Appending to error log");
			Util.appendSiteError(f.getName() + "\n");
			e.printStackTrace();
			return "";
		}
	}
	
	static HashMap<String, Double> newWikiSearch(List<String> important, String question){
		HashMap<String, Double> mappings = new HashMap<String, Double>();
		String[] brokenDown = question.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
		
		//Take the nouns and verbs and rank them very highly so they show up later
		for(String s : important){
			String[] split = s.split("_");
			for(String word : split){
				word = word.toLowerCase();
				mappings.put(word, 100.0);
			}
		}
		
		//Break up the question into non useless words so that we can look through each wiki looking for them
		HashSet<String> qWords = new HashSet<String>();
		String[] broken = question.split(" ");
		for(String s : broken){
			if(!Util.stopwords.contains(s))
				qWords.add(s.toLowerCase());
		}
		
		//Takes the nouns and uses the wikisearch to find the three best articles that the search found
		List<String> bestSites = searchBroadSites(important);
		RankMap<Double, String> sites = new RankMap<Double, String>(Collections.reverseOrder());
		
		//Loop through each article and find the most relevant parts
		for(String s : bestSites){
			System.out.println("Going to this page: " + s);
			
			String[] header = s.toLowerCase().split("_");
			for(String head : header){
				String toAdd = head.replaceAll("[^a-z0-9]", "");
				System.out.println("Bumping up: " + toAdd);
				if(!mappings.containsKey(toAdd)){
					mappings.put(toAdd, 0.0);
				}
				int ins = 75;
				if(qWords.contains(toAdd)){
					System.out.println("Not that high");
					ins = 25;
				}
				mappings.put(toAdd, mappings.get(toAdd) + ins);
			}
			
			List<String> n = new LinkedList<String>();
			n.add(s);
			//Parse the given site
			String site = searchTargetSite(n);

			//Split the site into sentences
			String[] sentences = site.split("[.?!]");
			RankMap<Integer, String> sentencePerformance = new RankMap<Integer, String>(Collections.reverseOrder());
			
			//Group the article into pairings of three sentences each so that we have some context
			for(int i = 0; i < sentences.length; i+=3){
				int range = Math.min(i+2, sentences.length-1);
				String sentence = "";
				for(int ind = i; ind <= range; ind++){
					sentence += sentences[ind];
				}
				
				//Split the sentence into words and see how many times the non-useless words occur from the question
				String[] words = sentence.split(" ");
				int count = 0;
				for(String word : words){
					if(qWords.contains(word)){
						count++;
					}
				}
				
				//Put this in our map so that we can rank it later
				sentencePerformance.put(count, sentence);
			}
			
			//Take out the three best groupings of sentences
			List<String> best = sentencePerformance.getOrderedValues(3);
			String together = "";
			double total = 0.0;
			
			//Mash the three best groupings into a single sentence and tally how similar it is
			for(String str : best){
				together += str + " ";
				total += sentencePerformance.getValue(str);
			}
			
			//We rank articles based on how similar they are, so that when we go through later, we weight these higher
			sites.put(total, together);
		}
		
		
		//Loop through the best from each article and give them some weight in our mapping
		List<String> best = sites.getOrderedValues();
		int weight = (best.size() * 2)+1;
		for(String s : best){
			String[] cur = s.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
			for(String word : cur){
				if(Util.stopwords.contains(word))
					continue;
				if(!mappings.containsKey(word))
					mappings.put(word, 0.0);
				mappings.put(word, mappings.get(word) + weight);
			}
			weight -= 2;
		}
		
		return mappings;
	}
	
	static HashMap<String, Double> wikiSearch(List<String> important, String question){
		HashMap<String, Double> mappings = new HashMap<String, Double>();
		HashSet<String> qWords = new HashSet<String>();
		String[] brokenDown = question.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
		for(String s : brokenDown){
			if(!Util.stopwords.contains(s))
				mappings.put(s, 50.0);
		}
		
		String s = searchAllSites(important);
		String[] ranked = s.split("[.]{3}");
		int initWeight = ((ranked.length - 1) * 2) + 1;
		for(int i = 0; i < ranked.length; i++){
			String[] cur = ranked[i].toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
			for(String word : cur){
				if(Util.stopwords.contains(word))
					continue;
				if(!mappings.containsKey(word))
					mappings.put(word, 0.0);
				mappings.put(word, mappings.get(word) + initWeight);
			}
			initWeight -= 2;
		}
		
		s = searchTargetSite(important, question).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
		String[] allWords = s.split(" ");
		for(String word : allWords){
			if(Util.stopwords.contains(word))
				continue;
			
			if(!mappings.containsKey(word))
				mappings.put(word, 0.0);
			mappings.put(word, mappings.get(word) + 2);
		}
		return mappings;
	}
	
	public static void main(String [] args){
		ArrayList<String> arr = new ArrayList<String>();
		System.out.println(Util.stopwords.contains(" "));
		arr.add("golden");
		arr.add("gate");
		arr.add("bridge");
		arr.add("type");
		HashMap<String, Double> map = WikiParser.newWikiSearch(arr, "What type of bridge is the golden gate bridge?");
		RankMap<Double, String> r = new RankMap<Double, String>(map, Collections.reverseOrder());
		List<String> ordered = r.getOrderedValues();
		for(String s : ordered){
			System.out.println(s + " - " + map.get(s));
		}
	}

}
