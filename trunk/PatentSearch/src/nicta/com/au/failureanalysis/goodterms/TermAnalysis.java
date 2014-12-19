package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import nicta.com.au.failureanalysis.GeneralExecuteTopic.GeneralParseQuery;
import nicta.com.au.failureanalysis.optimalquery.CreateOptimalQuery;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class TermAnalysis {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public void printQueryTerms() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/TermAnalysis/test.txt";
		
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		String path = "data/CLEF-IP-2010/PAC_test/topics/";
		TreeMap<String,Integer> qterms_sorted = null;

//		CreateOptimalQuery oq = new CreateOptimalQuery();
//		GeneralParseQuery pq = new GeneralParseQuery();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();

			CollectionReader reader = new CollectionReader(indexDir);
			/*--------------------------------- Query Words -------------------------------*/		
			HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid);

			/*--------------------------- Sort terms scores pair------------------------------------*/
			ValueComparator1 bvc1 =  new ValueComparator1(query_terms);
			qterms_sorted = new TreeMap<String,Integer>(bvc1);
			qterms_sorted.putAll(query_terms);

			System.out.println(queryid);
			ps.println(queryid);

			System.out.println(qterms_sorted.size() + " " + qterms_sorted);
			System.out.println();
			ps.println(qterms_sorted.size() + " " + qterms_sorted);
			ps.println();
		}
	}
	
	public void printOptQueryTerms() throws IOException, ParseException{
		
	}


	public static void main(String[] args) throws IOException, ParseException {
		TermAnalysis ta = new TermAnalysis();
		ta.printQueryTerms();		
		
		/*--------------------------- Write in output file. ------------------------*/
		
		String outputfile = "./output/TermAnalysis/optimal-queryterm_scores.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		
//		String path = "data/CLEF-IP-2010/PAC_test/topics/";
//		TreeMap<String,Integer> qterms_sorted = null;
//
//		CreateOptimalQuery oq = new CreateOptimalQuery();
//		GeneralParseQuery pq = new GeneralParseQuery();
//
//		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  
//		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
//			String qUcid = topic.getValue().getUcid();
//			String queryid = topic.getKey();
//			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
//			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
//
//			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
//			String ipcfilter = g.getIpc();

			
			/*----------------------------- optimal query --------------------------------*/
			/*String optquery = oq.generateOptimalQuery(queryid, 0);
			System.out.println(queryid);
			ps.println(queryid);

			System.out.println(qterms_sorted.size() + " " + qterms_sorted);
			System.out.println();
			ps.println(qterms_sorted.size() + " " + qterms_sorted);
			ps.println();*/
//		}
	}
}


/*-------------- This class is used to sort a hashmap --------------*/

class ValueComparator1 implements Comparator<String> {

	Map<String, Integer> base;
	public ValueComparator1(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		if (base.get(b) <= base.get(a)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}



