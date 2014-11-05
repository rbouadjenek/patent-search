package nicta.com.au.failureanalysis.evaluate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class EvaluateResults {

	public String _queryID = null;

	public ArrayList<String> evaluatePatents(String queryID, String select)
			throws IOException {

		ArrayList<String> TPs = new ArrayList<>();
		ArrayList<String> FPs = new ArrayList<>();
		ArrayList<String> FNs = new ArrayList<>();

		String resultsfile = "output/results/" + /*"results-lmdir-desc-1000.txt"*/ 
				/*"results-lmdir-desc-200.txt"*/ 
				"results-lmdir-desc-100.txt"
				;

		_queryID = queryID.toUpperCase();

		QueryAndPatents qps = new QueryAndPatents();

		HashMap<String, ArrayList<String>> _reldocs = qps
				.GetQueryPatents("data/qrel/"
						+ "filtered-qrelfile-original.txt"
						/*+ "PAC_test_rels.txt"*/);

		HashMap<String, ArrayList<String>> _retdocs = qps
				.GetQueryPatents(resultsfile);

		ArrayList<String> relevantdocs = _reldocs.get(_queryID);
		ArrayList<String> retrieveddocs = _retdocs.get(_queryID);
		/*System.out.println(relevantdocs.size() + "  " + relevantdocs);
		System.out.println(retrieveddocs.size() + "  " + retrieveddocs);*/

		/*if(retrieveddocs != null){
			for (String ret : retrieveddocs) {
				for (String rel : relevantdocs) {

					if (ret.equals(rel)) {
						// System.out.println(" matched: " + rel);
						TPs.add(rel);
						break;
					}
				}
			}
		}*/

		if(retrieveddocs != null){
			for (String rel : relevantdocs) {
				if(retrieveddocs.contains(rel)){TPs.add(rel);
				}					
			}
		}

		if(TPs.size() != 0){
			for (String tp : TPs) {
				retrieveddocs.remove(tp);
				FPs = retrieveddocs;
			}
		}else{
			FPs = retrieveddocs;		
		}

		if(TPs.size() != 0){
			for (String tp : TPs) {
				relevantdocs.remove(tp);
				FNs = relevantdocs;
			}
		}else{if(!relevantdocs.contains("XXXXXXXXXX")){FNs = relevantdocs;}
		}


		if (select.equals("TP")) {
			return TPs;
		} else {
			if (select.equals("FP")) {
				return FPs;
			} else {
				return FNs;
			}
		}

	}

	public static void main(String[] args) throws IOException {

		EvaluateResults er = new EvaluateResults();
		//		er.evaluatePatents("PAC-1149", "TP");

		String queryid = /*"PAC-1149"*//*"PAC-544"*//*"PAC-825"*//*"PAC-1012"*//*"PAC-1216"*//*"PAC-9"*//*"PAC-1036"*//*"PAC-1008"*/"PAC-992";

		ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
		ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
		ArrayList<String> fns = er.evaluatePatents(queryid, "FN");

		System.out.println(queryid);
		System.out.println("----------------------------------------------------------------------");
		System.out.println("----------- TPs: Relevant patents, retrieved at top 100 ------------ ");
		System.out.println("----------------------------------------------------------------------");
		int n = 0; 
		for (String tp : tps) { 
			n++; 

			System.out.print(" [" + n + "] [" + tp + "], "); 
		}			 

		System.out.println();
		System.out.println("----------------------------------------------------------------");
		System.out.println("------- FPs: Non-relevant patents, retrieved at top 100 -------");
		System.out.println("----------------------------------------------------------------");


		int m = 0; 
		for (String fp : fps) { 
			m++; 

			System.out.print(" [" + m + "] [" + fp + "],"); 
		}	

		System.out.println(" ");
		System.out.println();
		System.out.println("-------------------------------------------------------------");
		System.out.println("------ FNs: Relevant patents, not retrieved at top 100 ------");
		System.out.println("-------------------------------------------------------------");
		int k = 0; 
		for (String fn : fns) { 
			k++; 			  

			System.out.print(" [" + k + "] [" + fn + "],"); 
		}		 


	}

}
