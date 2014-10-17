package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;

public class PositiveTermsOverlap {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	
	public TreeMap<String, Float> getTermsScoresPair(String queryid, String qUcid) throws IOException, ParseException{
		
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		IndexReader ir = reader.getIndexReader();
		
		EvaluateResults er = new EvaluateResults();
		ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
		int fpsize = fps.size();
		HashMap<String, Float> termsscores = new HashMap<>();
/*-------------------------- Uncomment if you want to add query terms to the score -------------------*/
//		tps.add(qUcid);
//		System.out.println(tps);
/*----------------------------------------------------------------------------------------------------*/		
		int tpsize = tps.size();
		
		for (String tp : tps) {
			/*System.out.println("---------");
			System.out.println(tp);*/
			HashMap<String, Integer> termsfreqsTP =new HashMap<String, Integer>();
			if(tp.length()<11){
			termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + tp);
			}else{
				termsfreqsTP = reader.gettermfreqpairAllsecs(tp);
			}
			
			for(Entry<String, Integer> tfTP:termsfreqsTP.entrySet()){
				if(termsscores.containsKey(tfTP.getKey())){
					termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/tps.size());
				}else{
//					float test = (float)t.getValue()/tpsize;
//					System.out.println(test);
					termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/tpsize);
				}
			}
//			System.out.println(termsscores.size() + " " + tpsize + " " + termsscores);					
		}
		
		for (String fp : fps) {
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
	}

	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException, ParseException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/termoverlp-FNs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			System.out.println("=========================================");

			TreeMap<String, Float> tspair = olap.getTermsScoresPair(queryid, qUcid);
			System.out.println(tspair.size() + "\t" + tspair);
			
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();
			
			/*--------------------------------- Query Words -------------------------------*/			
//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
//			System.out.println(query_terms.size() + "\t" + query_terms);
			
			
			/*if(n_enfns != 0){

				System.out.println("---------------------------------------------------------");
				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "overlap/|Q|");
				System.out.println("---------------------------------------------------------");
				for (String doc : enfns) { 
					
				}
			}*/

		}
	}
}
