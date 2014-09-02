package nicta.com.au.failureanalysis.evaluate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.utility.GetPatentFile;
import nicta.com.au.patent.pac.evaluation.QrelsInMemory;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class AnalyseFNs {
	
	public ArrayList<String> getEnglishFNs(String queryid) throws IOException{
		
		String _rootpath  = "/media/mona/MyProfesion/";
		
		ArrayList<String> enFNs = new ArrayList<>();
		
		EvaluateResults er = new EvaluateResults();
		ArrayList<String> fns = er.evaluatePatents(queryid, "FN");
		for (String fn : fns) { 

			GetPatentFile gpf = new GetPatentFile();
			String path = gpf.GetPatentPath(fn);

			QueryGneration query = new QueryGneration(_rootpath + path, 0, 0, 1, 0, 0, 0, true, true);

//						System.out.println(" [" + k + "] [" + fn + "], " + query.getDescLangOrMising()); 

			if(query.getDescLangOrMising().equals("en")){
				enFNs.add(fn);
//				en++;
				}
//			if(query.getDescLangOrMising().equals("non-en")){nen++;}
//			if(query.getDescLangOrMising().equals("missing")){miss++;}
		}
		
		
		return enFNs;		
	}
	
	public static void main(String[] args) throws IOException {
		
		/*-------------- Test English FN patents ---------------*/
		
		String _queryId = "PAC-1305"/*"PAC-1604"*/ /*"PAC-1142"*//*"PAC-1087"*//*"PAC-1379"*//*"PAC-1035"*//*"PAC-1012"*//*"PAC-544"*/ /*"PAC-825" *//*"PAC-1149"*//*"PAC-1460"*/;
		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> efns = afn.getEnglishFNs(_queryId);
		
		System.out.print(_queryId + ":");
		System.out.println(" List of FN patents due to term mismatch (k=100):");
		int count = 0;
		if(efns.size()==0){System.out.println("There is no FN patent: " + 0);}
		for(String efn : efns){
			count++;
			System.out.print(" [" + count + "] " + efn);
			
		}
		
		/*--------------------------- Write in outputfile. ------------------------*/
		/*String outputfile = "./output/AnalysisFNs/AnalyzeFNs-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		------------------------------------------------------------------------

		String _rootpath  = "/media/mona/MyProfesion/";
//		String _queryId = "PAC-544" "PAC-825" "PAC-1149""PAC-1460";
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		QrelsInMemory qrels = new QrelsInMemory("data/qrel/PAC_test_rels.txt");
		
		EvaluateResults er = new EvaluateResults();
		
				
		System.out.println("------------------------------------------------------------------------------------------");
		System.out.println(" No." + "\t" + "QueryId" + "\t\t" + "En(%)" + "\t" + "N-En(%)" + "\t" + "miss(%)" + "\t" + "EN" + "\t" + "N-EN" + "\t" + "miss" + "\t" + "FN-size" + "\t" + "qrel" + "\t" + "FN(%)");
		System.out.println("------------------------------------------------------------------------------------------");
		
		ps.println("----------------------------------------------------------------------------------");
		ps.println(" No." + "\t" + "QueryId" + "\t\t" + "En(%)" + "\t" + "N-En(%)" + "\t" + "miss(%)" + "\t" + "EN" + "\t" + "N-EN" + "\t" + "miss" + "\t" + "FN-size" + "\t" + "qrel" + "\t" + "FN(%)");
		ps.println("----------------------------------------------------------------------------------");
		
		int n=0;
		for(String  q:topics.getTopics().keySet()){
			n++;
			ArrayList<String> fns = er.evaluatePatents(q_queryId, "FN");
			
	        int num_rel_patents = qrels.getNumberOfRelevantPatent(q);


			int en = 0;
			int nen = 0;
			int miss = 0;
			int k = fns.size(); 
			float error_percentage = (float)k/num_rel_patents;
			for (String fn : fns) { 

				GetPatentFile gpf = new GetPatentFile();
				String path = gpf.GetPatentPath(fn);

				QueryGneration query = new QueryGneration(_rootpath + path, 0, 0, 1, 0, 0, 0, true, true);

				//			System.out.println(" [" + k + "] [" + fn + "], " + query.getDescLangOrMising()); 

				if(query.getDescLangOrMising().equals("en")){en++;}
				if(query.getDescLangOrMising().equals("non-en")){nen++;}
				if(query.getDescLangOrMising().equals("missing")){miss++;}
			}
			//		System.out.println("QueryId" + "\t\t" + "En%" + "\t\t" + "Non-En%" + "\t\t" + "missing%" + "\t" + "FN size");
			if(k != 0){
				System.out.println("(" + n + ")\t" + q_queryId + "\t" + (float)en/k + "\t" + (float)nen/k + "\t" + (float)miss/k + "\t" + en + "\t" + nen + "\t" + miss + "\t" + k + "\t" + num_rel_patents+ "\t" + error_percentage);
				ps.println("(" + n + ")\t" + q + "\t" + (float)en/k + "\t" + (float)nen/k + "\t" + (float)miss/k + "\t" + en + "\t" + nen + "\t" + miss + "\t" + k+ "\t" + num_rel_patents + "\t" + error_percentage);
			}else {
				System.out.println("(" + n + ")\t" + q + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + k + "\t" + num_rel_patents + "\t" + error_percentage);
				ps.println("(" + n + ")\t" + q + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + k+ "\t" + num_rel_patents + "\t" + error_percentage);
			}
		}*/
	}

}
