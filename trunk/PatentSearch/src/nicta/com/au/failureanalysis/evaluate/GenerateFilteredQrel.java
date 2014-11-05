package nicta.com.au.failureanalysis.evaluate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.queryparser.classic.ParseException;

public class GenerateFilteredQrel {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/DocPLusQueryINDEX"/*"data/QINDEX"*/;

	AnalyseFNs afn = new AnalyseFNs();

	public void writeFilteredQrel() throws IOException, ParseException{
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			//			System.out.println(queryid);
			ArrayList<String> filteredfns = afn.getFilteredFNs(queryid, queryfile);
//			System.out.println(queryid +" "+ filteredfns);
			EvaluateResults er = new EvaluateResults();
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			
			if(filteredfns.size()!=0 || tps.size() != 0){
				
				for(String tp:tps){
					System.out.println(queryid + " 0 " + tp + " 1");
				}
				
				for(String f:filteredfns){
					System.out.println(queryid + " 0 " + f + " 1");
				}
			}else{
				System.out.println(queryid + " 0 " + "XXXXXXXXXX 1");
			}
		}
	}

	public static void main(
			String[] args) throws IOException, ParseException {
		GenerateFilteredQrel gfq = new GenerateFilteredQrel();
		gfq.writeFilteredQrel();
	}
}
