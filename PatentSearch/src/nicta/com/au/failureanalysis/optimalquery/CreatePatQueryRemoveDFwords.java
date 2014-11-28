package nicta.com.au.failureanalysis.optimalquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nicta.com.au.failureanalysis.goodterms.GetDocFrequency;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class CreatePatQueryRemoveDFwords {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public String GeneratePatQueryRemoveDFs(String queryid, String qUcid, double tau) throws IOException, ParseException{
		
		CollectionReader reader = new CollectionReader(indexDir); 
		
		ArrayList<String> qterms = new ArrayList<String>();
		
		HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid);
		Set<String> qts = query_terms.keySet();
		for(String qterm : query_terms.keySet()){
			qterms.add(qterm);
		}
		
		/*System.out.println
		(queryid + " " + query_terms.size() + " " + query_terms);		
		System.out.println();*/
		
		GetDocFrequency df = new GetDocFrequency();
		HashMap<String, Float> df_tspairs = df.getTermDocFreqScorePair(queryid);
		/*System.out.println(queryid + " " + df_tspairs.size() + " " + df_tspairs);
		System.out.println();*/
		
		int size = 0;
		String new_query = "";
		
		for(String qt : query_terms.keySet()){
			if(df_tspairs.keySet().contains(qt)){
				if(df_tspairs.get(qt) > tau){
					if (!Functions.isNumeric(qt) && !Functions.isSpecialCahr(qt)) {
						size++;						
						qterms.remove(qt);
					}				
				}
			}
		}
		int k = 0;
		for(String newterm : qterms){
			if (!Functions.isNumeric(newterm) && !Functions.isSpecialCahr(newterm)) {
				k++;
//							new_query += term + "^" + score + " ";		
				new_query += newterm + "^" + 1 + " ";
			}	
		}		
		/*System.out.println(queryid + " " + qterms.size() + " " + qterms);				
		System.out.println(" (" + size + ")   " + k + " " + new_query);		
		System.out.println();*/
		return new_query;		
	}
	
	

	public static void main(String[] args) throws IOException, ParseException {
		double tau = 1/*Integer.parseInt(args[0])*/;
		CreatePatQueryRemoveDFwords c = new CreatePatQueryRemoveDFwords();
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  /*PAC_topics-omit-PAC-1094.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			String newquery = c.GeneratePatQueryRemoveDFs(queryid, qUcid, tau);
			System.out.println(queryid + " --> " + newquery);
		}
	}
}
