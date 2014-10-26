package nicta.com.au.failureanalysis.weighting;

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
import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class WeightHistogram {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	EvaluateResults er = new EvaluateResults();
	AnalyseFNs afn = new AnalyseFNs();

	public void weightPerQueryFNs() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/weighting/weightHisto_FNs.txt";/*termoverlp-tophalf-all4.txt";
		 */
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		EvaluateResults er = new EvaluateResults();
		System.out.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
		System.out.println("---------------------------------------------------------");
		ps.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");/*PAC_topics-omit-PAC-1094.xml");
*/		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String qUcid = topic.getValue().getUcid();

			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int fns_size = enfns.size();
						
			/*--------------------------------- Query Words -------------------------------*/
			HashMap<String, Integer> query_termsfreqs = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			//			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_termsfreqs.size();
			/*System.out.println(querysize +"\t"+ query_termsfreqs);
			System.out.println();*/
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			/*TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
						System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);*/

			boolean exists = false;
			int weight=0;
			int overlap;
			float weightratio = 0;
			float overlapratio = 0;
			float w_sum =0;
			float w_avg = 0;
			float o_sum =0;
			float o_avg = 0;
			float wr_sum =0;
			float wr_avg = 0;
			float or_sum =0;
			float or_avg = 0;
			
			if(fns_size != 0){
			for (String doc : enfns) { 
				weight = 0;
				overlap = 0;
				HashMap<String, Integer> docterms = reader.gettermfreqpairAllsecs("UN-" + doc);
				
				int doclength = 0; 
				for (int i : docterms.values()) {
					doclength = doclength + i;
				}
				
//				System.out.println(doclength +"\t"+ docterms.size()+"\t"+docterms);
				
				for(Entry<String, Integer> qt : query_termsfreqs.entrySet()){					
					exists = docterms.keySet().contains(qt.getKey());
					if(exists){
						overlap++;
						weight = weight + docterms.get(qt.getKey());
//						System.out.println(qt+"\t"+docterms.get(qt.getKey()));
					}						
				}	
				weightratio = (float)weight/doclength;
				overlapratio = (float)overlap/querysize;				
//				System.out.println(weightratio + "\t" + overlapratio +"\t"+ weight + "\t" + overlap);
				wr_sum = wr_sum + weightratio;
				or_sum = or_sum + overlapratio;
				w_sum = w_sum + weight;
				o_sum = o_sum + overlap;
			}
			wr_avg = (float)wr_sum/fns_size;
			or_avg = (float)or_sum/fns_size;
			w_avg = (float)w_sum/fns_size;
			o_avg = (float)o_sum/fns_size;
//			System.out.println("---------------------------------------------------------");
			System.out.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
//			System.out.println("---------------------------------------------------------");
			ps.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");
			}
		}	
	}

	public void weightPerQueryTPs() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/weighting/weightHisto_TPs.txt";/*termoverlp-tophalf-all4.txt";
		 */
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		EvaluateResults er = new EvaluateResults();
		System.out.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
		System.out.println("---------------------------------------------------------");
		ps.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");/*PAC_topics-omit-PAC-1094.xml");
*/		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String qUcid = topic.getValue().getUcid();

			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			int tps_size = tps.size();
			
			/*--------------------------------- Query Words -------------------------------*/
			HashMap<String, Integer> query_termsfreqs = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			//			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_termsfreqs.size();
			/*System.out.println(querysize +"\t"+ query_termsfreqs);
			System.out.println();*/
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			/*TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
						System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);*/

			boolean exists = false;
			int weight=0;
			int overlap;
			float weightratio = 0;
			float overlapratio = 0;
			float w_sum =0;
			float w_avg = 0;
			float o_sum =0;
			float o_avg = 0;
			float wr_sum =0;
			float wr_avg = 0;
			float or_sum =0;
			float or_avg = 0;
			
			if(tps_size != 0){
			for (String doc : tps) { 
				weight = 0;
				overlap = 0;
				HashMap<String, Integer> docterms = reader.gettermfreqpairAllsecs("UN-" + doc);
				
				int doclength = 0; 
				for (int i : docterms.values()) {
					doclength = doclength + i;
				}
				
//				System.out.println(doclength +"\t"+ docterms.size()+"\t"+docterms);
				
				for(Entry<String, Integer> qt : query_termsfreqs.entrySet()){					
					exists = docterms.keySet().contains(qt.getKey());
					if(exists){
						overlap++;
						weight = weight + docterms.get(qt.getKey());
//						System.out.println(qt+"\t"+docterms.get(qt.getKey()));
					}						
				}	
				weightratio = (float)weight/doclength;
				overlapratio = (float)overlap/querysize;				
//				System.out.println(weightratio + "\t" + overlapratio +"\t"+ weight + "\t" + overlap);
				wr_sum = wr_sum + weightratio;
				or_sum = or_sum + overlapratio;
				w_sum = w_sum + weight;
				o_sum = o_sum + overlap;
			}
			wr_avg = (float)wr_sum/tps_size;
			or_avg = (float)or_sum/tps_size;
			w_avg = (float)w_sum/tps_size;
			o_avg = (float)o_sum/tps_size;
//			System.out.println("---------------------------------------------------------");
			System.out.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
//			System.out.println("---------------------------------------------------------");
			ps.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
			}else{
				System.out.println(queryid+"\t" + "No TP for this query");
				ps.println(queryid+"\t"+ "No TP for this query");
			}
		}
	}
	
	public void weightPerQueryFPs() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/weighting/weightHisto_FPs2-test.txt";
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		EvaluateResults er = new EvaluateResults();
		System.out.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
//		System.out.println("---------------------------------------------------------");
//		ps.println("queryid" + "\t" + "wr_avg" + "\t" + "or_avg"+ "\t" + "w_avg" + "\t" + "o_avg");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");/*PAC_topics-omit-PAC-1094.xml");
*/		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String qUcid = topic.getValue().getUcid();

			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			int fps_size = fps.size();
			
			/*--------------------------------- Query Words -------------------------------*/
			HashMap<String, Integer> query_termsfreqs = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			//			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_termsfreqs.size();
			/*System.out.println(querysize +"\t"+ query_termsfreqs);
			System.out.println();*/
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			/*TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
						System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);*/

			boolean exists = false;
			int weight=0;
			int overlap;
			float weightratio = 0;
			float overlapratio = 0;
			float w_sum =0;
			float w_avg = 0;
			float o_sum =0;
			float o_avg = 0;
			float wr_sum =0;
			float wr_avg = 0;
			float or_sum =0;
			float or_avg = 0;
			
			if(fps_size != 0){
			for (String doc : fps) { 
				weight = 0;
				overlap = 0;
				HashMap<String, Integer> docterms = reader.gettermfreqpairAllsecs("UN-" + doc);
				
				int doclength = 0; 
				for (int i : docterms.values()) {
					doclength = doclength + i;
				}
				
//				System.out.println(doclength +"\t"+ docterms.size()+"\t"+docterms);
				
				for(Entry<String, Integer> qt : query_termsfreqs.entrySet()){					
					exists = docterms.keySet().contains(qt.getKey());
					if(exists){
						overlap++;
						weight = weight + docterms.get(qt.getKey());
//						System.out.println(qt+"\t"+docterms.get(qt.getKey()));
					}						
				}	
				weightratio = (float)weight/doclength;
				overlapratio = (float)overlap/querysize;				
//				System.out.println(weightratio + "\t" + overlapratio +"\t"+ weight + "\t" + overlap);
				wr_sum = wr_sum + weightratio;
				or_sum = or_sum + overlapratio;
				w_sum = w_sum + weight;
				o_sum = o_sum + overlap;
			}
			wr_avg = (float)wr_sum/fps_size;
			or_avg = (float)or_sum/fps_size;
			w_avg = (float)w_sum/fps_size;
			o_avg = (float)o_sum/fps_size;
//			System.out.println("---------------------------------------------------------");
			System.out.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
//			System.out.println("---------------------------------------------------------");
			ps.println(queryid + "\t" + wr_avg + "\t" + or_avg+ "\t" + w_avg + "\t" + o_avg);
			}else{
				System.out.println(queryid+"\t" + "No FP for this query");
//				ps.println(queryid+"\t"+ "No FP for this query");
			}
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		WeightHistogram w = new WeightHistogram();
//		w.weightPerQueryFNs();
//		w.weightPerQueryTPs();
		w.weightPerQueryFPs();
	}
}
