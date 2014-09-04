package nicta.com.au.failureanalysis.TermOverlap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;

/**
 * @author mona
 *
 * This calculates the average term overlap between the query and its false negative patents.
 * Just a reminder: we consider FN patents which are complete and the original language is in English. 
 * In other words, we ignore FN patents due to missing part or being in another language. 
 */
public class CalculateTermOverlap {
	
	static String path = "data/CLEF-IP-2010/PAC_test/topics/";
	static String _queryid = "PAC-1216"/*"PAC-1379"*/;
	static String queryfile = "PAC-1216_EP-1749865-A1.xml"/*"PAC-1379_EP-1304229-A2.xml"*/;

	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	
	static String field = /* PatentDocument.Classification */PatentDocument.Description;
	
	public void TermOverlapPerQuery(CollectionReader reader, String queryid) throws IOException{
		
		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> enfns = afn.getEnglishFNs(queryid);
		int n_enfns = enfns.size();

		QueryGneration query = new QueryGneration(path + queryfile, 0, 1, 0, 0, 0, 0, true, true);
		Map<String, Integer> qterms = query.getSectionTerms(field);

		String doc = "EP-0603697";
		//		for (String doc : enfns) { 
		int qtermsindoc = 0;
		int i=0;
		for(Entry<String, Integer> t : qterms.entrySet()){
			i++;
			boolean x=reader.getTFreq(field, t.getKey(), doc)>0;
			System.out.println("[" + i + "] " + t.getKey() + "\t" + t.getValue() + "\t" + reader.getTFreq(field, t.getKey(), doc) + "\t" + x + "\t");
			if(reader.getTFreq(field, t.getKey(), doc)>0){
				qtermsindoc++;					
			}				

			//			}
			
		}
		int querysize = qterms.size();
		 float overlapratio = (float)qtermsindoc/querysize;
		System.out.println(qtermsindoc + "\t" + querysize + "\t" + overlapratio);
		
	}

	public static void main(String[] args) throws IOException {
		
		CollectionReader reader = new CollectionReader(indexDir); 
		
		CalculateTermOverlap cto = new CalculateTermOverlap();
		cto.TermOverlapPerQuery(reader, _queryid);
		
	}
}
