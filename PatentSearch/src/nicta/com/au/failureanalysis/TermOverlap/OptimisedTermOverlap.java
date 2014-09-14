package nicta.com.au.failureanalysis.TermOverlap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class OptimisedTermOverlap {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	static String field = PatentDocument.Description;

	public void FNsTermOverlapAllQueries(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FNs-Optimized.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);

			float sum =0;
			float avg = 0;
			float overlapratio = 0;
			int querydocintersection;
			if(n_enfns != 0){

				for (String doc : enfns) { 
					querydocintersection = 0;
					//							System.out.println(doc);					
					ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);
					//						boolean bool = terms.contains("mona"/*"work"*/);

					for(Entry<String, Integer> t : qterms.entrySet()){
						boolean exists = terms.contains(t.getKey());						
						if(exists){
							querydocintersection++;	
						}
					}


					int querysize = qterms.size();
					overlapratio = (float)querydocintersection/querysize;
					sum = sum + overlapratio;

					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
				}

				avg = (float)sum/n_enfns;
				System.out.println("---------------------------------------------------------");
				System.out.println(queryid + "\t" + avg);
				System.out.println("---------------------------------------------------------");
				ps.println(queryid + "\t" + avg);
				
				
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}

	}
	
//	public void TPsTermOverlapAllQueries(CollectionReader reader) throws IOException{
//
//		/*--------------------------- Write in output file. -Mona ------------------------*/
//		String outputfile = "./output/TermOverlap/termoverlp-TPs-Optimized.txt";
//
//		FileOutputStream out = new FileOutputStream(outputfile);
//		PrintStream ps = new PrintStream(out);
//		/*-------------------------------------------------------------------------------*/
//
//		EvaluateResults er = new EvaluateResults();
//		//		AnalyseFNs afn = new AnalyseFNs();
//
//		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
//		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
//
//			String queryid = topic.getKey();
//			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
//
//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
//			//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
//			int n_tps = tps.size();
//
//			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
//			Map<String, Integer> qterms = query.getSectionTerms(field);
//
//			float sum =0;
//			float avg = 0;
//			float overlapratio = 0;
//			int querydocintersection;
//			if(n_tps != 0){
//
//				for (String doc : tps) { 
//					querydocintersection = 0;
//					//							System.out.println(doc);					
//					ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);
//					//						boolean bool = terms.contains("mona"/*"work"*/);
//
//					for(Entry<String, Integer> t : qterms.entrySet()){
//						boolean exists = terms.contains(t.getKey());
//						/*boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
//							//			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");
//
//							if(x){
//								//						m++;
//								//						System.out.println(m);
//								querydocintersection++;					
//							}	*/
//						if(exists){
//							querydocintersection++;	
//						}		
//					}
//
//
//					int querysize = qterms.size();
//					overlapratio = (float)querydocintersection/querysize;
//					sum = sum + overlapratio;
//
//					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
//				}
//
//				avg = (float)sum/n_tps;
//				System.out.println("---------------------------------------------------------");
//				System.out.println(queryid + "\t" + avg);
//				ps.println(queryid + "\t" + avg);
//				//			System.out.println("Avg. Term Overlap between query and FN patents =" + avg);
//				/*}else{
//					System.out.println(queryid+"\t" + "big FNs");
//					ps.println(queryid+"\t"+ "big FNs");
//				}*/
//
//			}else{
//				System.out.println(queryid+"\t" + "No TP for this query");
//				ps.println(queryid+"\t"+ "No TP for this query");
//
//			}
//
//			//    		System.out.println(queryid + "\t" + queryfile);
//		}
//
//	}


	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir);
		OptimisedTermOverlap opt = new OptimisedTermOverlap();
		
		opt.FNsTermOverlapAllQueries(reader);
		}
}
