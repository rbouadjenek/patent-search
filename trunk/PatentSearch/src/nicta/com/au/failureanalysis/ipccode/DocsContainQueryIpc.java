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

public class DocsContainQueryIpc {
	String _qrootpath = "data/CLEF-IP-2010/PAC_test/topics/";
	String indexDir =  "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	String classfield = PatentDocument.Classification;
//	HashSet<String> docs = new HashSet<>();
	
	public void docsIpcFilterAllQueries() throws IOException{
		
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/IPCOverlap/ipcfilter-1.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		int docssize;
		int sum = 0;
		float avg;
		int topicsize = 0;
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test.xml");
		CollectionSearcher searcher = new CollectionSearcher(indexDir, "bm25ro", 2600000);
		
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = queryid + "_" + topic.getValue().getUcid() + ".xml";
									
			QueryGneration query = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			List<String> qipcs = query.getIpclist();

			HashSet<String> docs = new HashSet<>();
			for(String qipc:qipcs){
				System.out.print(qipc + "\t" );
//				System.out.println(searcher.singleTermSearch(classfield, qipc.toLowerCase()));
				docs.addAll(searcher.singleTermSearch(classfield, qipc.toLowerCase()));
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
		
		DocsContainQueryIpc docipc = new DocsContainQueryIpc();
		docipc.docsIpcFilterAllQueries();
		
	}

}
