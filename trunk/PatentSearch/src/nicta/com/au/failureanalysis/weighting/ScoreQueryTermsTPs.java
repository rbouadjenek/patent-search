/*package nicta.com.au.failureanalysis.weighting;

public class ScoreQueryTermsTPs {

}*/
package nicta.com.au.failureanalysis.weighting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.failureanalysis.search.CollectionSearcher;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.search.PACSearcher;

public class ScoreQueryTermsTPs {

	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	static String field = PatentDocument.Description;
	String similarity = /*"tfidf"*/"lmdir"/*"bm25ro"*/;
	int topK =1000000;


	public void getTopQTerms(CollectionReader reader) throws IOException{

		PACSearcher searcher = new PACSearcher(indexDir, similarity, topK);
		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();
		
		CollectionSearcher collsearcher = new CollectionSearcher(indexDir, similarity, topK);
		System.out.println(collsearcher.getscore(field, "hammer", "EP-0426633"));

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			int tps_size = tps.size();
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			
			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);
			HashMap<String, Float> termscores = new HashMap<String, Float>();

			System.out.println(qterms.size());
			System.out.println(topic.getValue().getUcid());
			for(Entry<String, Integer> t : qterms.entrySet()){
				String qterm = t.getKey();				
				IndexSearcher is = searcher.getIndexSearch();

				TermQuery q = new TermQuery(new Term(field, qterm));

				TopDocs topdocs = is.search(q, topK);
				/*System.err.println("'" + qterm + "'"
						+ " appeared in " + topdocs.totalHits
						+ " documents:");*/

				for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
					Document doc = is.doc(scoreDoc.doc);
					//					System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName) + " " + scoreDoc.score);
					float s = scoreDoc.score; 
					if(doc.get(PatentDocument.FileName).contains(topic.getValue().getUcid())){
						//						System.out.println(doc.get(PatentDocument.FileName) + "\t" + topic.getValue().getUcid());
						//						System.out.println(/*doc.get(PatentDocument.FileName) + "\t" + */qterm + "\t" + s/*coreDoc.score*/ /*+ "\t" + scoreDoc.doc*/);
						termscores.put(qterm, s);

					}					
				}
			}

			System.out.println("=========================================");
			ValueComparator bvc =  new ValueComparator(termscores);
			TreeMap<String,Float> sortedtermscores = new TreeMap<String,Float>(bvc);

			//	        System.out.println("unsorted map: " + termscores);			
			sortedtermscores.putAll(termscores);
			//	        System.out.println("sorted map: " + sortedtermscores);
			float avg = 0;
			for (String doc : tps) {
//				System.out.println(collsearcher.getscore(field, "hammer", "EP-0426633"));
				int i=0;
				int j=0;
				float sumtopqtermsscore = 0;
				float sumdoctermscore = 0;
				HashSet<String> dterms = reader.getDocTerms("UN-" + doc, field);
				System.out.println(doc+"\t"+dterms);
				for( Entry<String, Float> topscoreterm : sortedtermscores.entrySet()){
					String topterm = topscoreterm.getKey();
					Float toptermscore = topscoreterm.getValue();
					i++;
					if(i <= 100){						
						sumtopqtermsscore = sumtopqtermsscore + toptermscore;
						if(dterms!=null && dterms.contains(topterm)){
							j++;
							float doctermscore = collsearcher.getscore(field, topterm, doc);
							System.out.println("["+ j + "]\t" + topterm + "\t" + toptermscore + "\t" 
									+ doctermscore);
							sumdoctermscore = sumdoctermscore + doctermscore;
						}						
					}			
				}
				System.out.println(sumtopqtermsscore + "\t" + j + "\t" + sumdoctermscore );
				avg = avg + sumdoctermscore;
			}
			avg = avg/tps_size;
			System.out.println(avg);
		}
	}

	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir); 

		ScoreQueryTermsTPs toptrtms = new ScoreQueryTermsTPs();
		toptrtms.getTopQTerms(reader);

	}
}

class ValueComparator implements Comparator<String> {

	Map<String, Float> base;
	public ValueComparator(Map<String, Float> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
