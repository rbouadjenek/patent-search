package nicta.com.au.failureanalysis.GeneralExecuteTopic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nicta.com.au.failureanalysis.pseudorelevancefeedback.CreatPRFquery;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class GeneralParseQuery {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX";
	static String path = "data/CLEF-IP-2010/PAC_test/topics/"; 
	private final boolean filter = true;
	private static final String[] fields = {PatentDocument.Classification, PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims};
	private Map<String, Float> boosts = null;

	public Query parse(String query, String ipcfilter) throws ParseException {

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
			if (query != null && !query.equals("") && boosts.get(fields[i]) != 0) {
				//        		System.err.println("we are here");
				QueryParser qp;

				qp = new QueryParser(Version.LUCENE_48, fields[i], new StandardAnalyzer(Version.LUCENE_48));
				//        		System.out.println(query);
				Query q = qp.parse(query);
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
		CreatPRFquery prfq = new CreatPRFquery();
		GeneralParseQuery pq = new GeneralParseQuery();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");  
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			String PRFquery = prfq.generatePRFQuery(queryid, tau);
			QueryGneration g = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
			String ipcfilter = g.getIpc();

			Query parsedquery = pq.parse(PRFquery, ipcfilter);
			System.out.println(PRFquery);
			System.out.println(parsedquery);
		}
	}
}
