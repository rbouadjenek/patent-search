package nicta.com.au.failureanalysis.SectionBasedAnalysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.QueryAndPatents;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class PRFScoreSection {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	QueryAndPatents qps = new QueryAndPatents();
	
	public TreeMap<String, Float> getSectionPRFTermscoresPair(String queryid, String field) throws IOException, ParseException{
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		HashMap<String, Float> termsscores = new HashMap<>();

		HashMap<String, HashMap<String, String>> _docranks = qps
				.GetQueryRanksPatents2("output/results/results-lmdir-desc-100.txt");

		/*In PRF, we assume that top 5 docs are TP*/
		for(int i=1;i<=5;i++){
			String top5_patent = _docranks.get(queryid).get(Integer.toString(i));
			/*System.out.println(i + " " + top5_patent);*/

			HashMap<String, Integer> termsfreqsTP =new HashMap<String, Integer>();

			//termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + top5_patent);
			termsfreqsTP = reader.gettermfreqpair("UN-" + top5_patent, field);
			//			System.out.println(termsfreqsTP);

			for(Entry<String, Integer> tfTP:termsfreqsTP.entrySet()){

				if(termsscores.containsKey(tfTP.getKey())){
					termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/5);
				}else{
					//					float test = (float)t.getValue()/tpsize;
					//					System.out.println(test);
					termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/5);
				}
			}
			/*System.out.println(termsscores.size() + " " + termsscores);	*/
		}
//		System.out.println();

		/*In PRF, we assume that bottom 95 docs are FP*/
		for(int i=6;i<=100;i++){
			String top95_patents = _docranks.get(queryid).get(Integer.toString(i));
			/*System.out.println(i + " " + top95_patents);*/

			//HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + top95_patents);
			HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpair("UN-" + top95_patents, field);

			for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
				if(termsscores.containsKey(t.getKey())){
					termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/95);
				}else{						
					termsscores.put(t.getKey(), -(float)t.getValue()/95);
				}
			}
			/*System.out.println(termsscores.size() + " " + termsscores);	*/
		}
		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);

		return termsscoressorted;
	}
	
	public String createPRFSectionbasedQuery(String queryid, float tau, String field) throws IOException, ParseException{
		PRFScoreSection PRFs = new PRFScoreSection();
		TreeMap<String, Float> tspairs = PRFs.getSectionPRFTermscoresPair(queryid, field);

		String new_query = "";
		int i = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
				i++;
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
//					optimal_query += tskey + "^" + tsvalue + " ";
					new_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		
//		System.out.println(new_query);
		return new_query;
	}
	
	public static void main(String[] args) throws IOException, ParseException {		
		CollectionReader reader = new CollectionReader(indexDir); 
		PRFScoreSection PRFsec = new PRFScoreSection();

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
			
			System.out.println();
			System.out.println(queryid);
			TreeMap<String, Float> a = PRFsec.getSectionPRFTermscoresPair(queryid, claimsfield);
			System.out.println(a.size() + " " + a);
			String b = PRFsec.createPRFSectionbasedQuery(queryid, tau, claimsfield);
			System.out.println(b);

		}
	}

}
