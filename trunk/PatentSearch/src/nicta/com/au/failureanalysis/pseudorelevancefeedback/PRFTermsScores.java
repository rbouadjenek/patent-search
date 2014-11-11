package nicta.com.au.failureanalysis.pseudorelevancefeedback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.QueryAndPatents;
import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
//import nicta.com.au.failureanalysis.goodterms.ValueComparator;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class PRFTermsScores {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	QueryAndPatents qps = new QueryAndPatents();


	public TreeMap<String, Float> getTermsScoresPairPRF(String queryid) throws IOException, ParseException{
		TreeMap<String,Float> termsscoressorted = null;
		CollectionReader reader = new CollectionReader(indexDir); 
		HashMap<String, Float> termsscores = new HashMap<>();

		HashMap<String, HashMap<String, String>> _docranks = qps
				.GetQueryRanksPatents2("output/results/results-lmdir-desc-100.txt");

		/*In PRF, we assume that top 5 docs are TP*/
		for(int i=1;i<=5;i++){
			String top5_patent = _docranks.get(queryid).get(Integer.toString(i));
			/*System.out.println(i + " " + top5_patent);*/

			HashMap<String, Integer> termsfreqsTP =new HashMap<String, Integer>();

			termsfreqsTP = reader.gettermfreqpairAllsecs("UN-" + top5_patent);
			//			System.out.println(termsfreqsTP);

			for(Entry<String, Integer> tfTP:termsfreqsTP.entrySet()){

				if(termsscores.containsKey(tfTP.getKey())){
					termsscores.put(tfTP.getKey(), termsscores.get(tfTP.getKey()) + (float)tfTP.getValue()/5);
				}else{
					//					float test = (float)t.getValue()/tpsize;
					//					System.out.println(test);
					termsscores.put(tfTP.getKey(), (float)tfTP.getValue()/5);
				}
			}
			/*System.out.println(termsscores.size() + " " + termsscores);	*/
		}
//		System.out.println();

		/*In PRF, we assume that bottom 95 docs are FP*/
		for(int i=6;i<=100;i++){
			String top95_patents = _docranks.get(queryid).get(Integer.toString(i));
			/*System.out.println(i + " " + top95_patents);*/

			HashMap<String, Integer> termsfreqsFP = reader.gettermfreqpairAllsecs("UN-" + top95_patents);

			for(Entry<String, Integer> t:termsfreqsFP.entrySet()){
				if(termsscores.containsKey(t.getKey())){
					termsscores.put(t.getKey(), termsscores.get(t.getKey()) - (float)t.getValue()/95);
				}else{						
					termsscores.put(t.getKey(), -(float)t.getValue()/95);
				}
			}
			/*System.out.println(termsscores.size() + " " + termsscores);	*/
		}


		/*--------------------------- Sort terms scores pair------------------------------------*/
		ValueComparator bvc =  new ValueComparator(termsscores);
		termsscoressorted = new TreeMap<String,Float>(bvc);
		termsscoressorted.putAll(termsscores);

		return termsscoressorted;
	}


	public static void main(String[] args) throws IOException, ParseException {
		String queryid = "PAC-191";

		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		PRFTermsScores ts = new PRFTermsScores();
		
		TreeMap<String, Float> RFtermsScores = olap.getTermsScoresPair(queryid);
		TreeMap<String, Float> PRFtermsScores = ts.getTermsScoresPairPRF(queryid);
		HashMap<String, Float> PRFhash = new HashMap<>();

		for(Entry<String, Float> prfts:PRFtermsScores.entrySet()){
			PRFhash.put( prfts.getKey(), prfts.getValue());
		}

		System.out.println("RF: " + RFtermsScores.containsKey("separ") + "\t" + RFtermsScores);
		System.out.println("PRF: " + /*PRFtermsScores*/PRFhash.containsKey("emiss") + "\t" + PRFtermsScores);		

		int j =0;
		float sumscore = 0;
		float sumscorehat = 0;
		for(Entry<String, Float> tspair : RFtermsScores.entrySet()){
			String term = tspair.getKey();
			Float score = tspair.getValue();
			if (score>10 && PRFhash.containsKey(term)){
				j++;
				Float scorehat = PRFhash.get(term);
				System.out.println(" ["+j+"] "+term + "\t" + score + "\t" + scorehat );
				sumscore = sumscore + score;
				sumscorehat = sumscorehat + scorehat;
			}else{break;}			
		}
		System.out.println(sumscore + "\t" + sumscorehat);

	}
}


/*-------------- This class is used to sort a hashmap --------------*/

class ValueComparator implements Comparator<String> {

	Map<String, Float> base;
	public ValueComparator(Map<String, Float> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
