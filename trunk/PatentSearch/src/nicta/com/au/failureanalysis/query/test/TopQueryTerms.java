package nicta.com.au.failureanalysis.query.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.search.PACSearcher;

public class TopQueryTerms {

	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = /*"data/DocPLusQueryINDEX"*/"data/QINDEX";
	static String field = PatentDocument.Description;
	String similarity = /*"tfidf"*/"lmdir"/*"bm25ro"*/;
	int topK =10000;


	public void getTopQTerms(/*CollectionReader reader*/) throws IOException{

//		TopQueryTerms toptertms = new TopQueryTerms();

		PACSearcher searcher = new PACSearcher(indexDir, similarity, topK);

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);
			HashMap<String, Float> termscores = new HashMap<String, Float>();


			System.out.println(qterms.size());
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
						//						System.out.println(/*doc.get(PatentDocument.FileName) + "\t" + */qterm + "\t" + s/*coreDoc.score*/ /*+ "\t" + scoreDoc.doc*/);
						termscores.put(qterm, s);

					}					
				}
			}
			
			//			Map sortedtermscores = toptertms.sortByValue(termscores);
			System.out.println("=========================================");
			ValueComparator bvc =  new ValueComparator(termscores);
			TreeMap<String,Float> sortedtermscores = new TreeMap<String,Float>(bvc);

			//	        System.out.println("unsorted map: " + termscores);
			sortedtermscores.putAll(termscores);
			//	        System.out.println("sorted map: "+sortedtermscores);
			for( Entry<String, Float> ts:sortedtermscores.entrySet()){
				System.out.println(ts.getKey()+"\t"+ts.getValue());
			}			
		}
	}

	public static void main(String[] args) throws IOException {

		TopQueryTerms toptrtms = new TopQueryTerms();
		toptrtms.getTopQTerms();

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
