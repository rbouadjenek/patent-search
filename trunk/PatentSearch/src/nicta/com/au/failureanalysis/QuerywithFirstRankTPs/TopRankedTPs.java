package nicta.com.au.failureanalysis.QuerywithFirstRankTPs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import nicta.com.au.failureanalysis.GeneralExecuteTopic.GeneralParseQuery;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.evaluate.QueryAndPatents;
import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.failureanalysis.optimalquery.CreateOptimalPatentQuery;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;


public class TopRankedTPs {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	TreeMap<String,Float> termsscoressorted = null;
	
	public TreeMap<String, Float> getRFtermscores(String queryid, int topk) throws IOException {
		int fpsize = 0 ;
		
		ArrayList<String> FPs = new ArrayList<>();
		String resultsfile = "output/results/results-lmdir-desc-100.txt";
              
		CollectionReader reader = new CollectionReader(indexDir); 
		TreeMap<String,Integer> tprankssorted = null;
		QueryAndPatents qps = new QueryAndPatents();
		HashMap<String, HashMap<String, Integer>> _querydocranks = qps.GetQueryPatentsRanks3(resultsfile);
		HashMap<String, ArrayList<String>> _retdocs = qps.GetQueryPatents(resultsfile);
		ArrayList<String> retrieveddocs = _retdocs.get(queryid);

		EvaluateResults er = new EvaluateResults();
		ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		HashMap<String, Integer> tp_ranks = new HashMap<>();
						
		HashMap<String, Integer> docranks = _querydocranks.get(queryid);
				
		for(String tp:tps){
			if(docranks.containsKey(tp)){
				Integer r = docranks.get(tp);
				tp_ranks.put(tp, r);
			}
		}

		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator1 bvc1 =  new ValueComparator1(tp_ranks);
		tprankssorted = new TreeMap<String, Integer>(bvc1);
		tprankssorted.putAll(tp_ranks);
		
//		System.out.println(tprankssorted);		

		int tpsize = topk;
		int b = 0;
		HashMap<String, Float> termsscores = new HashMap<>();
		if(tprankssorted.keySet().size() != 0){
		for (String tp : tprankssorted.keySet()) {
			b++;
//			System.out.println(tp);
			retrieveddocs.remove(tp);
			FPs = retrieveddocs;
			
			/*System.out.println("---------");
			System.out.println(tp);*/
			HashMap<String, Integer> termsfreqsTP = new HashMap<String, Integer>();
			if(tp.length()<11){
			termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + tp);
//			System.out.println(termsfreqsTP);
			}else{
				termsfreqsTP = reader.gettermfreqpairAllsecs(tp);
			}
			
			for(Entry<String, Integer> tfTP : termsfreqsTP.entrySet()){
				if(termsscores.containsKey(tfTP.getKey())){
					termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/tpsize);
				}else{
//					float test = (float)t.getValue()/tpsize;
//					System.out.println(test);
					termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/tpsize);
				}
			}
//			System.out.println(termsscores.size() + " " + tpsize + " " + termsscores);		
			if(b == topk)break;
		}
		}else{
			FPs = retrieveddocs;		
		}
		
		fpsize = FPs.size();
		for (String fp : FPs) {
			HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + fp);

			for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
				if(termsscores.containsKey(t.getKey())){
					termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/fpsize);
				}else{						
					termsscores.put(t.getKey(), -(float)t.getValue()/fpsize);
				}
			}
		}
		
		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);
		
		return termsscoressorted;
//		System.out.println(FPs.size() + " " + FPs);
//		return tprankssorted;
	}
	
	public String generateTopRFQuery(String queryid, float tau, int topk) throws IOException, ParseException{
		
//		PositiveTermsOverlap olap = new PositiveTermsOverlap();		
//		Map<String, Float> tspairs = olap.getTermsScoresPair(queryid);
		
		TopRankedTPs t = new TopRankedTPs();
		TreeMap<String, Float> tspairs = t.getRFtermscores(queryid, topk);

//		System.out.println(/*tspairs.size() + "\t" + */tspairs );
		String optimal_query = "";
//		int i = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
//				i++;
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
//					optimal_query += tskey + "^" + tsvalue + " ";
					optimal_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		
//		System.err.println(i);
//		if(i == 0){
//			System.err.println("There is no optimal query");
//			return null;
//		}
//		System.out.println(optimal_query);
		return optimal_query;
	}

	public static void main(String[] args) throws IOException, ParseException {
//		String queryid = /*"PAC-1006"*/"PAC-191";
		int topk = 3;
		
		TopRankedTPs t = new TopRankedTPs();
		
		String path = "data/CLEF-IP-2010/PAC_test/topics/";

		int tau = 1 /*Integer.parseInt(args[0])*/;
		CreateOptimalPatentQuery optpatentq = new CreateOptimalPatentQuery();
		GeneralParseQuery pq = new GeneralParseQuery();
	
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  /*PAC_topics-omit-PAC-1094.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
//			TreeMap<String, Float> RFtermscore_toptps = t.getRFtermscores(queryid, topk);
//			System.out.println(queryid + " " + RFtermscore_toptps.size() + " " + RFtermscore_toptps);
					
			
			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();
			
			String patentquery = t.generateTopRFQuery(queryid, tau, topk); 
			System.out.println(queryid + " (" + patentquery + ") ");
			Query q = pq.parse(patentquery, ipcfilter);
//			System.out.println(queryid + " (" + q + ") ");
			System.out.println();
		}
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
		if (base.get(b) >= base.get(a)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}

/*-------------- This class is used to sort a hashmap --------------*/

class ValueComparator implements Comparator<String> {

	Map<String, Float> base;
	public ValueComparator(Map<String, Float> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}

