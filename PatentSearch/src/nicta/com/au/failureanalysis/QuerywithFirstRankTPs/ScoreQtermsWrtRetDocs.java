package nicta.com.au.failureanalysis.QuerywithFirstRankTPs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.GeneralExecuteTopic.GeneralParseQuery;
import nicta.com.au.failureanalysis.evaluate.QueryAndPatents;
import nicta.com.au.failureanalysis.pseudorelevancefeedback.PRFTermsScores;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class ScoreQtermsWrtRetDocs {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public TreeMap<String, Float> RFscorePatQuery(String queryid, String qUcid, int bottomk) throws IOException, ParseException{
		CollectionReader reader = new CollectionReader(indexDir);
		TreeMap<String,Integer> docrankssorted = null;
		TreeMap<String,Float> termsscoressorted = null;

		String resultsfile = "output/results/results-lmdir-desc-100.txt";
		QueryAndPatents qps = new QueryAndPatents();
		HashMap<String, HashMap<String, Integer>> _querydocranks = qps.GetQueryPatentsRanks3(resultsfile);

		HashMap<String, Float> termsscores = new HashMap<>();
		/*--------------------------------- Query Words -------------------------------*/		
		HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid); 
		int querysize = query_terms.size();		
		/*-----------------------------------------------------------------------------*/
		for(Entry<String, Integer> qt : query_terms.entrySet()){
			String key = qt.getKey();
			float value = qt.getValue();
			termsscores.put(key, value);
		}
		HashMap<String, Integer> docranks = _querydocranks.get(queryid);
		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator1 bvc1 =  new ValueComparator1(docranks);
		docrankssorted = new TreeMap<String, Integer>(bvc1);
		docrankssorted.putAll(docranks);

		//		System.out.println(queryid);
		//		System.out.println(termsscores.size() + " " + termsscores);
		////		System.out.println(querysize + " " + query_terms);
		////		System.out.println(docrankssorted);
		//		System.out.println();

		float fpsize = 100 - bottomk;
		for(Entry<String, Integer> dr : docrankssorted.entrySet()){
			String doc = dr.getKey();
			Integer rank = dr.getValue(); 	
			if(rank > bottomk){ //tau=50, last 50 docs
				//				System.out.println(document + " " + rank);
				HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + doc);
				//				System.out.println(termsfreqsFP);
				for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
					if(termsscores.containsKey(t.getKey())){
						termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/fpsize);
					}else{						
						termsscores.put(t.getKey(), -(float)t.getValue()/fpsize);
					}
				}
				/*System.out.println(termsscores);
				System.out.println();*/
			}
		}

		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);

		return termsscoressorted;		
	}
	
	public String generateRFPatQuery(String queryid, String qUcid, float tau, int bottomk) throws IOException, ParseException{
		CollectionReader reader = new CollectionReader(indexDir);
		/*--------------------------------- Query Words -------------------------------*/		
		HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid); 

		ScoreQtermsWrtRetDocs s = new ScoreQtermsWrtRetDocs();
		
//		System.out.println(s.RFscorePatQuery(queryid, qUcid, 0));
		TreeMap<String, Float> tspairs = s.RFscorePatQuery(queryid, qUcid, 0);

		//		System.out.println(/*tspairs.size() + "\t" + */tspairs );
		String new_query = "";

		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
					//					new_query += tskey + "^" + tsvalue + " ";
					new_query += tskey + "^" + query_terms.get(tskey) + " ";
//					new_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		return new_query;
	}





	public static void main(String[] args) throws IOException, ParseException {
		String path = "data/CLEF-IP-2010/PAC_test/topics/";

		GeneralParseQuery pq = new GeneralParseQuery();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  /*PAC_topics-omit-PAC-1094.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();

			ScoreQtermsWrtRetDocs s = new ScoreQtermsWrtRetDocs();
			System.out.println();
			System.out.println(s.RFscorePatQuery(queryid, qUcid, 0));
			System.out.println();
		}
	}
}
