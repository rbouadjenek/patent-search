package nicta.com.au.failureanalysis.SectionBasedAnalysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.goodterms.PositiveTermsOverlap;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

import org.apache.commons.lang3.SystemUtils;
import org.apache.lucene.queryparser.classic.ParseException;

public class UsefulTermsInSections {
	static String indexDir = /*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/"data/DocPLusQueryINDEX"/*"data/QINDEX"*/;
	float tsum = 0;
	float asum = 0;
	float dsum = 0;
	float csum = 0;
	int c = 0;
	
	public Map<String, Float> termsInSections(String queryid, String qUcid, float tau) throws IOException, ParseException{
		CollectionReader reader = new CollectionReader(indexDir); 
		String titlefield = PatentDocument.Title;
		String absfield = PatentDocument.Abstract;
		String descfield = PatentDocument.Description;
		String claimsfield = PatentDocument.Claims;
		
		int tcount = 0;
		int acount = 0;
		int dcount = 0;
		int ccount = 0;
		
		int title_size = 0;
		int abs_size = 0;
		int desc_size = 0;
		int claims_size = 0;
		
		PositiveTermsOverlap olap = new PositiveTermsOverlap();
		Map<String, Float> RF_tspairs = olap.getTermsScoresPair(queryid);
		
//		HashMap<String, Integer> tfs_ = new HashMap<String, Integer>();
		HashMap<String, Integer> tfs_title = reader.gettermfreqpair(qUcid, titlefield);
		HashMap<String, Integer> tfs_abs = reader.gettermfreqpair(qUcid, absfield);
		HashMap<String, Integer> tfs_desc = reader.gettermfreqpair(qUcid, descfield);
		HashMap<String, Integer> tfs_claims = reader.gettermfreqpair(qUcid, claimsfield);
		
		title_size = tfs_title.size();
		abs_size = tfs_abs.size();
		desc_size = tfs_desc.size();
		claims_size = tfs_claims.size();
		
		System.out.println("----------------");
		System.out.println(title_size+" "+abs_size+" "+desc_size+" "+claims_size);
		
		int i = 0;
		
		for(Entry<String, Float> rfts : RF_tspairs.entrySet()){
			String rf_term = rfts.getKey(); 
			Float rf_score = rfts.getValue();	
			if(rf_score>tau){
				i++;
			if(tfs_title.containsKey(rf_term)){tcount++;}
			if(tfs_abs.containsKey(rf_term)){acount++;}
			if(tfs_desc.containsKey(rf_term)){dcount++;}
			if(tfs_claims.containsKey(rf_term)){ccount++;}
			}
		}
		
		if(i != 0 && claims_size!=0 && desc_size!=0 && abs_size!=0 && title_size!=0){
			c++;
		System.out.println(queryid+ " " + i +": "+ tcount + " " + acount + " " + dcount + " " + ccount);
		System.out.println(queryid+ " " + i +": "+ (float)tcount/title_size + " " + (float)acount/abs_size + " " + (float)dcount/desc_size + " " + (float)ccount/claims_size);
		tsum = tsum + (float)tcount/title_size ;
		asum = asum + (float)acount/abs_size;
		dsum = dsum + (float)dcount/desc_size;
		csum = csum + (float)ccount/claims_size;
		
		/*tsum = tsum + tcount;
		asum = asum + acount;
		dsum = dsum + dcount;
		csum = csum + ccount;*/
//		System.out.println(queryid+ " " + i +": "+ (float)tcount/i + " " + (float)acount/i + " " + (float)dcount/i + " " + (float)ccount/i);
		}
		System.out.println(c);
		System.out.println(tsum + " " + asum + " " + dsum + " " + csum);
		System.out.println((float)tsum/c + " " + (float)asum/c + " " + (float)dsum/c + " " + (float)csum/c);
		
		return RF_tspairs;
	}
	public static void main(String[] args) throws IOException, ParseException {
		
		float tau = Float.parseFloat(args[0]);
		UsefulTermsInSections ts = new UsefulTermsInSections();
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-omit-PAC-1094.xml");
//		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-tt.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
			Map<String, Float> rfterms = ts.termsInSections(queryid, qUcid, tau);
//			System.out.println(rfterms);
			}
	}
}
