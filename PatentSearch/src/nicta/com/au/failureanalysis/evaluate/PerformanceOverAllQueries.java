package nicta.com.au.failureanalysis.evaluate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class PerformanceOverAllQueries {

	public void calculateRecall() throws IOException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/Recall/recallfinal.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();
		
		System.out.println("Query id"+"\t" + "Recall" + "\t" + "EnFn_Recall");
		ps.println("Query id"+"\t" + "Recall" + "\t" + "EnFn_Recall");
		System.out.println("=====================================");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";			
			
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> fns = er.evaluatePatents(queryid, "FN");
//			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			
			int enfnsize = enfns.size();			
		    int tpsize = tps.size();
			int fnsize = fns.size();
			int qrelsize = tpsize + fnsize;
			int trueqrelsize = tpsize + enfnsize;
			
			System.out.println("TPs: " + tps.size()+"\t"+tps);
			System.out.println("FNs: " + fns.size()+"\t"+fns);
			System.out.println("En FNs: " + enfns.size()+"\t"+enfns);
			float recall = (float)tpsize/qrelsize;
			float enfnrecall = (float)tpsize/trueqrelsize;
			System.out.println(queryid + "\t" + recall + "\t" + enfnrecall);
			ps.println(queryid + "\t" + recall + "\t" + enfnrecall);
			System.out.println("--------------");
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		PerformanceOverAllQueries performance = new PerformanceOverAllQueries();
		performance.calculateRecall();
	}
}
