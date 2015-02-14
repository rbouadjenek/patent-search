package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.optimalquery.IpcDefinition;
import nicta.com.au.failureanalysis.pseudorelevancefeedback.PRFTermsScores;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class AbsVsGoodTerm {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX";

	public void getAbsGoodTerms(int tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/AbstractTerms/abstractterms-all.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		String _qrootpath = "data/CLEF-IP-2010/PAC_test/topics/";
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		PRFTermsScores prf = new PRFTermsScores();
		
		CollectionReader reader = new CollectionReader(indexDir);

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration content = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String abstractcontent = content.getAbstract();

			TreeMap<String, Float> RFtermsscores = olap.getTermsScoresPair(queryid);			
			Map<String, Float> PRFtermsscores = prf.getTermsScoresPairPRF(queryid);
			HashMap<String, Float> RFhash = new HashMap<>();

			for(Entry<String, Float> rfts : RFtermsscores.entrySet()){
				RFhash.put( rfts.getKey(), rfts.getValue());
			}

			System.out.println(queryid);
			System.out.println("Abstract: " + abstractcontent);
			
			ps.println(queryid);
			ps.println("Abstract: " + abstractcontent);	
			System.out.println();
			ps.println();
//---------------------------RF-----------------------------
			/*System.out.print("RF Terms: ");
			ps.print("RF Terms: ");
			
			for(Entry<String, Float> tspair : RFtermsscores.entrySet()){
				String term = tspair.getKey();
				Float score = tspair.getValue();

				if (score > tau){
					System.out.print(term + ": "+ score + ", ");
					ps.print(term + ": "+ score + ", ");
				}
			}	*/
			
	
//--------------------------------- DF -------------------------------------			
			GetDocFrequency df = new GetDocFrequency();
			HashMap<String, Float> df_tspairs = df.getTermDocFreqScorePair(queryid);
			/*--------------------------- Sort terms scores pair------------------------------------*/
			ValueComparator bvc = new ValueComparator(df_tspairs);
			TreeMap<String, Float> DFssorted = new TreeMap<String,Float>(bvc);
			DFssorted.putAll(df_tspairs);
			System.out.print("DF Terms: ");
			ps.print("DF Terms: ");
			
			for(Entry<String, Float> tspair : DFssorted.entrySet()){
				String DFterm = tspair.getKey();
				Float DFscore = tspair.getValue();

				if (DFscore > tau){
					System.out.print(DFterm + ": "+ RFhash.get(DFterm) + ", ");
					ps.print(DFterm + ":"+ RFhash.get(DFterm) + ", ");
				}
			}
			/*System.out.println("Document Frequency Score");
			System.out.println(df_tspairs.size() + " " + df_tspairs);*/
			
			System.out.println();
			ps.println();
			System.out.println();
			ps.println();			
//--------------------------------- Query Words -------------------------------	
			HashMap<String, Integer> query_terms = reader.gettermfreqpairAllsecs(qUcid);
			/*--------------------------- Sort terms scores pair------------------------------------*/
			ValueComparator1 bvc1 =  new ValueComparator1(query_terms);
			TreeMap<String, Integer> qterms_sorted = new TreeMap<String,Integer>(bvc1);
			qterms_sorted.putAll(query_terms);
			System.out.print("QTF terms: ");
			ps.print("QTF Terms: ");
//			System.out.println(qterms_sorted);
			for(Entry<String, Integer> tspair : qterms_sorted.entrySet()){
				String QTFterm = tspair.getKey();
				Integer QTFscore = tspair.getValue();

				if (QTFscore > tau){
					System.out.print(QTFterm + ": "+ RFhash.get(QTFterm) + ", ");
					ps.print(QTFterm + ":"+ RFhash.get(QTFterm) + ", ");
				}
			}
						
//----------------------------PRF----------------------------
			System.out.println();
			ps.println();
			System.out.println();
			ps.println();
			System.out.print("PRF Terms: ");
			ps.print("PRF Terms: ");
			for(Entry<String, Float> tspair : PRFtermsscores.entrySet()){
				String PRFterm = tspair.getKey();
				Float PRFscore = tspair.getValue();

				if (PRFscore > tau){
					System.out.print(PRFterm + ": " + RFhash.get(PRFterm) + ", ");
					ps.print(PRFterm + ":" + RFhash.get(PRFterm) + ", ");
				}
			}	
			/*System.out.println("PRF Score");
			System.out.println(PRFtermsscores.size() + " " + PRFtermsscores);*/
//------------------------------IPC def --------------------------			
			System.out.println();
			ps.println();
			System.out.println();
			ps.println();
			System.out.print("IPC def Terms: ");
			ps.print("IPC def Terms: ");
			
			IpcDefinition ipcdef = new IpcDefinition();
			ArrayList<String> ipcdefs = ipcdef.GetIpcDefWords(queryfile);
			for(String ipcterm:ipcdefs){
				System.out.print(ipcterm + ": " + RFhash.get(ipcterm) + ", ");
				ps.print(ipcterm + ": " + RFhash.get(ipcterm) + ", ");
			}
			
			System.out.println();
			ps.println();
			System.out.println();
			ps.println();
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		int tau = Integer.parseInt(args[0]);
		AbsVsGoodTerm at = new AbsVsGoodTerm();
		at.getAbsGoodTerms(tau);
	}

}
