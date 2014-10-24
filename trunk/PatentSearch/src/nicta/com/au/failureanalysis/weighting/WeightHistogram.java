package nicta.com.au.failureanalysis.weighting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

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
		String outputfile = "./output/weighting/test.txt";/*termoverlp-tophalf-all4.txt";
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

			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int size = enfns.size();
			/*--------------------------------- Query Words -------------------------------*/
			//			HashMap<String, Integer> query_termsfreqspair = reader.gettermfreqpair(qUcid, PatentDocument.Description);
			HashSet<String> query_terms = reader.getDocTerms(qUcid, PatentDocument.Description);
			int querysize = query_terms.size();
			//			System.out.println(query_termsfreqspair.size() +"\t"+ query_termsfreqspair);
			//			System.out.println(querysize + "\t" + query_terms);
			/*-----------------------------------------------------------------------------*/			

			TreeMap<String, Float> termsscores_sorted = olap.getTermsScoresPair(queryid);
			//			System.out.println(termsscores_sorted.size() +"\t"+termsscores_sorted);
		
		}
	}

	public static void main(String[] args) throws IOException, ParseException {

	}
}
