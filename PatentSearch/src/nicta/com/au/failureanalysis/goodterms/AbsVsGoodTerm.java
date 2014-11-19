package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class AbsVsGoodTerm {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX";

	public void getAbsGoodTerms(String tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
//		String outputfile = "./output/AbstractTerms/abstractterms.txt";
//
//		FileOutputStream out = new FileOutputStream(outputfile);
//		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		String _qrootpath = "data/CLEF-IP-2010/PAC_test/topics/";
		PositiveTermsOverlap olap = new PositiveTermsOverlap();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration content = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String abstractcontent = content.getAbstract();

			TreeMap<String, Float> RFtermsscores = olap.getTermsScoresPair(queryid);

			System.out.println(queryid);
			System.out.println("Abstract: " +abstractcontent);
			
//			ps.println(queryid);
//			ps.println("Abstract: " + abstractcontent);	
			System.out.println();
//			ps.println();

			System.out.print("Important Terms: ");
//			ps.print("Important Terms: ");
			
			for(Entry<String, Float> tspair : RFtermsscores.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();

				if (score > 10){
					System.out.print(term + ", ");
//					ps.print(term + ", ");
				}
			}	
			System.out.println();
//			ps.println();
			System.out.println();
//			ps.println();
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		String tau = args[0];
		AbsVsGoodTerm at = new AbsVsGoodTerm();
		at.getAbsGoodTerms(tau);
	}

}
