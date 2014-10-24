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

import org.apache.lucene.queryparser.classic.ParseException;

public class OverlapWithQueryDiffCriteria {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public void overlapWithQuerytop100pos() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/relevancefeedback-score/termoverlp-postop100-tophalf4.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String qUcid = topic.getValue().getUcid();


			/*--------------------------------- Query Words -------------------------------*/
			//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_terms.size();
			//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
//			System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);
			
			int middle = termsscores_sorted.size()/2;
			/*int overlap1=0;
			int overlap2=0;*/
			int overlap3=0;
			int overlap4=0;
			/*int count1=0;
			int count2=0;*/
			int i =0;
			int k =0;
			int s =0;
//			System.out.println(termsscores_sorted);
			for(Entry<String, Float> tspair : termsscores_sorted.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();
				
				k++;
				if(k<=100 && score>0){
					s++;
					if(query_terms.contains(term)){
						overlap3++;
					}					
				}

				
				i++;
				if(i<=middle){
					if(query_terms.contains(term)){
						overlap4++;
					}					
				}			
			}			
			
			float o3 = (float)overlap3/s;
			float o4 = (float)overlap4/middle;
			
			System.out.println(s+"\t"+queryid + "\t" + o3 + "\t" + o4);
			ps.println(queryid + "\t" + o3 + "\t" + o4);
			/*System.out.println(median +"\t"+ queryid + "\t" + o1 + "\t" + o2+ "\t" + o3 + "\t" + overlap4 + "\t" + o5);
			ps.println(queryid + "\t" + o1 + "\t" + o2+ "\t" + o3 + "\t" + overlap4 + "\t" + o5);*/
		}					
	}
	
	public void overlapWithQueryMedian() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/relevancefeedback-score/test.txt";/*termoverlp-tophalf-all4.txt";
*/
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String qUcid = topic.getValue().getUcid();


			/*--------------------------------- Query Words -------------------------------*/
			//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_terms.size();
			//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
//			System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);
			
			int middle = termsscores_sorted.size()/2;
			int overlap1=0;
			int overlap2=0;
			int overlap3=0;
			int overlap4=0;
			int overlap10=0;
			int count10=0;
			int count1=0;
			int count2=0;
			int i =0;
			int k =0;
//			HashSet<Float> positivescore = new HashSet<Float>();
			for(Entry<String, Float> tspair : termsscores_sorted.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();
				if (score>10 && query_terms.contains(term)){
					overlap10++;
				}
				if (score>10){
					count10++;
				}
				if (score>1 && query_terms.contains(term)){
					overlap10++;
				}
				if (score>1){
					count10++;
				}
				if (score>0 && query_terms.contains(term)){
					overlap2++;
				}
				if (score>0){
					count2++;
				}
				k++;
				if(k<=100){
					if(query_terms.contains(term)){
						overlap3++;
					}					
				}
				
				i++;
				if(i<=middle){
					if(query_terms.contains(term)){
						overlap4++;
					}					
				}			
			}			
			
			HashMap<String, Float> useful_terms = new HashMap<>();
			HashMap<String, Float> non_useful_terms = new HashMap<>();
			for(Entry<String, Float> scoresorted:termsscores_sorted.entrySet()){
				if(scoresorted.getValue()>0){				
					useful_terms.put( scoresorted.getKey(), scoresorted.getValue());
				}else{
					non_useful_terms.put( scoresorted.getKey(), scoresorted.getValue());
				}
			}	
			ValueComparator bvc =  new ValueComparator(useful_terms);
			TreeMap<String,Float> useful_terms_sorted = new TreeMap<String,Float>(bvc);
			useful_terms_sorted.putAll(useful_terms);
			int median = useful_terms_sorted.size()/2;
			
//			System.out.println(useful_terms_sorted.size() +"\t" + median  + "\t" + useful_terms_sorted);
			int j=0;
			int overlap_median = 0;
			for(Entry<String, Float> usefulterm:useful_terms_sorted.entrySet()){
				String uterm = usefulterm.getKey();
				j++;
				if(j<=median){
					if(query_terms.contains(uterm)){
						overlap_median++;
					}						
				}
			}
			float o1 = (float)overlap1/count1;
			float o2 = (float)overlap2/count2;
			float o3 = (float)overlap3/100;
			float o_med = (float)overlap_median/median;
			float o10 = (float)overlap10/count10;
			System.out.println(median +"\t"+ queryid + "\t" + o1 + "\t" + o2+ "\t" + o3 + "\t" /*+ overlap4 + "\t"*/ + o_med);
			ps.println(queryid + "\t" + o1 + "\t" + o2+ "\t" + o3 + "\t" +/* overlap4 + "\t" +*/ o_med+ "\t" + o10);
		}					
	}
	
	public void overlapWithQueryAll() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/relevancefeedback-score/termoverlp-diff-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
						

			/*System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			System.out.println("=========================================");*/

			Map<String, Float> termscorepairs = olap.getTermsScoresPair(queryid);
//			System.out.println(termscorepairs.size() + "\t" + termscorepairs);
			
			int middle = termscorepairs.size()/2;
//			System.out.println(termscorepairs.values());
			/*HashMap<String, Float> tshashes = new HashMap<>();
			tshashes.putAll(termscorepairs);*/

			//			System.out.println(tspairs.size() + "\t" + tspairs);
			//			System.out.println(tshashes.size() + "\t" + tshashes.get("piston")+ "\t" + tshashes);
			
			/*--------------------------------- Query Words -------------------------------*/			
			//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_terms.size();
			//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
			//			System.out.println(query_terms.size() + "\t" + query_terms);
			int overlap1=0;
			int overlap2=0;
			int overlap3=0;
			int overlap4=0;
			int i =0;
			int j =0;
			HashSet<Float> positivescore = new HashSet<Float>();
			for(Entry<String, Float> tspair : termscorepairs.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();
				if (score>1 && query_terms.contains(term)){
					overlap1++;
				}
				if (score>0 && query_terms.contains(term)){
					overlap2++;
				}
				
				if(j<=100){
					if(query_terms.contains(term)){
						overlap3++;
					}					
				}
				
				i++;
				if(i<=middle){
					if(query_terms.contains(term)){
						overlap4++;
					}					
				}
				
				j++;
				
				/*if(value>0){
					positivescore.add(value);
				}*/
			}

			System.out.println(middle +"\t"+ queryid + "\t" + overlap1 + "\t" + overlap2+ "\t" + overlap3 + "\t" + overlap4);
			ps.println(queryid + "\t" + overlap1 + "\t" + overlap2+ "\t" + overlap3 + "\t" + overlap4);
			
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		OverlapWithQueryDiffCriteria olap = new OverlapWithQueryDiffCriteria();
//		olap.overlapWithQueryAll();
//		olap.overlapWithQueryMedian();
		olap.overlapWithQuerytop100pos();
	}
}


