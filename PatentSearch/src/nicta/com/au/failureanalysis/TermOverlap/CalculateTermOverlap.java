package nicta.com.au.failureanalysis.TermOverlap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

/**
 * @author mona
 *
 * This calculates the average term overlap between the query and its false negative patents.
 * Just a reminder: we consider FN patents which are complete and the original language is in English. 
 * In other words, we ignore FN patents due to missing part or being in another language. 
 */
public class CalculateTermOverlap {
	
	

	static String path = "data/CLEF-IP-2010/PAC_test/topics/";
	static String _queryid = "PAC-1216"/*"PAC-1379"*/;
	static String _queryfile = "PAC-1216_EP-1749865-A1.xml"/*"PAC-1379_EP-1304229-A2.xml"*/;

	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";

	static String field = /* PatentDocument.Classification */PatentDocument.Description;

	public void TermOverlapPerQuery(CollectionReader reader, String queryid) throws IOException{

		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> enfns = afn.getEnglishFNs(queryid);
		int n_enfns = enfns.size();

		QueryGneration query = new QueryGneration(path + _queryfile, 0, 1, 0, 0, 0, 0, true, true);
		Map<String, Integer> qterms = query.getSectionTerms(field);

		String doc = "EP-0603697";
		//		for (String doc : enfns) { 
		int qtermsindoc = 0;
		int i=0;
		for(Entry<String, Integer> t : qterms.entrySet()){
			i++;
			boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");
			if(reader.getTFreq(field, t.getKey(), doc)>0){
				qtermsindoc++;					
			}				

			//			}

		}
		int querysize = qterms.size();
		float overlapratio = (float)qtermsindoc/querysize;
		System.out.println(qtermsindoc + "\t" + querysize + "\t" + overlapratio);

	}

	public float FNsTermOverlapPerQuery(CollectionReader reader, String queryid) throws IOException{

		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> enfns = afn.getEnglishFNs(queryid);
		int n_enfns = enfns.size();

		QueryGneration query = new QueryGneration(path + _queryfile, 0, 1, 0, 0, 0, 0, true, true);
		Map<String, Integer> qterms = query.getSectionTerms(field);

		//		String doc = "EP-0603697";
		float sum =0;
		float avg = 0;
		float overlapratio = 0;
		int querydocintersection;
		for (String doc : enfns) { 
			querydocintersection = 0;
			int i=0;
			for(Entry<String, Integer> t : qterms.entrySet()){
				i++;
				boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
				//			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");
				if(reader.getTFreq(field, t.getKey(), doc)>0){
					querydocintersection++;					
				}	
			}
			//		System.out.println(qtermsindoc);
			//		sum = sum + querydocintersection;
			//		System.out.println(sum);


			int querysize = qterms.size();
			overlapratio = (float)querydocintersection/querysize;
			sum = sum + overlapratio;

			System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
		}
		avg = (float)sum/n_enfns;
		System.out.println("---------------------------------------------------------");
		System.out.println(queryid + "\t" + avg);
		System.out.println("Avg. Term Overlap between query and FN patents =" + avg);
		return avg;
	}
	
	public void FNsTermOverlapAllQueries(CollectionReader reader) throws IOException{
		
		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-part2.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-testoverlap.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(path + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);

			float sum =0;
			float avg = 0;
			float overlapratio = 0;
			int querydocintersection;
			if(n_enfns != 0){
				
				if(n_enfns < 11){
			for (String doc : enfns) { 
				querydocintersection = 0;
				int i=0;
//				int m=0;
				for(Entry<String, Integer> t : qterms.entrySet()){
					i++;
					boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
					//			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");
					
					if(x){
//						m++;
//						System.out.println(m);
						querydocintersection++;					
					}	
				}
				//		System.out.println(qtermsindoc);
				//		sum = sum + querydocintersection;
				//		System.out.println(sum);


				int querysize = qterms.size();
				overlapratio = (float)querydocintersection/querysize;
				sum = sum + overlapratio;

				System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + overlapratio + "\t" + sum);
			}
			
			avg = (float)sum/n_enfns;
			System.out.println("---------------------------------------------------------");
			System.out.println(queryid + "\t" + avg);
			ps.println(queryid + "\t" + avg);
//			System.out.println("Avg. Term Overlap between query and FN patents =" + avg);
				}else{
					System.out.println(queryid+"\t" + "big FNs");
					ps.println(queryid+"\t"+ "big FNs");
				}

		}else{
			System.out.println(queryid+"\t" + "No FN for this query");
			ps.println(queryid+"\t"+ "No FN for this query");
			
		}
			
			//    		System.out.println(queryid + "\t" + queryfile);
		}


	}

	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir); 

		CalculateTermOverlap cto = new CalculateTermOverlap();
		//		cto.TermOverlapPerQuery(reader, _queryid);
		/*float avg = cto.FNsTermOverlapPerQuery(reader, _queryid);
		System.out.println(avg);*/
		cto.FNsTermOverlapAllQueries(reader);

	}
}
