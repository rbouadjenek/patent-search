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

import nicta.com.au.failureanalysis.QuerywithFirstRankTPs.ScoreQtermsWrtRetDocs;
import nicta.com.au.failureanalysis.QuerywithFirstRankTPs.TopRankedTPs;
import nicta.com.au.failureanalysis.QuerywithFirstRankTPs.TopRankedTPsPlusPatQ;
import nicta.com.au.failureanalysis.SectionBasedAnalysis.PRFScoreSection;
import nicta.com.au.failureanalysis.SectionBasedAnalysis.RFScoreSections;
import nicta.com.au.failureanalysis.optimalquery.CreateOptimalPatentQuery;
import nicta.com.au.failureanalysis.optimalquery.CreateOptimalQuery;
import nicta.com.au.failureanalysis.optimalquery.CreateQueryRemoveDFwords;
import nicta.com.au.failureanalysis.pseudorelevancefeedback.CreatPRFquery;
import nicta.com.au.failureanalysis.pseudorelevancefeedback.PRFPatQueryTermSelection;
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
//	//	public String outputfile = "./output/results/results-lmdir-desc-100.txt";
//	public String outputfile = "./output/result_PRFquery-tau10-test.txt";
//
//	public FileOutputStream out = new FileOutputStream(outputfile);
//	public PrintStream ps = new PrintStream(out);
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


	public void execute(float tau, int querysize, String field) throws IOException, Exception {
		float Tau = tau;
		int Qsize = querysize;
		
		TopicsInMemory topics = new TopicsInMemory(topicFile);
		long start = System.currentTimeMillis();
		int j = 0;
		boolean startP = false;
		CreateOptimalQuery oq = new CreateOptimalQuery();
		CreatPRFquery prfq = new CreatPRFquery();
		GeneralParseQuery pq = new GeneralParseQuery();
		CreateOptimalPatentQuery optpatentq = new CreateOptimalPatentQuery();
		PRFPatQueryTermSelection PRFts = new PRFPatQueryTermSelection();
		CreateQueryRemoveDFwords c = new CreateQueryRemoveDFwords();
		TopRankedTPs t = new TopRankedTPs();
		ScoreQtermsWrtRetDocs s = new ScoreQtermsWrtRetDocs();
		RFScoreSections sec = new RFScoreSections();
		PRFScoreSection PRFsec = new PRFScoreSection();
		TopRankedTPsPlusPatQ tpatq = new TopRankedTPsPlusPatQ();
		
		for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
			String qUcid = e.getValue().getUcid();
			String queryid = e.getKey();
			String queryfile = e.getKey() + "_" + e.getValue().getUcid() + ".xml";
		
			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();
						
			/*----------------------------- 1-Create optimal query(score-threshold) -------------------------*/
//			String optquery = oq.generateOptimalQuery(queryid, Tau);
//			Query q = oq.parse(optquery, ipcfilter);
			/*--------------------------------------------------------------------------------------------*/
			
			/*----------------------------- 2-Create optimal query(query-size) -------------------------*/
//			String optquery = oq.generateOptQuerySize(queryid, Qsize);
//			Query q = oq.parse(optquery, ipcfilter);
			/*--------------------------------------------------------------------------------------------*/

			
			/*--------------------------------- 3-Create PRF query(score-threshold) -----------------------------*/
//			String PRFquery = prfq.generatePRFQuery(queryid, tau);	
//			Query q = pq.parse(PRFquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*--------------------------------- 4-Create PRF query(query-size) -----------------------------*/
//			String PRFquery = prfq.generatePRFQuerysize(queryid, Qsize);	
//			Query q = pq.parse(PRFquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/

			/*--------------------------------- 5-Create patent query(PQ term selection based on RF score) -----------------------------*/
//			String patentquery = optpatentq.GenerateOptPatentQuery(queryid, qUcid, tau);
//			Query q = pq.parse(patentquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*--------------------------------- 6-Create patent query(PQ term selection based on PRF score) -----------------------------*/
//			String patentquery = PRFts.GeneratePRFtPatentQuery(queryid, qUcid, tau);  
//			Query q = pq.parse(patentquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*--------------------------------- 7-Create PRF query minus doc frequent words -----------------------------*/
			/*------------------ the results were bad --------------------*/
//			String patentquery = PRFts.GeneratePRFQueryMinusDF(queryid, qUcid/*, Tau*/);   
//			Query q = pq.parse(patentquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
						
			/*------------------ 8-Create patent query minus frequent words in top-100 -----------------*/
//			int delta = Qsize;
//			String newquery = c.GeneratePatQueryRemoveDFs(queryid, qUcid, tau, delta);
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*------------------ 9-Create patent query minus frequent words in top-100, keep ipc def and QTF(t) > delta -----------------*/
//			int delta = Qsize;
//			String newquery = c.GeneratePatQueryRemDFs3Conditions(queryid, qUcid, queryfile, tau, delta);
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*------------------ 10-Create Partial RF query with top k TPs -----------------*/
			/*--------- Attention: querysize used for k -----------*/
//			String newquery = t.generateTopRFQuery(queryid, Tau, querysize);
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*------------------ 11-Create patent query with RF top k TPs -----------------*/
			/*------------------ Attention: querysize used for k ----------------*/
//			String newquery = t.selectTopRFQTerms(queryid, qUcid, Tau, querysize); 
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*------------------ 12-Create partial RF query with top k TPs plus query patent -----------------*/
			/*--------- Attention: querysize used for k -----------*/
//			String newquery = tpatq.generateTopRFQuery(queryid, qUcid, Tau, querysize); 
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/
			
			/*------------------ 13-Create RF patent query, take patent query as a TP then remove terms in top-100 or bottom k -----------------*/
//			int bottomk = querysize;
//			String newquery = s.generateRFPatQuery(queryid, qUcid, Tau, bottomk); 
//			Query q = pq.parse(newquery, ipcfilter);
			/*--------------------------------------------------------------------------------*/

/*--------------------------------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------ SECTION-BASED ANALYSIS --------------------------------------------------*/
/*--------------------------------------------------------------------------------------------------------------------------------------*/
			
			/*----------------------- 14-Create RF query based on sections ---------------------*/			
			String newquery = sec.createRFSectionbasedQuery(queryid, Tau, field); 
			Query q = pq.parse(newquery, ipcfilter);
			/*----------------------------------------------------------------------------------*/
			
		   /*----------------------- 15-Create PRF query based on sections ---------------------*/			
//			String newquery = PRFsec.createPRFSectionbasedQuery(queryid, Tau, field); 
//			Query q = pq.parse(newquery, ipcfilter);
			/*----------------------------------------------------------------------------------*/
			
			
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
//				                ps.println(queryid + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
			}
			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				i++;
				Document doc = searcher.getIndexSearch().doc(scoreDoc.doc);
				// TODO: uncomment to print the result on console  
				System.out.println(queryid + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + i + " " + scoreDoc.score + " STANDARD");

				/*-------------------------------- Write the retrieved results in output text file. ----------------------- */                

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
		topicFile = "data/CLEF-IP-2010/PAC_test/topics/PAC_topics-filtered.xml"; /*PAC_topics-omit-PAC-1094.xml */
		topK = 100;
		sim = "lmdir";
		float tau = Float.parseFloat(args[0]);
		int querysize = Integer.parseInt(args[1]);

		String titlefield = PatentDocument.Title;
		String absfield = PatentDocument.Abstract;
		String descfield = PatentDocument.Description;
		String claimsfield = PatentDocument.Claims;
		
		try {
			GeneralExecuteTopics ex = new GeneralExecuteTopics(indexDir, topicFile, topK, sim, decay);
			ex.execute(tau, querysize, titlefield);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
