package nicta.com.au.failureanalysis.SectionBasedAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;

public class RFScoreSections {
static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	
	public TreeMap<String, Float> getSectionRFTermscoresPair(String queryid, String field) throws IOException, ParseException{
		
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		IndexReader ir = reader.getIndexReader();
		
		EvaluateResults er = new EvaluateResults();
		ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
		int fpsize = fps.size();
		HashMap<String, Float> termsscores = new HashMap<>();
	
		int tpsize = tps.size();
		/*System.out.println();
		System.out.println(queryid);*/
		for (String tp : tps) {
			/*System.out.println("---------");
			System.out.println(tp);*/
			HashMap<String, Integer> termsfreqsTP =new HashMap<String, Integer>();
			
			/*HashMap<String, Integer>*/ termsfreqsTP = reader.gettermfreqpair("UN-" + tp, field);
//			System.out.println(termsfreqsTP.size() + " " + termsfreqsTP);
			
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
		}
		
		for (String fp : fps) {
			HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpair("UN-" + fp, field);

			for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
				if(termsscores.containsKey(t.getKey())){
					termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/fpsize);
				}else{						
					termsscores.put(t.getKey(), -(float)t.getValue()/fpsize);
				}
			}
		}
		
		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc = new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);
		
		return termsscoressorted;
	}
	
	public String createRFSectionbasedQuery(String queryid, float tau, String field) throws IOException, ParseException{
		RFScoreSections s = new RFScoreSections();
		TreeMap<String, Float> tspairs = s.getSectionRFTermscoresPair(queryid, field);
		

//		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		
//		Map<String, Float> tspairs = olap.getTermsScoresPair(queryid);
		//				HashMap<String, Float> tshashes = new HashMap<>();
		//				tshashes.putAll(tspairs);

//		System.out.println(/*tspairs.size() + "\t" + */tspairs );
		String optimal_query = "";
		int i = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
				i++;
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
//					optimal_query += tskey + "^" + tsvalue + " ";
					optimal_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		
//		System.out.println(optimal_query);
		return optimal_query;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException {		
		CollectionReader reader = new CollectionReader(indexDir); 
		RFScoreSections sec = new RFScoreSections();
		
		
//		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		int tau = 0;
		String titlefield = PatentDocument.Title;
		String absfield = PatentDocument.Abstract;
		String descfield = PatentDocument.Description;
		String claimsfield = PatentDocument.Claims;
	
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
			TreeMap<String, Float> a = sec.getSectionRFTermscoresPair(queryid, claimsfield);
			System.out.println(a.size() + " " + a);
			String b = sec.createRFSectionbasedQuery(queryid, tau, claimsfield);
			System.out.println(b);

		}
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

