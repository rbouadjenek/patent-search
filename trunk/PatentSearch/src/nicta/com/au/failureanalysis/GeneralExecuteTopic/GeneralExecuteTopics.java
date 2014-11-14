/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.failureanalysis.GeneralExecuteTopic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import nicta.com.au.failureanalysis.optimalquery.CreateOptimalQuery;
import nicta.com.au.failureanalysis.pseudorelevancefeedback.CreatPRFquery;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.search.PACSearcher;
import nicta.com.au.patent.document.PatentDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author rbouadjenetopK
 */
public final class GeneralExecuteTopics {
	static String path = "data/CLEF-IP-2010/PAC_test/topics/";

	/*--------------------------- Write in output file. -Mona ------------------------*/
	//	public String outputfile = "./output/results/results-lmdir-desc-100.txt";
	/*public String outputfile = "./output/result_prfquery-test.txt";

	public FileOutputStream out = new FileOutputStream(outputfile);
	public PrintStream ps = new PrintStream(out);*/
	/*-------------------------------------------------------------------------------*/

	private final File topicFile;
	private final PACSearcher searcher;
	private final String startingPoint;

	public GeneralExecuteTopics(String indexDir, String topicFile, int topK, String similarity, 
			String decay) throws IOException, Exception {
		this.topicFile = new File(topicFile);
		searcher = new PACSearcher(indexDir, similarity, topK);
		startingPoint = decay;
		if (startingPoint.equals("-1")) {
			System.out.print(" #indexDir: " + indexDir);
			System.out.print(" #topicfile: " + topicFile);
			System.out.print(" #topK: " + topK);
			System.out.println(" #Similarity: " + similarity);
		}
	}


	public void execute(int tau, int querysize) throws IOException, Exception {
		int Tau = tau;
		int Qsize = querysize;
		
		TopicsInMemory topics = new TopicsInMemory(topicFile);
		long start = System.currentTimeMillis();
		int j = 0;
		boolean startP = false;
		CreateOptimalQuery oq = new CreateOptimalQuery();
		CreatPRFquery prfq = new CreatPRFquery();
		GeneralParseQuery pq = new GeneralParseQuery();
		
		for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
			String queryid = e.getKey();
			String queryfile = e.getKey() + "_" + e.getValue().getUcid() + ".xml";
		
			/*----------------------------- Create optimal query(query-size) -------------------------*/
//			String optquery = oq.generateOptQuerySize(queryid, Qsize);
//			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
//			String ipcfilter = g.getIpc();
//			Query q = oq.parse(optquery, ipcfilter);
			/*--------------------------------------------------------------------------------------------*/
			
			/*----------------------------- Create optimal query(score-threshold) -------------------------*/
//			String optquery = oq.generateOptimalQuery(queryid, Tau);
//			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
//			String ipcfilter = g.getIpc();
//			//			if(optquery!=null){
//			Query q = oq.parse(optquery, ipcfilter);
//			//				System.err.println(q);
//			//			}else{System.err.println("no optimal query for this query paetent");}
			/*--------------------------------------------------------------------------------------------*/
			
			/*--------------------------------- Create PRF query -----------------------------*/
			String PRFquery = prfq.generatePRFQuery(queryid, tau);
			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();

			Query q = pq.parse(PRFquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			j++;			
			if (startingPoint.equals("-1")) {
				startP = true;
			} else if (startingPoint.equals(queryid)) {
				startP = true;
				continue;
			}
			if (!startP) {
				continue;
			}
			PatentDocument pt = e.getValue();
			System.err.println(j + "- " + queryid + " -> " + pt.getUcid());
			System.err.println(q);
			long start2 = System.currentTimeMillis();
			TopDocs hits;

			hits = searcher.search(q);

			long end2 = System.currentTimeMillis();
			System.err.println(" - Found " + hits.totalHits
					+ " document(s) has matched query " + pt.getUcid() + ". Processed in " + Functions.getTimer(end2 - start2) + ".");

			//            System.err.println(queryid + "\t" + hits.totalHits);
			int i = 0;
			if (hits.totalHits == 0) {
				System.out.println(queryid + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
				//                ps.println(queryid + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
			}
			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				i++;
				Document doc = searcher.getIndexSearch().doc(scoreDoc.doc);
				// TODO: uncomment to print the result on console  
				System.out.println(queryid + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + i + " " + scoreDoc.score + " STANDARD");

				/*-------------------------------- Write the retrieved results in output text file. -Mona ----------------------- */                

//				                ps.println(queryid + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + i + " " + scoreDoc.score + " STANDARD");

				/*------------------------------------------------------------------------------------------------------------------*/        
			}
		}
		long end = System.currentTimeMillis();
		long millis = (end - start);
		System.err.println("#Global Execution time: " + Functions.getTimer(millis) + ".");
	}


	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String indexDir;
		String topicFile;
		int topK;
		String sim;
		String decay = "-1";

		indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010/";
		topicFile = "data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml";
		topK = 100;
		sim = "lmdir";
		int tau = Integer.parseInt(args[0]);
		int querysize = Integer.parseInt(args[1]);

		try {
			GeneralExecuteTopics ex = new GeneralExecuteTopics(indexDir, topicFile, topK, sim, decay);
			ex.execute(tau, querysize);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
