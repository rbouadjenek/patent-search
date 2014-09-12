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
	
	public void TPsTermOverlapAllQueries(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-TPs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_tps = tps.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);

			boolean exists = false;
			float sum =0;
			float avg = 0;
			float overlapratio = 0;
			int querydocintersection;
			if(n_tps != 0){

					for (String doc : tps) { 
						querydocintersection = 0;
//							System.out.println(doc);	
						
						ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);
						for(Entry<String, Integer> t : qterms.entrySet()){
							if(terms != null){
							exists = terms.contains(t.getKey());}
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
					System.out.println("---------------------------------------------------------");
					ps.println(queryid + "\t" + avg);
//					}
					
			}else{
				System.out.println(queryid+"\t" + "No TP for this query");
				ps.println(queryid+"\t"+ "No TP for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}
	}
	
	public void FPsTermOverlapAllQueries(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FPs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			int n_fps = fps.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);

			boolean exists = false;
			float sum =0;
			float avg = 0;
			float overlapratio = 0;
			int querydocintersection;
			if(n_fps != 0){

				for (String doc : fps) { 
					querydocintersection = 0;			
					ArrayList<String> terms = reader.getDocTerms("UN-"+doc, field);

					for(Entry<String, Integer> t : qterms.entrySet()){
						if(terms != null){
							exists = terms.contains(t.getKey());}
						if(exists){
							querydocintersection++;	
						}								
					}						

					int querysize = qterms.size();
					overlapratio = (float)querydocintersection/querysize;
					sum = sum + overlapratio;

					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
				}

				avg = (float)sum/n_fps;
				System.out.println("---------------------------------------------------------");
				System.out.println(queryid + "\t" + avg);
				ps.println(queryid + "\t" + avg);
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

		
//		ctotps.TPsTermOverlapAllQueries(reader);
		ctotps.FPsTermOverlapAllQueries(reader);

	}

}
