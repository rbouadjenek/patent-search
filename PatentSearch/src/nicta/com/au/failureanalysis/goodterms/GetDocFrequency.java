package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;

import org.apache.lucene.queryparser.classic.ParseException;

public class GetDocFrequency {
	String resultsfile = "output/results/" + "results-lmdir-desc-100.txt"; //First-pass retrieval results.
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX";
	
	public HashMap<String, Float> getTermDocFreqScorePair(String queryid) throws IOException, ParseException{
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		EvaluateResults er = new EvaluateResults();
		ArrayList<String> retrieveddocs = er.getRetrievedPatents(queryid, resultsfile);
		HashMap<String, Float> termsscores = new HashMap<>();
		int k = 100;
		int i = 0;
		
		for (String retdoc : retrieveddocs) {
			i++;
			HashMap<String, Integer> termsfreqs = new HashMap<String, Integer>();
			if(retdoc.length()<11){
			termsfreqs = reader.gettermfreqpairAllsecs("UN-" + retdoc);
			}else{
				termsfreqs = reader.gettermfreqpairAllsecs(retdoc);
			}
			
//			System.out.println("[" + i + "] " + termsfreqs);
			for(Entry<String, Integer> tf : termsfreqs.entrySet()){
				if(termsscores.containsKey(tf.getKey())){
					termsscores.put(tf.getKey(), termsscores.get(tf.getKey()) + (float)tf.getValue()/k);
				}else{
//					float test = (float)t.getValue()/tpsize;
//					System.out.println(test);
					termsscores.put(tf.getKey(), (float)tf.getValue()/k);
				}
			}
//			System.out.println(termsscores.size() + " " + termsscores);					
		}
//		System.out.println(termsscores.size() + " " + termsscores);		
		/*--------------------------- Sort terms scores pair------------------------------------*/
		/*ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);
		
		return termsscoressorted;*/		
		return termsscores;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/PAC-1041-rf-df-score-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/		
		String queryid = /*"PAC-100"*//*"PAC-1904"*//*"PAC-499"*//*"PAC-1612"*//*"PAC-1347"*/"PAC-1041";		
		
		PositiveTermsOverlap olap = new PositiveTermsOverlap();	
		TreeMap<String, Float> rf_tspairs = olap.getTermsScoresPair(queryid);
		/*System.out.println("Relevance Feedback Score");
		System.out.println(rf_tspairs.size() + " " + rf_tspairs);*/
		
		GetDocFrequency df = new GetDocFrequency();
		HashMap<String, Float> df_tspairs = df.getTermDocFreqScorePair(queryid);
		/*System.out.println("Document Frequency Score");
		System.out.println(df_tspairs.size() + " " + df_tspairs);		*/
		
		System.out.println("word\tRFscore  \tDFscore");
		for( Entry<String, Float> rftspair : rf_tspairs.entrySet()){
			String term = rftspair.getKey();
			Float rf_score = rftspair.getValue();
			System.out.println(term + "\t" + rf_score + "\t" + df_tspairs.get(term));
			ps.println(/*term + "\t" + */rf_score + "\t" + df_tspairs.get(term));
		}		
	}
}
