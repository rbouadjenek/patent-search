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

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;

public class OverlapWithQuery {

	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public /*TreeMap<String, Float>*/void getTopnTermsOverlapWithQuery() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		IndexReader ir = reader.getIndexReader();		

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			/*System.out.println("=========================================");
			System.out.println(queryName);
			System.out.println("=========================================");*/
			/*int docid = reader.getDocId("UN-EP-0663270", PatentDocument.FileName);
			ir.getTermVector(docid, field) getTermVectors(b);*/

			EvaluateResults er = new EvaluateResults();
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			HashMap<String, Float> /*TFreqs*/ termsscores = new HashMap<>();


			/*--------------------------------- Query Words -------------------------------*/
			//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
//			System.out.println(query_terms.size() + "\t" + query_terms);
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
			termsscoressorted = new TreeMap<String,Float>(bvc);			
			termsscoressorted.putAll(termsscores);
//			System.out.println(termsscoressorted.size() + "\t" + termsscoressorted.keySet());
			int overlap = 0;
			int i = 0;
			for(Entry<String, Float> scoresorted:termsscoressorted.entrySet()){
				i++;
				if(i<=100){
					if(query_terms.contains(scoresorted.getKey())){
						overlap++;
					}					
				}
			}	
			System.out.println(queryName + "\t"+overlap);
			ps.println(queryName + "\t"+overlap);
		}

	}
	
	
	public static void main(String[] args) throws IOException, ParseException {
		OverlapWithQuery olap = new OverlapWithQuery();
		olap.getTopnTermsOverlapWithQuery();
		
	}
}
