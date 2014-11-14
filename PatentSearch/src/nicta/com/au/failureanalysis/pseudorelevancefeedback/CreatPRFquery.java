package nicta.com.au.failureanalysis.pseudorelevancefeedback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class CreatPRFquery {

	public String generatePRFQuery(String queryid, int tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		/*String outputfile = "./output/optimalquery/results-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);*/
		/*-------------------------------------------------------------------------------*/

		PRFTermsScores prf = new PRFTermsScores();
		Map<String, Float> tspairs = prf.getTermsScoresPairPRF(queryid);

		String prf_query = "";
		int i = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
				i++;
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
//					prf_query += tskey + "^" + tsvalue + " ";
					prf_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		if(i == 0){
			System.err.println("There is no optimal query");
			return null;
		}

		return prf_query;
	}

	public static void main(String[] args) throws IOException, ParseException {
		int tau = 0;
		CreatPRFquery prfquery = new CreatPRFquery();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");  /*test1.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
			String a = prfquery.generatePRFQuery(queryid, tau);
			System.out.println(a);
		}

	}


}
