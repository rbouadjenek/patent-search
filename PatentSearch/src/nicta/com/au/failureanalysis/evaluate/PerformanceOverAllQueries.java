package nicta.com.au.failureanalysis.evaluate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class PerformanceOverAllQueries {

	EvaluateResults er = new EvaluateResults();
	AnalyseFNs afn = new AnalyseFNs();
	QueryAndPatents qps = new QueryAndPatents();
	
	public void calculateRecall() throws IOException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/Performance/recallfinal.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
//		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();
		
		System.out.println("Query id"+"\t" + "Recall" + "\t" + "EnFn_Recall");
		ps.println("Query id"+"\t" + "Recall" + "\t" + "EnFn_Recall");
		System.out.println("=====================================");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
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
	
	public void calculateAvgPrecision() throws IOException {
		
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/Performance/test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		TreeMap<String,Integer> TPs_ranks_sorted = null;
		
		System.out.println("QueryId"+"\t" + "EnFn_Avg.P" + "\t" + "Avg. P");
		ps.println("QueryId"+"\t" + "EnFn_Avg.P" + "\t" + "Avg. P");
		System.out.println("=====================================");
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";			
			
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> fns = er.evaluatePatents(queryid, "FN");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			
			int enfnsize = enfns.size();			
		    int tpsize = tps.size();
			int fnsize = fns.size();
			int qrelsize = tpsize + fnsize;
			int excludedqrelsize = tpsize + enfnsize;
			
			HashMap<String, HashMap<String, String>> _docranks = qps.GetQueryPatentsRanks("output/results/results-lmdir-desc-100.txt");
			/*System.out.println("--------");
			System.out.println(queryid);
			System.out.println("--------");*/
			Map<String, Integer> TPs_ranks = new HashMap<String, Integer>();
//			Map<String, String> treeMap = new TreeMap<String, String>();
			for(String tp:tps){
				TPs_ranks.put(tp, Integer.parseInt(_docranks.get(queryid).get(tp)));
				
//				System.out.println(tp+"\t"+_docranks.get(queryid).get(tp));
			}
			ValueComparator bvc =  new ValueComparator(TPs_ranks);
			TPs_ranks_sorted = new TreeMap<String,Integer>(bvc);
			TPs_ranks_sorted.putAll(TPs_ranks);
//			System.out.println(TPs_ranks_sorted);
			
			int j = 0;
			float sum = 0;
			for( Entry<String, Integer> tp_rank : TPs_ranks_sorted.entrySet()){
				j++;
				Integer rank = tp_rank.getValue();
				float prec_at = (float)j/rank;
//				System.out.println(rank +"\t"+prec_at);
				sum = sum + prec_at;
			}
			float avg_precision_excluded = (float)sum/excludedqrelsize;
			float avg_precision = (float)sum/qrelsize; 
			System.out.println(queryid + "\t" + avg_precision_excluded + "\t" + avg_precision); 
			ps.println(queryid + "\t" + avg_precision_excluded + "\t" + avg_precision); 
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		PerformanceOverAllQueries performance = new PerformanceOverAllQueries();
//		performance.calculateRecall();
		performance.calculateAvgPrecision();
	}
}

/*-------------- This class is used to sort a hashmap --------------*/

class ValueComparator implements Comparator<String> {

	Map<String, Integer> base;
	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
//		if (base.get(a) >= base.get(b)) {
		if (base.get(b) >= base.get(a)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
