package nicta.com.au.failureanalysis.ipccode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionSearcher;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class DocsContainQuerySplittedIpc {
	String _qrootpath = "data/CLEF-IP-2010/PAC_test/topics/";
	String indexDir =  "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	String classfield = PatentDocument.Classification;
//	HashSet<String> docs = new HashSet<>();
	
	public void docsSplittedQueriesIpcFilter() throws IOException{
		
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/IPCOverlap/1stTwoIpcfilter.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		int docssize;
		int sum = 0;
		float avg;
		int topicsize = 0;
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		CollectionSearcher searcher = new CollectionSearcher(indexDir, "bm25ro", 2000000000);
		
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = queryid + "_" + topic.getValue().getUcid() + ".xml";
									
			QueryGneration query = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			List<String> qipcs = query.getIpclist();

			HashSet<String> docs = new HashSet<>();
			for(String qipc : qipcs){
				
				/*----------- Use 'firsttwoipc' for a filter like 'C07' ------------*/
				String firsttwoqipc = qipc.substring(0, 3).toLowerCase();
				/*----------- Use 'firstQipc' for a filter like 'C' ------------*/
				String firstqipc = qipc.substring(0, 1).toLowerCase();				
				
				System.out.print(/*firstqipc*/firsttwoqipc + "\t" );
//				System.out.println(searcher.singleTermSearch(classfield, qipc.toLowerCase()));
				docs.addAll(searcher.prefixQuerySearch(classfield, /*firstqipc*/firsttwoqipc));
			}
			System.out.println();
			System.out.println(queryid +"\t" + query.getIpc()+ "\t"+docs.size());
			docssize = docs.size();
			ps.println(queryid +"\t" /*+ query.getIpc()+ "\t"*/+docs.size());
			sum = sum + docssize;
			topicsize = topics.getTopics().size();
		}
		
		avg = (float)sum/topicsize;
		System.out.println("--------------------------------------------------------");
		System.out.println("Avg. number of documents filtered by IPC filter: " + avg);

	}
	
	public static void main(String[] args) throws IOException {
		
		DocsContainQuerySplittedIpc  docipc = new DocsContainQuerySplittedIpc ();
		docipc.docsSplittedQueriesIpcFilter();
		
	}

}
