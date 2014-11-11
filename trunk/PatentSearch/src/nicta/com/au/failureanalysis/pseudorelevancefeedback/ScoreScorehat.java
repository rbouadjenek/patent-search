package nicta.com.au.failureanalysis.pseudorelevancefeedback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class ScoreScorehat {

	public static void main(String[] args) throws IOException, ParseException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/relevancefeedback-score/score-scorehat.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		PRFTermsScores ts = new PRFTermsScores();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			TreeMap<String, Float> RFtermsScores = olap.getTermsScoresPair(queryid);
			TreeMap<String, Float> PRFtermsScores = ts.getTermsScoresPairPRF(queryid);
			HashMap<String, Float> PRFhash = new HashMap<>();

			for(Entry<String, Float> prfts:PRFtermsScores.entrySet()){
				PRFhash.put( prfts.getKey(), prfts.getValue());
			}

			int j =0;
			float sumscore = 0;
			float sumscorehat = 0;
			for(Entry<String, Float> tspair : RFtermsScores.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();
				if (score>10 && PRFhash.containsKey(term)){
					j++;
					Float scorehat = PRFhash.get(term);
//					System.out.println(" ["+j+"] "+term + "\t" + score + "\t" + scorehat );
					sumscore = sumscore + score;
					sumscorehat = sumscorehat + scorehat;
				}else{break;}			
			}
			System.out.println(queryid + "\t" + sumscore + "\t" + sumscorehat);
			ps.println(queryid + "\t" + sumscore + "\t" + sumscorehat);
		}
	}
}
