package nicta.com.au.failureanalysis.ipccode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.failureanalysis.utility.GetPatentFile;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class IPCcodeOverlap {
	
	String _qrootpath = "data/CLEF-IP-2010/PAC_test/topics/";
	String _queryid = "PAC-1492"/*"PAC-1521"*//*"PAC-1134"*//*"PAC-1012"*//*"PAC-1687"*//*"PAC-1099"*/;
	String _queryfile = "PAC-1492_EP-1719498-A1.xml"/*"PAC-1521_EP-1235091-A2.xml"*//*"PAC-1134_EP-1783182-A1.xml"*//*"PAC-1012_EP-1914594-A2.xml"*//*"PAC-1687_EP-1361254-A1.xml"*//*"PAC-1099_EP-1820820-A1.xml"*/;
	
	String collection_rootpath  = "/media/mona/MyProfesion/";
	
	public void AnalyzeIPCOverlapPerQuery() throws IOException{
		
				
		QueryGneration query = new QueryGneration(_qrootpath + _queryfile, 0, 0, 1, 0, 0, 0, true, true);
		String query_mainipc = query.getmainIpc();	
		
		List<String> query_notmain_ipcs = query.getIpclist();
		query_notmain_ipcs.remove(query_mainipc);
		
		/*--------------- Testing query ipc without the main ipc ---------------*/
		/*for(String newipclist : query_notmain_ipcs){
			System.out.println(newipclist);
		}*/
		/*-----------------------------------------------------------*/
		
		System.out.println(_queryid + "\t" + query_mainipc +"\t"+query.getIpc());
		String removed_qmainipc = query.getIpc().replace(query_mainipc, "");
		System.out.println(removed_qmainipc);
		System.out.println("----------------------------------------------");
				
		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> enfns = afn.getEnglishFNs(_queryid);
		
		int same_mainipc = 0;
		int same_furtheripc = 0;
		int old_same_furtheripc = 0;
		int none = 0;
		
		for(String enfn : enfns){
			GetPatentFile gpf = new GetPatentFile();
			String fn_path = gpf.GetPatentPath(enfn);
			
			QueryGneration doc = new QueryGneration(collection_rootpath + fn_path, 0, 0, 1, 0, 0, 0, true, true);
			String doc_ipc = doc.getIpc();
			List<String> doc_ipc_list = doc.getIpclist();
			System.out.println(enfn + /*"\t" + doc.getmainIpc()+*/ "\t" + doc_ipc);
			if(doc_ipc.contains(query_mainipc)){
				same_mainipc++;
			}else{

				if(query_notmain_ipcs.size() == 0){
					if(!doc_ipc.contains(query_mainipc)){none++;}
				}else{
					old_same_furtheripc = same_furtheripc;
					for(String newipclist : query_notmain_ipcs){
						if(doc_ipc.contains(newipclist)){
							same_furtheripc++; 
							break;
						}
						
					}
					if(old_same_furtheripc == same_furtheripc){none++;}
				}				
			}	
		}
		System.out.println(_queryid + "\t" + query_mainipc + "\t" + same_mainipc + "\t" + same_furtheripc + "\t" + none + "\t" + enfns.size());
		
	}
	
	public void FNsAnalyzeIPCOverlapAllQueries() throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/IPCOverlap/ipcoverlp-k100.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration query = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String query_mainipc = query.getmainIpc();	

			List<String> query_notmain_ipcs = query.getIpclist();
			query_notmain_ipcs.remove(query_mainipc);

			/*--------------- Testing query ipc without the main ipc ---------------*/
			/*for(String newipclist : query_notmain_ipcs){
			System.out.println(newipclist);
		}*/
			/*-----------------------------------------------------------*/
			
			String removed_qmainipc = query.getIpc().replace(query_mainipc, "");
			System.out.println(queryid + "\t" + query_mainipc +"\t"+query.getIpc() +"\t\t" + removed_qmainipc);			
			System.out.println();

			AnalyseFNs afn = new AnalyseFNs();
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);

			int same_mainipc = 0;
			int same_furtheripc = 0;
			int old_same_furtheripc = 0;
			int none = 0;

			for(String enfn : enfns){
				GetPatentFile gpf = new GetPatentFile();
				String fn_path = gpf.GetPatentPath(enfn);

				QueryGneration doc = new QueryGneration(collection_rootpath + fn_path, 0, 0, 1, 0, 0, 0, true, true);
				String doc_ipc = doc.getIpc();
				List<String> doc_ipc_list = doc.getIpclist();
				System.out.println(enfn + "\t" /*+ doc.getmainIpc()+ "\t"*/ + doc_ipc);
				if(doc_ipc.contains(query_mainipc)){
					same_mainipc++;
				}else{

					if(query_notmain_ipcs.size() == 0){
						if(!doc_ipc.contains(query_mainipc)){none++;}
					}else{
						old_same_furtheripc = same_furtheripc;
						for(String newipclist : query_notmain_ipcs){
							if(doc_ipc.contains(newipclist)){
								same_furtheripc++; 
								break;
							}

						}
						if(old_same_furtheripc == same_furtheripc){none++;}
					}				
				}	
			}
			System.out.println("----------------------------------------------");
			System.out.println("Main\tFurther\tNone\tFNs");
			System.out.println("----------------------------------------------");
			System.out.println(/*queryid + "\t" + query_mainipc + "\t" + */ same_mainipc + "\t" + same_furtheripc + "\t" + none + "\t" + enfns.size());
			System.out.println();
			ps.println(queryid + "\t" + query_mainipc + "\t" + same_mainipc + "\t" + same_furtheripc + "\t" + none + "\t" + enfns.size());
		}
	}
	
	public void TPsAnalyzeIPCOverlapAllQueries() throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/IPCOverlap/ipcoverlp-k100-TPs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration query = new QueryGneration(_qrootpath + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String query_mainipc = query.getmainIpc();	

			List<String> query_notmain_ipcs = query.getIpclist();
			query_notmain_ipcs.remove(query_mainipc);

			/*--------------- Testing query ipc without the main ipc ---------------*/
			/*for(String newipclist : query_notmain_ipcs){
			System.out.println(newipclist);
		}*/
			/*-----------------------------------------------------------*/
			
			String removed_qmainipc = query.getIpc().replace(query_mainipc, "");
			System.out.println(queryid + "\t" + query_mainipc +"\t"+query.getIpc() +"\t" + removed_qmainipc);			
			System.out.println();

//			AnalyseFNs afn = new AnalyseFNs();
			EvaluateResults er = new EvaluateResults();
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			int tps_size = tps.size();
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);

			int same_mainipc = 0;
			int same_furtheripc = 0;
			int old_same_furtheripc = 0;
			int none = 0;

			for(String tp : tps){
				GetPatentFile gpf = new GetPatentFile();
				String fn_path = gpf.GetPatentPath(tp);

				QueryGneration doc = new QueryGneration(collection_rootpath + fn_path, 0, 0, 1, 0, 0, 0, true, true);
				String doc_ipc = doc.getIpc();
				List<String> doc_ipc_list = doc.getIpclist();
				System.out.println(tp + "\t" /*+ doc.getmainIpc()+ "\t"*/ + doc_ipc);
				if(doc_ipc.contains(query_mainipc)){
					same_mainipc++;
				}else{

					if(query_notmain_ipcs.size() == 0){
						if(!doc_ipc.contains(query_mainipc)){none++;}
					}else{
						old_same_furtheripc = same_furtheripc;
						for(String newipclist : query_notmain_ipcs){
							if(doc_ipc.contains(newipclist)){
								same_furtheripc++; 
								break;
							}

						}
						if(old_same_furtheripc == same_furtheripc){none++;}
					}				
				}	
			}
			System.out.println("----------------------------------------------");
			System.out.println("Main\tFurther\tNone\tTPs");
			System.out.println("----------------------------------------------");
			System.out.println(/*queryid + "\t" + query_mainipc + "\t" + */ same_mainipc + "\t" + same_furtheripc + "\t" + none + "\t" + tps_size);
			System.out.println();
			ps.println(queryid + "\t" + query_mainipc + "\t" + same_mainipc + "\t" + same_furtheripc + "\t" + none + "\t" + tps_size);
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		IPCcodeOverlap overlap = new IPCcodeOverlap();
//		overlap.AnalyzeIPCOverlapPerQuery();
//		overlap.FNsAnalyzeIPCOverlapAllQueries();
		overlap.TPsAnalyzeIPCOverlapAllQueries();
	
		
		/*List<String> ipclists = query.getIpclist();
		System.out.println(ipclists.get(0));
		System.out.println(ipclists.get(1));
		System.out.println(ipclists.get(2));*/
	}
}
