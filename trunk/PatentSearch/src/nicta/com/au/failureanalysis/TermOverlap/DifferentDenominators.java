package nicta.com.au.failureanalysis.TermOverlap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class DifferentDenominators {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	static String queryfield = PatentDocument.Description;
	static String titlefield = PatentDocument.Title;
	static String absfield = PatentDocument.Abstract;
	static String descfield = PatentDocument.Description;
	static String claimsfield = PatentDocument.Claims;
	
	public void mergeHashSets(){

		HashSet<String> list1 = new HashSet<>();
		list1.add("li");
		list1.add("fx");
		list1.add("ws");
		list1.add("mona");
		list1.add("first");
		list1.add("second");
		System.out.print("first list: ");
		for(String l:list1){
			System.out.print(l + "\t");			
		}
		System.out.println();
		HashSet<String> list2 = new HashSet<>();
		list2.add("mona");
		list2.add("first");
		list2.add("second");
		list2.add("third");
		list2.add("forth");
		list2.add("fifth");
		HashSet<String> list3 = new HashSet<>();
		list3.add("fx");
		list3.add("ws");
		HashSet<String> list4 = new HashSet<>();
		list4.add("1");
		list4.add("2");
		
		System.out.println(list1.contains("mona") + "\t"+list1.contains("moa"));
		System.out.print("second list: ");
		for(String l:list2){
			System.out.print(l + "\t");			
		}
		System.out.println();
		//add all the elements in list2 to list
		list1.addAll(list2);
		list1.addAll(list4);
//		list1.removeAll(list2);
//		list1.removeAll(list3);
		for(String l:list1){
			System.out.print(l + "\t");
		}		
	}
	
	public void FNsOverlapSeperateSections(CollectionReader reader) throws IOException {
		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FNs-seperatesections.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test1.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
			int querydocintersection;
			int querytitleoverlap;
			int queryabsoverlap;
			int querydescoverlap;
			int queryclaimsoverlap;
			int SectionsSumOverlap;
			int union;
			int titleunion;
			int absunion;
			int descunion;
			int claimsunion;
			int dminusoverlap = 0;
			int titleminusoverlap;
			int absminusoverlap;
			int descminusoverlap;
			int claimsminusoverlap;
			
			if(n_enfns != 0){

				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("-----------------------------------------------------------------------------------------");
				for (String doc : enfns) { 
					querydocintersection = 0;
					querytitleoverlap = 0;
					queryabsoverlap = 0;
					querydescoverlap = 0;
					queryclaimsoverlap = 0;
					SectionsSumOverlap = 0;
					HashSet<String> titleterms = reader.getDocTerms("UN-"+doc, titlefield);
					HashSet<String> absterms = reader.getDocTerms("UN-"+doc, absfield);
					HashSet<String> descterms = reader.getDocTerms("UN-"+doc, descfield);
					HashSet<String> claimsterms = reader.getDocTerms("UN-"+doc, claimsfield);
					int titlesize = titleterms.size();
					int abssize = absterms.size();
					int descsize = descterms.size();
					int claimssize = claimsterms.size();
					/*descterms.addAll(titleterms);
					descterms.addAll(absterms);
					descterms.addAll(claimsterms);
					int docsize = descterms.size();*/
					/*descterms.removeAll(titleterms);
					descterms.removeAll(absterms);
					descterms.removeAll(claimsterms);*/
					
					
					/*for(Entry<String, Integer> t : qterms.entrySet()){
						boolean exists = descterms.contains(t.getKey());						
						if(exists){
							querydocintersection++;	
						}
					}*/

					for(Entry<String, Integer> t : qterms.entrySet()){
						boolean titleexists = titleterms.contains(t.getKey());	
						boolean absexists = absterms.contains(t.getKey());
						boolean descexists = descterms.contains(t.getKey());
						boolean claimsexists = claimsterms.contains(t.getKey());
						if(titleexists){
							querytitleoverlap++;	
						}
						if(absexists){
							queryabsoverlap++;	
						}
						if(descexists){
							querydescoverlap++;	
						}
						if(claimsexists){
							queryclaimsoverlap++;	
						}
					}
					
					SectionsSumOverlap = querytitleoverlap + queryabsoverlap + querydescoverlap + queryclaimsoverlap;
					
//					dminusoverlap = docsize - querydocintersection;
					titleminusoverlap = titlesize - querytitleoverlap;
					absminusoverlap = abssize - queryabsoverlap;
					descminusoverlap = descsize - querydescoverlap;
					claimsminusoverlap = claimssize - queryclaimsoverlap;
					
					union = querysize + dminusoverlap;
					titleunion = querysize + titleminusoverlap;
					absunion = querysize + absminusoverlap;
					descunion = querysize + descminusoverlap;
					claimsunion = querysize + claimsminusoverlap;

					overlapratio = (float)SectionsSumOverlap/querysize;
//					overlapratio = (float)querydocintersection/querysize;
					uoverlapratio = (float)querydocintersection/union;
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;
					
					/*System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + docsize + "\t" + dminusoverlap + "\t"+ union + "\t" + overlapratio + "\t" + uoverlapratio + "\t" + usum);*/
				}

				avg = (float)sum/n_enfns;
				uavg = (float)usum/n_enfns;
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);
				
				
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}
			
	}
	
	public void FNsOverlapUnionOfSections(CollectionReader reader) throws IOException {
		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FNs-unionsections.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();
		
		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics-test1.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
			int querydocintersection;
			int querytitleoverlap;
			int queryabsoverlap;
			int querydescoverlap;
			int queryclaimsoverlap;
			int SectionSumOverlap;
			int union;
			int dminusoverlap;
			
			if(n_enfns != 0){

				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("-----------------------------------------------------------------------------------------");
				for (String doc : enfns) { 
					querydocintersection = 0;
					querytitleoverlap = 0;
					queryabsoverlap = 0;
					querydescoverlap = 0;
					queryclaimsoverlap = 0;
					SectionSumOverlap = 0;
					HashSet<String> titleterms = reader.getDocTerms("UN-"+doc, titlefield);
					HashSet<String> absterms = reader.getDocTerms("UN-"+doc, absfield);
					HashSet<String> descterms = reader.getDocTerms("UN-"+doc, descfield);
					HashSet<String> claimsterms = reader.getDocTerms("UN-"+doc, claimsfield);
					int titlesize = titleterms.size();
					int abssize = absterms.size();
					int descsize = descterms.size();
					int claimssize = claimsterms.size();
					descterms.addAll(titleterms);
					descterms.addAll(absterms);
					descterms.addAll(claimsterms);
					int docsize = descterms.size();
					/*descterms.removeAll(titleterms);
					descterms.removeAll(absterms);
					descterms.removeAll(claimsterms);*/
					
					
					for(Entry<String, Integer> t : qterms.entrySet()){
						boolean exists = descterms.contains(t.getKey());						
						if(exists){
							querydocintersection++;	
						}
					}

					/*for(Entry<String, Integer> t : qterms.entrySet()){
						boolean titleexists = titleterms.contains(t.getKey());	
						boolean absexists = absterms.contains(t.getKey());
						boolean descexists = descterms.contains(t.getKey());
						boolean claimsexists = claimsterms.contains(t.getKey());
						if(titleexists){
							querytitleoverlap++;	
						}
						if(absexists){
							queryabsoverlap++;	
						}
						if(descexists){
							querydescoverlap++;	
						}
						if(claimsexists){
							queryclaimsoverlap++;	
						}
					}*/
					
					SectionSumOverlap = querytitleoverlap + queryabsoverlap + querydescoverlap + queryclaimsoverlap;
					
					dminusoverlap = docsize - querydocintersection;
					union = querysize + dminusoverlap;

					overlapratio = (float)querydocintersection/querysize;
//					overlapratio = (float)querydocintersection/querysize;
					uoverlapratio = (float)querydocintersection/union;
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;
					
					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + docsize + "\t" + dminusoverlap + "\t"+ union + "\t" + overlapratio + "\t" + uoverlapratio + "\t" + usum);
				}

				avg = (float)sum/n_enfns;
				uavg = (float)usum/n_enfns;
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);
				
				
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}
			
	}
	
	public void FNsTermOverlapNormToUnion(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FNs-DocPlusQuery.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();		

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
			int querydocintersection;
			int union;
			int dminusoverlap;
			
			if(n_enfns != 0){

				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("-----------------------------------------------------------------------------------------");
				for (String doc : enfns) { 
					querydocintersection = 0;
					HashSet<String> terms = reader.getDocTerms("UN-"+doc, queryfield);
					int docsize = terms.size();

					for(Entry<String, Integer> t : qterms.entrySet()){
						boolean exists = terms.contains(t.getKey());						
						if(exists){
							querydocintersection++;	
						}
					}
					dminusoverlap = docsize - querydocintersection;
					union = querysize + dminusoverlap;
//					System.out.println(dminusoverlap+"\t"+union);
					/*terms.removeAll(qterms.keySet());
					System.out.println(terms);
					System.out.println(terms.size());*/
					overlapratio = (float)querydocintersection/querysize;
					uoverlapratio = (float)querydocintersection/union;
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;
					
					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + docsize + "\t" + dminusoverlap + "\t"+ union + "\t" + overlapratio + "\t" + uoverlapratio + "\t" + usum);
				}

				avg = (float)sum/n_enfns;
				uavg = (float)usum/n_enfns;
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);
				
				
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}

			//    		System.out.println(queryid + "\t" + queryfile);
		}

	}
	
	public void TPsTermOverlapNormToUnion(CollectionReader reader) throws IOException{

		/*--------------------------- Write in output file. -Mona ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-TPs-DocPlusQuery.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/

		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();
		
		

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
//			int n_enfns = enfns.size();
			int n_tps = tps.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
			int querydocintersection;
			int union;
			int dminusoverlap;
			
			if(n_tps != 0){

				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("TP patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("-----------------------------------------------------------------------------------------");
				for (String doc : tps) { 
					querydocintersection = 0;
					HashSet<String> terms = reader.getDocTerms("UN-"+doc, queryfield);
					//					System.out.println(terms);
					int docsize;
					if(terms != null){
						docsize = terms.size();
					}else{
						docsize = 0;
					}

					for(Entry<String, Integer> t : qterms.entrySet()){
						if(terms!=null){
							boolean exists = terms.contains(t.getKey());						
							if(exists){
								querydocintersection++;	
							}
						}
					}
					dminusoverlap = docsize - querydocintersection;
					union = querysize + dminusoverlap;
//					System.out.println(dminusoverlap+"\t"+union);
					/*terms.removeAll(qterms.keySet());
					System.out.println(terms);
					System.out.println(terms.size());*/
					overlapratio = (float)querydocintersection/querysize;
					uoverlapratio = (float)querydocintersection/union;
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;
					
					System.out.println(doc + "\t" + querydocintersection + "\t" + querysize + "\t" + docsize + "\t" + dminusoverlap + "\t"+ union + "\t" + overlapratio + "\t" + uoverlapratio + "\t" + usum);
				}

				avg = (float)sum/n_tps;
				uavg = (float)usum/n_tps;
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);
				
				
			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No TP for this query");

			}
		}

	}

	
	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir);
		DifferentDenominators dd = new DifferentDenominators();
//		dd.mergeHashSets();
//		dd.FNsTermOverlapNormToUnion(reader);
//		dd.TPsTermOverlapNormToUnion(reader);
//		dd.FNsOverlapUnionOfSections(reader);
		dd.FNsOverlapSeperateSections(reader);
		
	}
}
