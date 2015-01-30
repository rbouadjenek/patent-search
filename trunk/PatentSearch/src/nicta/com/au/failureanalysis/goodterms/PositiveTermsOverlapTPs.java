package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.analysis.test;
import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class PositiveTermsOverlapTPs {
	static String indexDir =/* "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*//*"data/DocPLusQueryINDEX"*/"data/QINDEX";

	public static void main(String[] args) throws IOException, ParseException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/test-termoverlp-TPs2.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");

			int tpssize = tps.size();
			if(tpssize != 0){
				/*System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			System.out.println("=========================================");*/

				Map<String, Float> tspairs = olap.getTermsScoresPair(queryid);
				HashMap<String, Float> tshashes = new HashMap<>();
				tshashes.putAll(tspairs);

				//			System.out.println(tspairs.size() + "\t" + tspairs);
				//			System.out.println(tshashes.size() + "\t" + tshashes.get("piston")+ "\t" + tshashes);

				/*ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();*/

				/*--------------------------------- Query Words -------------------------------*/			
				//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
				HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
				int querysize = query_terms.size();
				//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
				//			System.out.println(query_terms.size() + "\t" + query_terms);


				int usefuloverlap;
				int nonusefuloverlap;
				int overlap;
				float usefulsum=0;
				float nonusefulsum=0;
				float sum=0;
				float usefulavg=0;
				float nonusefulavg=0;
				float avg=0;
				for (String doc : tps) { 
					usefuloverlap = 0;
					nonusefuloverlap = 0;
					overlap = 0;
					Set<String> docterms = reader.gettermfreqpairAllsecs("UN-" + doc).keySet();

					for( String qt : query_terms){

						boolean existsuseful = (docterms.contains(qt) && tshashes.get(qt)>0);						
						if(existsuseful){
							usefuloverlap++;	
						}					
						boolean existsnonuseful = (docterms.contains(qt) && tshashes.get(qt)<=0);						
						if(existsnonuseful){
							nonusefuloverlap++;	
						}
						boolean exists = (docterms.contains(qt));						
						if(exists){
							overlap++;	
						}
					}
					//				System.out.println((float)usefuloverlap/querysize+"\t"+(float)nonusefuloverlap/querysize+"\t"+(float)overlap/querysize);
					usefulsum = usefulsum + (float)usefuloverlap/querysize;
					nonusefulsum = nonusefulsum + (float)nonusefuloverlap/querysize;
					sum = sum + (float)overlap/querysize;

				}

				usefulavg = (float)usefulsum/tpssize;
				nonusefulavg = (float)nonusefulsum/tpssize;
				avg = (float)sum/tpssize;
				System.out.println(queryName+"\t"+usefulavg+"\t"+nonusefulavg+"\t"+avg);
				ps.println(queryName+"\t"+usefulavg+"\t"+nonusefulavg+"\t"+avg);
			}
		}/*else{
			System.out.println(queryName+"\t" + "No TP for this query");
			ps.println(queryName+"\t"+ "No TP for this query");

		}*/
	}

}
