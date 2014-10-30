package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * @author mona
 *
 */
public class GoodTerms {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public TreeMap<String, Float> getTermsScoresPair() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
				
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		IndexReader ir = reader.getIndexReader();		
					
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
			System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			System.out.println("=========================================");
			/*int docid = reader.getDocId("UN-EP-0663270", PatentDocument.FileName);
			ir.getTermVector(docid, field) getTermVectors(b);*/
			
			EvaluateResults er = new EvaluateResults();
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			HashMap<String, Float> /*TFreqs*/ termsscores = new HashMap<>();
			tps.add(qUcid);
//			System.out.println(tps);
//			int tpsize = tps.size()+1;
			
/*--------------------------------- Query Words -------------------------------*/
//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
			System.out.println(query_terms.size() + "\t" + query_terms);
/*-----------------------------------------------------------------------------*/			
			
			
//			System.out.println("-----TPs----");
			for (String tp : tps) {
				/*System.out.println("---------");
				System.out.println(tp);*/
				HashMap<String, Integer> termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + tp);
				
				for(Entry<String, Integer> tfTP:termsfreqsTP.entrySet()){
					if(termsscores.containsKey(tfTP.getKey())){
						termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/tps.size());
					}else{
//						float test = (float)t.getValue()/tps.size();
//						System.out.println(test);
						termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/tps.size());
					}
				}
//				System.out.println(termsscores.size() + " " + termsscores);					
			}
			
			/*System.out.println();
			System.out.println("-----FNs----");*/
			for (String fp : fps) {
				/*System.out.println("---------");
				System.out.println(fp);*/
				HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + fp);
				
				for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
					if(termsscores.containsKey(t.getKey())){
						termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/fps.size());
					}else{						
						termsscores.put(t.getKey(), -(float)t.getValue()/fps.size());
					}
				}
//				System.out.println(TFreqs.size() + " " + TFreqs);
			}
//			System.out.println(termsscores.size() + " " + termsscores);
			ValueComparator bvc =  new ValueComparator(termsscores);
			/*TreeMap<String,Float> */termsscoressorted = new TreeMap<String,Float>(bvc);
			termsscoressorted.putAll(termsscores);
			int overlap = 0;
			int i = 0;
			for(Entry<String, Float> scoresorted:termsscoressorted.entrySet()){
				i++;
				if(i<=100){
					if(query_terms.contains(scoresorted.getKey())){
						overlap++;
					}
					System.out.println("["+ i +"]" + scoresorted.getKey()+"\t"+scoresorted.getValue());
				}
			}
			
//			System.out.println(termsscoressorted.size() + " " + termsscoressorted);
//			int i = 0;
//			for(Entry<String, Float> scoresorted:termsscoressorted.entrySet()){
//				i++;
////				if(i<=100){
//					System.out.println("["+ i +"]" + scoresorted.getKey()+"\t"+scoresorted.getValue());
////				}
//			}
			
		}
		return termsscoressorted;			
	}
	
	public HashMap<String, Float> getUsefulTermsScores(String status) throws IOException, ParseException{
		GoodTerms gt = new GoodTerms();
		TreeMap<String, Float> termsscores_sorted = gt.getTermsScoresPair();
		HashMap<String, Float> useful_terms = new HashMap<>();
		HashMap<String, Float> non_useful_terms = new HashMap<>();
		for(Entry<String, Float> scoresorted:termsscores_sorted.entrySet()){
			if(scoresorted.getValue()>0){				
				useful_terms.put( scoresorted.getKey(), scoresorted.getValue());
			}else{
				non_useful_terms.put( scoresorted.getKey(), scoresorted.getValue());
			}
		}
		if(status.equals("useful")){
			return useful_terms;	
		}
		if(status.equals("non-useful")){
			return non_useful_terms;	
		}
			return useful_terms;			
	}
	
	public TreeMap<String, Float> getTopnTermsScores(int n) throws IOException, ParseException{		
		GoodTerms gt = new GoodTerms();
		TreeMap<String, Float> termsscores_sorted = gt.getTermsScoresPair();
		TreeMap<String, Float> topn_terms = new TreeMap<String, Float>();
		int i = 0;
		for(Entry<String, Float> scoresorted:termsscores_sorted.entrySet()){
			i++;
			if(i<=n){
				System.out.println("["+ i +"] " + scoresorted.getKey()+"\t"+scoresorted.getValue());
				topn_terms.put(scoresorted.getKey(), scoresorted.getValue());
			}else {
				break;
			}
		}
		
		return topn_terms;		
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		GoodTerms gt = new GoodTerms();
		
		/*------------------ Test: get all terms_score pairs from qrel file --------------------*/
		TreeMap<String, Float> termsscores_sorted = gt.getTermsScoresPair();
		System.out.println(termsscores_sorted.size() + " " + termsscores_sorted);
		int i = 0;
		for(Entry<String, Float> scoresorted:termsscores_sorted.entrySet()){
			i++;
//			if(i<=100){
				System.out.println("["+ i +"]" + scoresorted.getKey()+"\t"+scoresorted.getValue());
//			}
		}
		/*--------------------------------------------------------------------------------------------*/
		
		/*---------------------- Test: get useful terms_score pairs from qrel file (score>0) ----------------------*/
//		HashMap<String, Float> useful_termsscores_sorted = gt.getUsefulTermsScores("useful");
//		HashMap<String, Float> non_useful_termsscores_sorted = gt.getUsefulTermsScores("non-useful");
//		int j =0;
//		for(Entry<String, Float> scoresorted:useful_termsscores_sorted.entrySet()){
//			j++;
//			System.out.println("["+ j +"]" + scoresorted.getKey()+"\t"+scoresorted.getValue());
//		}
//		System.out.println("========================================");
//		int k =0;
//		for(Entry<String, Float> scoresorted:non_useful_termsscores_sorted.entrySet()){
//			k++;
//			System.out.println("["+ k +"]" + scoresorted.getKey()+"\t"+scoresorted.getValue());
//		}
//		
		/*--------------------------------------------------------------------------------------------*/
		
		/*---------------------- Test: get top-n(100) terms_score pairs from qrel file (score>0) ----------------------*/
//		gt.getTopnTermsScores(100);
		/*--------------------------------------------------------------------------------------------*/
		
		
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

