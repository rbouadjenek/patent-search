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

public class CalculateTermOverlapTPs {

	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	static String field = PatentDocument.Description;

	public void TPsTermOverlapPerQuery(CollectionReader reader) throws IOException{
		String _queryid = "PAC-1216"/*"PAC-1379"*//*"PAC-125"*/ /*"PAC-1261"*/;
		String _queryfile = "PAC-1216_EP-1749865-A1.xml"/*"PAC-1379_EP-1304229-A2.xml"*/;
		
		EvaluateResults er = new EvaluateResults();
		ArrayList<String> tps = er.evaluatePatents(_queryid, "TP");
		int n_tps = tps.size();

		QueryGneration query = new QueryGneration(querypath + _queryfile, 0, 1, 0, 0, 0, 0, true, true);
		Map<String, Integer> qterms = query.getSectionTerms(field);

		String doc = "EP-0687533"/*"EP-0603697"*/;
		//		for (String doc : enfns) { 
		ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);
		for(String docterm:terms){
			System.out.println(docterm);
		}
		int qtermsindoc = 0;
		int i=0;
		for(Entry<String, Integer> t : qterms.entrySet()){
			i++;
			/*boolean x=reader.getTFreq(field, t.getKey(), doc)>0;*/
			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" );
			/*if(reader.getTFreq(field, t.getKey(), doc)>0){
				qtermsindoc++;					
			}*/				

			//			}

		}
		int querysize = qterms.size();
		float overlapratio = (float)qtermsindoc/querysize;
		System.out.println(qtermsindoc + "\t" + querysize + "\t" + overlapratio);

	}
	public void TPsTermOverlapAllQueries(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-TPs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-testoverlap.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_tps = tps.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);

			float sum =0;
			float avg = 0;
			float overlapratio = 0;
			int querydocintersection;
			if(n_tps != 0){

//				if(n_tps < 11){
					for (String doc : tps) { 
						querydocintersection = 0;
//							System.out.println(doc);					
						ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);
//						boolean bool = terms.contains("mona"/*"work"*/);
						
						for(Entry<String, Integer> t : qterms.entrySet()){
							boolean exists = terms.contains(t.getKey());
							/*boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
							//			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");

							if(x){
								//						m++;
								//						System.out.println(m);
								querydocintersection++;					
							}	*/
							if(exists){
								querydocintersection++;	
							}							
							
							
						}
						

						int querysize = qterms.size();
						overlapratio = (float)querydocintersection/querysize;
						sum = sum + overlapratio;

						System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
					}

					avg = (float)sum/n_tps;
					System.out.println("---------------------------------------------------------");
					System.out.println(queryid + "\t" + avg);
					ps.println(queryid + "\t" + avg);
					//			System.out.println("Avg. Term Overlap between query and FN patents =" + avg);
				/*}else{
					System.out.println(queryid+"\t" + "big FNs");
					ps.println(queryid+"\t"+ "big FNs");
				}*/

			}else{
				System.out.println(queryid+"\t" + "No TP for this query");
				ps.println(queryid+"\t"+ "No TP for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}


	}


	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir); 

		CalculateTermOverlapTPs ctotps = new CalculateTermOverlapTPs();
//		ctotps.TPsTermOverlapPerQuery(reader);
		
		ctotps.TPsTermOverlapAllQueries(reader);

	}

}
