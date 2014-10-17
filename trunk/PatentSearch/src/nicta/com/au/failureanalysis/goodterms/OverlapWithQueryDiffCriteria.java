package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class OverlapWithQueryDiffCriteria {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	public static void main(String[] args) throws IOException, ParseException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/relevancefeedback-score/termoverlp-diff.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		CollectionReader reader = new CollectionReader(indexDir); 
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");

			int fpssize = fps.size();

			/*System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			System.out.println("=========================================");*/

			Map<String, Float> termscorepairs = olap.getTermsScoresPair(queryid, qUcid);
			System.out.println(termscorepairs);
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
			HashSet<Float> positivescore = new HashSet<Float>();
			for(Entry<String, Float> tspair : termscorepairs.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();
				if (score>0 && query_terms.contains(term)){
					overlap1++;
				}
				/*if(value>0){
					positivescore.add(value);
				}*/
			}

			System.out.println(queryid + "\t" + overlap1);
			
		}
	}
}
