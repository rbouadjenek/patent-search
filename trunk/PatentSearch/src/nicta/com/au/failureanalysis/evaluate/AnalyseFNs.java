package nicta.com.au.failureanalysis.evaluate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.utility.GetPatentFile;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class AnalyseFNs {
	
	public static void main(String[] args) throws IOException {
		
		/*--------------------------- Write in outputfile. ------------------------*/
		String outputfile = "./output/results/AnalyzeFNs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*------------------------------------------------------------------------*/

		String _rootpath  = "/media/mona/MyProfesion/";
		String _queryId = "PAC-544" /*"PAC-825"*/ /*"PAC-1149"*//*"PAC-1460"*/;
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		
		EvaluateResults er = new EvaluateResults();
		
		System.out.println("-----------------------------------------------------------------------");
		System.out.println(" No." + "\t" + "QueryId" + "\t\t" + "En(%)" + "\t" + "N-En(%)" + "\t" + "miss(%)" + "\t" + "FN size");
		System.out.println("-----------------------------------------------------------------------");
		
		ps.println("-----------------------------------------------------------------------");
		ps.println(" No." + "\t" + "QueryId" + "\t\t" + "En(%)" + "\t" + "N-En(%)" + "\t" + "miss(%)" + "\t" + "FN size");
		ps.println("-----------------------------------------------------------------------");
		
		int n=0;
		for(String  q:topics.getTopics().keySet()){
			n++;
			ArrayList<String> fns = er.evaluatePatents(q/*_queryId*/, "FN");

			int en = 0;
			int nen = 0;
			int miss = 0;
			int k = 0; 
			for (String fn : fns) { 
				k++; 

				GetPatentFile gpf = new GetPatentFile();
				String path = gpf.GetPatentPath(fn);

				QueryGneration query = new QueryGneration(_rootpath + path, 0, 0, 1, 0, 0, 0, true, true);

				//			System.out.println(" [" + k + "] [" + fn + "], " + query.getDescLangOrMising()); 

				if(query.getDescLangOrMising().equals("en")){en++;}
				if(query.getDescLangOrMising().equals("non-en")){nen++;}
				if(query.getDescLangOrMising().equals("missing")){miss++;}
			}
			//		System.out.println("QueryId" + "\t\t" + "En%" + "\t\t" + "Non-En%" + "\t\t" + "missing%" + "\t" + "FN size");
			if(fns.size() != 0){
				System.out.println("(" + n + ")\t" + q/*_queryId*/ + "\t" + (float)en/k + "\t" + (float)nen/k + "\t" + (float)miss/k + "\t" + fns.size());
				ps.println("(" + n + ")\t" + q/*_queryId*/ + "\t" + (float)en/k + "\t" + (float)nen/k + "\t" + (float)miss/k + "\t" + fns.size());
			}else {
				System.out.println("(" + n + ")\t" + q/*_queryId*/ + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + fns.size());
				ps.println("(" + n + ")\t" + q/*_queryId*/ + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + fns.size());
			}
		}
	}

}
