package nicta.com.au.failureanalysis.optimalquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nicta.com.au.failureanalysis.goodterms.GetDocFrequency;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class CreateQueryRemoveDFwords {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public String GeneratePatQueryRemoveDFs(String queryid, String qUcid, double tau) throws IOException, ParseException{

		CollectionReader reader = new CollectionReader(indexDir); 

		ArrayList<String> qterms = new ArrayList<String>();

		HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid);
		Set<String> qts = query_terms.keySet();
		for(String qterm : query_terms.keySet()){
			qterms.add(qterm);
		}

		/*System.out.println(queryid);	
		System.out.println("query term/freq: " + query_terms.size() + " " + query_terms);		
		System.out.println();*/

		GetDocFrequency df = new GetDocFrequency();
		HashMap<String, Float> df_tspairs = df.getTermDocFreqScorePair(queryid);
		/*System.out.println(queryid);
		System.out.println("Top-100 termfreq: " + df_tspairs.size() + " " + df_tspairs);
		System.out.println();*/

		int size = 0;
		String new_query = "";

		for(Entry<String, Integer> qtpair : query_terms.entrySet()){
			String qt = qtpair.getKey();
			Integer qfreq = qtpair.getValue();
			if(df_tspairs.keySet().contains(qt)){
				//								if(df_tspairs.get(qt) > tau){
				if(df_tspairs.get(qt) > 1 && qfreq <= 10){ //Discarding criteria: df_tspairs.get(qt) > tau && qfreq <= 8
					//					if (!Functions.isNumeric(qt) && !Functions.isSpecialCahr(qt)) {
					size++;						
					qterms.remove(qt);
					//					}				
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
		/*System.out.println(queryid);
		System.out.println(" new queryterms: " + qterms.size() + " " + qterms);	
		System.out.println();
		System.out.println(" (" + size + ")    " + k + " " + new_query);		
		System.out.println();*/

		return new_query;		
	}

	public String GeneratePatQueryRemDFs3Conditions(String queryid, String qUcid, String queryfile, double tau, double delta) throws IOException, ParseException{
		CollectionReader reader = new CollectionReader(indexDir); 
		ArrayList<String> qterms = new ArrayList<String>();
		
		HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid);
//		Set<String> qts = query_terms.keySet();
		for(String qterm : query_terms.keySet()){
			qterms.add(qterm);
		}

		/*System.out.println(queryid);	
		System.out.println("query term/freq: " + query_terms.size() + " " + query_terms);		
		System.out.println();*/

		GetDocFrequency df = new GetDocFrequency();
		HashMap<String, Float> df_tspairs = df.getTermDocFreqScorePair(queryid);
		/*System.out.println(queryid);
		System.out.println("Top-100 termfreq: " + df_tspairs.size() + " " + df_tspairs);
		System.out.println();*/
		
		IpcDefinition def = new IpcDefinition();
		ArrayList<String> ipcdeflists = def.GetIpcDefWords(queryfile);
		/*System.out.println(queryid);
		System.out.println("IPC def: " + ipcdeflists.size() + " " + ipcdeflists);
		System.out.println();*/

		int size = 0;
		String new_query = "";

		for(Entry<String, Integer> qtpair : query_terms.entrySet()){
			String qt = qtpair.getKey();
			Integer qtf = qtpair.getValue();
			if(df_tspairs.keySet().contains(qt)){
				//								if(df_tspairs.get(qt) > tau){
				if((df_tspairs.get(qt) > tau) && (qtf <= delta || ipcdeflists.contains(qt))){ //Discarding criteria: df_tspairs.get(qt) > tau && qfreq <= 8
			
					size++;						
					qterms.remove(qt);					
				}
			}
		}
		int k = 0;
		for(String newterm : qterms){
			if (!Functions.isNumeric(newterm) && !Functions.isSpecialCahr(newterm)) {
				k++;
				new_query += newterm + "^" + query_terms.get(newterm) + " ";		
//				new_query += newterm + "^" + 1 + " ";
			}	
		}	
		/*System.out.println(queryid);
		System.out.println(" new queryterms: " + qterms.size() + " " + qterms);	
		System.out.println();
		System.out.println(" (" + size + ")    " + k + " " + new_query);		
		System.out.println();*/

		return new_query;		
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		double tau = 0/*Integer.parseInt(args[0])*/;
		double delta = 5;
		CreateQueryRemoveDFwords c = new CreateQueryRemoveDFwords();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  /*PAC_topics-omit-PAC-1094.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
//			String newquery = c.GeneratePatQueryRemoveDFs(queryid, qUcid, tau);
			String newquery = c.GeneratePatQueryRemDFs3Conditions(queryid, qUcid, queryfile, tau, delta);
//			System.out.println(newquery);
			//			System.out.println(queryid + " --> " + newquery);
			System.out.println();
		}
	}
}
