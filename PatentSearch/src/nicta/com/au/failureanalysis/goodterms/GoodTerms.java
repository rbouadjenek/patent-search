package nicta.com.au.failureanalysis.goodterms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.index.TermFreqVector;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

public class GoodTerms {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"/*"data/DocPLusQueryINDEX"*//*"data/QINDEX"*/;

	public void getGoodTerms() throws IOException, ParseException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/score/test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		
		CollectionReader reader = new CollectionReader(indexDir); 
		IndexReader ir = reader.getIndexReader();
		
		
					
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test2.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
			String qUcid = topic.getValue().getUcid();
			String queryid = topic.getKey();
			String queryName = topic.getKey() + "_" + topic.getValue().getUcid();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";
			
			System.out.println("=========================================");
			System.out.println(queryName + "\t" +qUcid);
			
			/*int docid = reader.getDocId("UN-EP-0663270", PatentDocument.FileName);
			ir.getTermVector(docid, field) getTermVectors(b);*/
			
			EvaluateResults er = new EvaluateResults();
			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
			HashSet<String> vocabularies = new HashSet<String>();
			HashMap<String, Integer> TFs = new HashMap<>();
			
			for (String tp : tps) {
//				System.out.println(tp);
				HashMap<String, Integer> termsfreqs = reader.gettermfreqpair("UN-" + tp, PatentDocument.Title);
				for(Entry<String, Integer> t:termsfreqs.entrySet()){
					if(TFs.containsKey(t.getKey())){
						TFs.put(t.getKey(), TFs.get(t.getKey())+t.getValue()/tps.size());
					}else{
						
					}
				}

				
			}
			
			for (String fp : fps) {
				
			}
			
			
			
//			System.out.println(tps);
			for (String doc : tps) {
				vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Title));
				
				/*System.out.println("-----------------------");
				System.out.println("new doc : " + vocabularies.size());*/
				if(vocabularies!=null){
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Description)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Description));}
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract));}
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Claims)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Claims));}
				}
			}
//			System.out.println(vocabularies.size());
//			System.out.println(vocabularies);
			for (String doc : fps) {
				if(vocabularies!=null){
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Title)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Title));}
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Description)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Description));}
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Abstract));}
					if(reader.getDocTerms("UN-" + doc, PatentDocument.Claims)!=null){vocabularies.addAll(reader.getDocTerms("UN-" + doc, PatentDocument.Claims));}
				}
			}
			System.out.println(vocabularies.size());
			System.out.println(vocabularies);
//			for(String v:vocabularies){
				/*for (String tp : tps) {
					System.out.println(tp);
//					vocabularies.contains(o);
					
				}
				
				for (String fp : fps) {
					
				}*/
//			}
		}
		
		
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		GoodTerms gt = new GoodTerms();
		gt.getGoodTerms();
		
	}
}
