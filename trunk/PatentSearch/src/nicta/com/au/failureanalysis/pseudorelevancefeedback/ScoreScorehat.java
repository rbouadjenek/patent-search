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

	
	public void ScoreRFTopRFPRFTerms(int tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		//	String outputfile = "./output/relevancefeedback-score/score-scorehat-test.txt";
		//
		//	FileOutputStream out = new FileOutputStream(outputfile);
		//	PrintStream ps = new PrintStream(out);
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
			HashMap<String, Float> RFhash = new HashMap<>();
			

			for(Entry<String, Float> rfts : RFtermsScores.entrySet()){
				RFhash.put( rfts.getKey(), rfts.getValue());
			}

			for(Entry<String, Float> prfts:PRFtermsScores.entrySet()){
				PRFhash.put( prfts.getKey(), prfts.getValue());
			}
			
			int i=0;
			float sumscore = 0;
			float avgscore = 0;
			for(Entry<String, Float> rftspair : RFtermsScores.entrySet()){
				String rfterm = rftspair.getKey();
				Float rfscore = rftspair.getValue();
				if (rfscore>tau){
					i++;
//					Float a = RFhash.get(prfterm);
					sumscore = sumscore + rfscore;
//					System.out.println(" [" + i + "] " + rfterm + "\t" + rfscore);
				}
			}
//			System.out.println("-------------------------");
						
			int j = 0;
			float sumscorehat = 0;
			float avgscorehat = 0;
			for(Entry<String, Float> prftspair : PRFtermsScores.entrySet()){
				
				String prfterm = prftspair.getKey();
				Float prfscore = prftspair.getValue();
				if (prfscore>tau){
					j++;
					Float rfscore2 = RFhash.get(prfterm);
					sumscorehat = sumscorehat + rfscore2;
//					System.out.println(" [" + j + "] "+ prfterm + "\t" + prfscore + "\t" + rfscore2);
				}
			}
			avgscore = sumscore/i;
			avgscorehat = sumscorehat/j;
			System.out.println(queryid + "\t" + avgscore + "\t" + avgscorehat);
		}
	}
	
	/**
	 * @param tau
	 * @throws IOException
	 * @throws ParseException
	 * This function print score_RF(t) and score_PRF(t) for {t|score_RF(t)>tau} 
	 */
	public void ScoreRFScorePRF(int tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		//		String outputfile = "./output/relevancefeedback-score/score-scorehat-test.txt";
		//
		//		FileOutputStream out = new FileOutputStream(outputfile);
		//		PrintStream ps = new PrintStream(out);
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
				if (score>tau && PRFhash.containsKey(term)){
					j++;
					Float scorehat = PRFhash.get(term);
					//					System.out.println(" ["+j+"] "+term + "\t" + score + "\t" + scorehat );
					sumscore = sumscore + score;
					sumscorehat = sumscorehat + scorehat;
				}else{break;}			
			}
			System.out.println(queryid + "\t" + sumscore + "\t" + sumscorehat);
			//			ps.println(queryid + "\t" + sumscore + "\t" + sumscorehat);
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		int tau = Integer.parseInt(args[0]);
		
		ScoreScorehat s = new ScoreScorehat();
//		s.ScoreRFScorePRF(10);
		s.ScoreRFTopRFPRFTerms(tau);
	}
}
