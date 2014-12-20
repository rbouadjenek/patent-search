/*package nicta.com.au.failureanalysis.weighting;

public class ScoreQueryTermsTPs {

}*/
package nicta.com.au.failureanalysis.weighting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.failureanalysis.search.CollectionSearcher;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.search.PACSearcher;

public class ScoreQueryTermsFNs {

	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	static String field = PatentDocument.Description;
	String similarity = /*"tfidf"*/"lmdir"/*"bm25ro"*/;
	int topK =1000000;


	public void getTopQTermsFNs(CollectionReader reader) throws IOException, ParseException{
		
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/weighting/scoreFNs.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		ps.println("=============================================================");
		ps.println( "Query Id/Name    " + "\t" + "Q Topscore" + "\t" + "olap" + "\t" + "Avg. D Score");
		ps.println("=============================================================");

		PACSearcher searcher = new PACSearcher(indexDir, similarity, topK);
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();
		
		CollectionSearcher collsearcher = new CollectionSearcher(indexDir, similarity, topK);
//		System.out.println(collsearcher.getscore(field, "hammer", "EP-0426633"));

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int size = enfns.size();
			float sumtopqtermsscore=0;
			
//			CollectionReader reader = new CollectionReader(indexDir);
			int qindxId = reader.getDocId(qUcid, PatentDocument.FileName);
			IndexSearcher is = searcher.getIndexSearch();
			
			if(size != 0){
			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(field);
			HashMap<String, Float> termscores = new HashMap<String, Float>();

//			System.out.println(qterms.size());
			System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			for(Entry<String, Integer> t : qterms.entrySet()){
				String qterm = t.getKey();				
//				IndexSearcher is = searcher.getIndexSearch();
/*--------------------------------- Search over all fields ----------------------------*/
				/*MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
						Version.LUCENE_48, 
		                new String[]{PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims},
		                new StandardAnalyzer(Version.LUCENE_48));

				Query q = queryParser.parse(qterm);*/
/*------------------------------------------------------------------------------------*/		
				TermQuery q = new TermQuery(new Term(field, qterm));
		
				Explanation explain = is.explain(q, qindxId);
				float qscore = explain.getValue();
				termscores.put(qterm, qscore);
				
			}

			System.out.println("=========================================");
			ValueComparator bvc =  new ValueComparator(termscores);
			TreeMap<String,Float> sortedtermscores = new TreeMap<String,Float>(bvc);

			//	        System.out.println("unsorted map: " + termscores);			
			sortedtermscores.putAll(termscores);
			//	        System.out.println("sorted map: " + sortedtermscores);
			float avg = 0;
			int ol =0;
			float sumdoctermscore = 0;
			for (String doc : enfns) {
//				System.out.println(collsearcher.getscore(field, "hammer", "EP-0426633"));
				int i=0;
				int j=0;
				sumtopqtermsscore = 0;
				sumdoctermscore = 0;
				HashSet<String> dterms = reader.getDocTerms("UN-" + doc, field);
				if(reader.getDocTerms("UN-" + doc, PatentDocument.Title)!=null){dterms.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Title));}
				if(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract)!=null){dterms.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract));}
				if(reader.getDocTerms("UN-" + doc, PatentDocument.Claims)!=null){dterms.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Claims));}
				System.out.println("----------------------------------------");
				System.out.println(doc/*+"\t"+dterms*/);
				System.out.println("----------------------------------------");
				for( Entry<String, Float> topscoreterm : sortedtermscores.entrySet()){
					String topterm = topscoreterm.getKey();
					Float toptermscore = topscoreterm.getValue();
					i++;
					if(i <= 100){						
						sumtopqtermsscore = sumtopqtermsscore + toptermscore;
						if(dterms!=null && dterms.contains(topterm)){
							j++;
//							float doctermscore = collsearcher.getscoreinMultipleFields(topterm, doc);
							int dindxId = reader.getDocId("UN-"+doc, PatentDocument.FileName);
							
							MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
									Version.LUCENE_48, 
									new String[]{PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims},
									new StandardAnalyzer(Version.LUCENE_48));

							Query dq = queryParser.parse(topterm);
							
							
//							TermQuery dq = new TermQuery(new Term(field, topterm));
							Explanation explain = is.explain(dq, dindxId);
							float doctermscore = explain.getValue();
						
							System.out.println("["+ j + "]\t" + topterm + "\t" + toptermscore + "\t" 
									+ doctermscore);
							sumdoctermscore = sumdoctermscore + doctermscore;
						}
//						System.out.println("["+ i + "]\t" + topterm + "\t" + toptermscore);	
					}			
				}
				System.out.println(sumtopqtermsscore + "\t" + j + "\t" + sumdoctermscore );
				avg = avg + sumdoctermscore;
				ol = ol + j;
			}
			avg = avg/size;
			ol = ol/size;
//			System.out.println(avg);
			System.out.println("=============================================================");
			System.out.println( "Query Id/Name        " + "\t" + "Q Topscore" + "\t" + "olap" + "\t" + "Avg. D Score");
			System.out.println("=============================================================");
			System.out.println( queryName + "\t" + sumtopqtermsscore + "\t" + ol + "\t" + avg);
			ps.println( queryName + "\t" + sumtopqtermsscore + "\t" + ol + "\t" + avg);
			System.out.println("=============================================================");
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}			
		}		
	}

	public static void main(String[] args) throws IOException, ParseException {

		CollectionReader reader = new CollectionReader(indexDir); 

		ScoreQueryTermsFNs toptrtms = new ScoreQueryTermsFNs();
		toptrtms.getTopQTermsFNs(reader);

	}
}

