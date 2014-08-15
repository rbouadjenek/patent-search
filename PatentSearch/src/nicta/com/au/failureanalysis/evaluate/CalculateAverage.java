package nicta.com.au.failureanalysis.evaluate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;

public class CalculateAverage {
	
	public static void main(String[] args) throws IOException {
		
		String path = "data/CLEF-IP-2010/PAC_test/topics/";
		String queryid = "PAC-825";
		String queryfile = "PAC-825_EP-1267369-A2.xml";
		
		/*String queryid = "PAC-544";
		String queryfile = "PAC-544_EP-1405720-A1.xml";
		*/
		String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
		String field = /* PatentDocument.Classification */PatentDocument.Description;
		
		QueryGneration query = new QueryGneration(path + queryfile, 0, 1, 0, 0, 0, 0, true, true);
    	Map<String, Integer> terms = query.getSectionTerms(/*"title"*//*"abstract"*/"description"/*"claims"*/);
    	
    	EvaluateResults er = new EvaluateResults();
    	ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
		ArrayList<String> fns = er.evaluatePatents(queryid, "FN");
		
		CollectionReader reader = new CollectionReader(indexDir);	
		
				
    	int count = 0;
    	for(Entry<String, Integer> t : terms.entrySet()){
    		int avg = 0;
    		count++;
    		System.out.println(count + " " + t.getKey() + " " + t.getValue());
    		for (String tp : tps) { 
    			avg = avg + reader.getTFreq(field, t.getKey(), tp);
    			
    		}
    		System.out.println((float)avg/tps.size());
    	}
    	
    	 for (String tp : tps) { 
	  			  
			  System.out.print("[" + tp + "], "); 
			  
			 		
				
//				int b = reader.getTFreq(field, term, filename);
//				System.out.println(b);
			  }	
    	
	}	

}
