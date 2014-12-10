package nicta.com.au.failureanalysis.optimalquery;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.ClassificationIpcr;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * @author mona
 *
 */
public class CreateOptimalQuery {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX";
	static String path = "data/CLEF-IP-2010/PAC_test/topics/"; 
	private final boolean filter = true;
	private static final String[] fields = {PatentDocument.Classification, PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims};
	private Map<String, Float> boosts = null;
	
	public String generateOptQuerySize(String queryid, int querysize) throws IOException, ParseException{
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		Map<String, Float> tspairs = olap.getTermsScoresPair(queryid);
		
		String optimal_query = "";
		int k = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String termkey = ts.getKey();
			Float scorevalue = ts.getValue();
			k++;
			if(k <= querysize && scorevalue>0){	
				
				if (!Functions.isNumeric(termkey) && !Functions.isSpecialCahr(termkey)) {
//					optimal_query += termkey + "^" + tsvalue + " ";
					optimal_query += termkey + "^" + 1 + " ";
				}				
			}
		}
		return optimal_query;
	}
	
	public String generateOptimalQuery(String queryid, float tau) throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------
		String outputfile = "./output/optimalquery/results-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);*/
		/*-------------------------------------------------------------------------------*/

		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		
		Map<String, Float> tspairs = olap.getTermsScoresPair(queryid);
		//				HashMap<String, Float> tshashes = new HashMap<>();
		//				tshashes.putAll(tspairs);

//		System.out.println(/*tspairs.size() + "\t" + */tspairs );
		String optimal_query = "";
		int i = 0;
		for(Entry<String, Float> ts : tspairs.entrySet()){
			String tskey = ts.getKey();
			Float tsvalue = ts.getValue();
			if(tsvalue > tau){	
				i++;
				if (!Functions.isNumeric(tskey) && !Functions.isSpecialCahr(tskey)) {
					optimal_query += tskey + "^" + tsvalue + " ";
//					optimal_query += tskey + "^" + 1 + " ";
				}				
			}
		}
		
//		System.err.println(i);
		if(i == 0){
			System.err.println("There is no optimal query");
			return null;
		}
//		System.out.println(optimal_query);
		return optimal_query;
	}
	
	public Query parse(String optquery, String ipcfilter) throws ParseException {
		
		boosts = new HashMap<>();
		boosts.put(PatentDocument.Classification, new Float(0));
		boosts.put(PatentDocument.Title, new Float(1));
		boosts.put(PatentDocument.Abstract, new Float(1));
		boosts.put(PatentDocument.Description, new Float(1));
		boosts.put(PatentDocument.Claims, new Float(1));

        BooleanQuery bQuery = new BooleanQuery();
        BooleanQuery bQueryFields = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        
        /*---------------------------------------- This is another way for multi field search -------------------------------*/
       /* if (optquery != null && !optquery.equals("")) {
        	MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
        			Version.LUCENE_48, 
        			new String[]{PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims},
        			new StandardAnalyzer(Version.LUCENE_48));

        	Query q = queryParser.parse(optquery);
        	q.setBoost(1);
        	if (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0) {
        		bQueryFields.add(q, BooleanClause.Occur.SHOULD);
        	}
        }*/
        /*-----------------------------------------------------------------------------------------------------------------*/
        
        /*---------------------------------------- This is Reda's code for multi field  -------------------------------*/
        for (int i = 1; i < fields.length; i++) {
        	if (optquery != null && !optquery.equals("") && boosts.get(fields[i]) != 0) {
//        		System.err.println("we are here");
        		QueryParser qp;

        		qp = new QueryParser(Version.LUCENE_48, fields[i], new StandardAnalyzer(Version.LUCENE_48));
//        		System.out.println(optquery);
        		Query q = qp.parse(optquery);
        		q.setBoost(boosts.get(fields[i]));
        		if (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0) {
        			bQueryFields.add(q, BooleanClause.Occur.SHOULD);
        		}
        	}
        }
        /*-----------------------------------------------------------------------------------------------------------------*/
        if (filter) {
            Query q = new QueryParser(Version.LUCENE_48, fields[0], new StandardAnalyzer(Version.LUCENE_48)).parse(ipcfilter);
            q.setBoost(boosts.get(fields[0]));
            bQuery.add(q, BooleanClause.Occur.MUST);
        }
        bQuery.add(bQueryFields, BooleanClause.Occur.MUST);
        return bQuery;
    }
	
	

	public static void main(String[] args) throws IOException, ParseException {
		int tau = 0;
		
		CreateOptimalQuery oq = new CreateOptimalQuery();
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");  /*test1.xml");*/
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			String optquery = oq.generateOptimalQuery(queryid, tau);
			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();
			if(optquery!=null){
				Query q = oq.parse(optquery, ipcfilter);
				System.out.println(queryid +"\t"+ q);
			}else{System.err.println("no optimal query for this query paetent");}
		}
	}
}

