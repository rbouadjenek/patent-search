package nicta.com.au.failureanalysis.QuerywithFirstRankTPs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.evaluate.QueryAndPatents;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class Get1stTPRank {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	TreeMap<String,Float> termsscoressorted = null;

	public void get1stTPRank(String queryid, int topk) throws IOException {
		int fpsize = 0 ;

		ArrayList<String> FPs = new ArrayList<>();
		String resultsfile = "output/results/results-lmdir-desc-100.txt";

		CollectionReader reader = new CollectionReader(indexDir); 
		TreeMap<String,Integer> tprankssorted = null;
		QueryAndPatents qps = new QueryAndPatents();
		HashMap<String, HashMap<String, Integer>> _querydocranks = qps.GetQueryPatentsRanks3(resultsfile);
		
//		System.out.println(_querydocranks);
		HashMap<String, ArrayList<String>> _retdocs = qps.GetQueryPatents(resultsfile);
		ArrayList<String> retrieveddocs = _retdocs.get(queryid);

		EvaluateResults er = new EvaluateResults();
		ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		HashMap<String, Integer> tp_ranks = new HashMap<>();

		HashMap<String, Integer> docranks = _querydocranks.get(queryid);
		
		for(String tp : tps){
			if(docranks.containsKey(tp)){
//				x++;
				Integer r = docranks.get(tp);
				
				tp_ranks.put(tp, r);
//				if(x==1)break;
			}
		}
//		System.out.println(queryid + " " + tp_ranks);

		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator1 bvc1 =  new ValueComparator1(tp_ranks);
		tprankssorted = new TreeMap<String, Integer>(bvc1);
		tprankssorted.putAll(tp_ranks);

//		System.out.println(queryid + " " + tprankssorted);	
		
		int x=0;
//		if(!tp_ranks.isEmpty()){
			for(Entry<String, Integer> tpr : tprankssorted.entrySet()){
				x++;
//				System.out.println(queryid + " " + tpr.getValue());
				System.out.println(tpr.getValue());
				//			if(tpr == null){System.out.println(0);}
				if(x==1) break;
			}
		/*}else{
			System.out.println(0);
		}*/

		int tpsize = topk;
		int b = 0;
		HashMap<String, Float> termsscores = new HashMap<>();
//		if(tprankssorted.keySet().size() != 0){
//			for (String tp : tprankssorted.keySet()) {
//				b++;
//				//			System.out.println(tp);
//				retrieveddocs.remove(tp);
//				FPs = retrieveddocs;
//
//				/*System.out.println("---------");
//			System.out.println(tp);*/
//				HashMap<String, Integer> termsfreqsTP = new HashMap<String, Integer>();
//				if(tp.length()<11){
//					termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + tp);
//					//			System.out.println(termsfreqsTP);
//				}else{
//					termsfreqsTP = reader.gettermfreqpairAllsecs(tp);
//				}
//
//				for(Entry<String, Integer> tfTP : termsfreqsTP.entrySet()){
//					if(termsscores.containsKey(tfTP.getKey())){
//						termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/tpsize);
//					}else{
//						//					float test = (float)t.getValue()/tpsize;
//						//					System.out.println(test);
//						termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/tpsize);
//					}
//				}
//				//			System.out.println(termsscores.size() + " " + tpsize + " " + termsscores);		
//				if(b == topk)break;
//			}
//		}else{
//			FPs = retrieveddocs;		
//		}

//		fpsize = FPs.size();
//		for (String fp : FPs) {
//			HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + fp);
//
//			for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
//				if(termsscores.containsKey(t.getKey())){
//					termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/fpsize);
//				}else{						
//					termsscores.put(t.getKey(), -(float)t.getValue()/fpsize);
//				}
//			}
//		}

		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);

		//		return termsscoressorted;
		//		System.out.println(FPs.size() + " " + FPs);
		//		return tprankssorted;
	}


	public static void main(String[] args) throws IOException {
		Get1stTPRank a = new Get1stTPRank();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml");  /*PAC_topics-omit-PAC-1094.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			a.get1stTPRank(queryid, 1);
		}

	}

}
