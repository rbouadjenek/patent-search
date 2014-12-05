package nicta.com.au.failureanalysis.evaluate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author mona
 * 
 */
public class QueryAndPatents {

	public String _filename = null;

	/**
	 * @param filename
	 * @return query and related patents- retrieved patents in result file OR relevant patents in qrel file-
	 * @throws IOException
	 */
	public HashMap<String, ArrayList<String>> GetQueryPatents(String filename)
			throws IOException {

		_filename = filename;

		BufferedReader br = new BufferedReader(new FileReader(_filename));
		String line;

		HashMap<String, ArrayList<String>> query_patents = new HashMap<String, ArrayList<String>>();
		//		HashMap<String, ArrayList<String>> query_patents = new HashMap<String, ArrayList<String>>();
		ArrayList<String> patents = new ArrayList<String>();
		// HashMap<String, String> doc_rank = new HashMap<String, String>();

		String[] cols = null;
		String current_quryID;
		String previous_quryID = null;

		int linenum = 0;

		while ((line = br.readLine()) != null) {
			linenum++;
			cols = line.split(" ");
			current_quryID = cols[0];

			if (!current_quryID.equals(previous_quryID) && linenum != 1) {
				// add a new entry for this column number
				query_patents.put(previous_quryID, patents);
				patents = new ArrayList<String>();
			}
			//			System.out.println(cols[2]);
			patents.add(cols[2]);
			// doc_rank.put(cols[2], cols[3]);
			previous_quryID = current_quryID;

		}

		query_patents.put(previous_quryID, patents);

		br.close();

		return query_patents;

	}

	/**
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, String>> GetQueryPatentsRanks(
			String filename) throws IOException {

		_filename = filename;

		BufferedReader br = new BufferedReader(new FileReader(_filename));
		String line;

		// HashMap<String, ArrayList<String>> query_docs = new HashMap<String,
		// ArrayList<String>>();
		HashMap<String, String> patents_ranks = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> query_patent_rank = new HashMap<String, HashMap<String, String>>();

		String[] cols = null;
		String current_quryID;
		String previous_quryID = null;

		int linenum = 0;

		while ((line = br.readLine()) != null) {
			linenum++;
			cols = line.split(" ");
			current_quryID = cols[0];

			if (!current_quryID.equals(previous_quryID) && linenum != 1) {
				// add a new entry for this column number
				query_patent_rank.put(previous_quryID, patents_ranks);
				patents_ranks = new HashMap<String, String>();
			}

			patents_ranks.put(cols[2], cols[3]);
			previous_quryID = current_quryID;

		}

		query_patent_rank.put(previous_quryID, patents_ranks);

		br.close();

		return query_patent_rank;

	}

	/**
	 * @param filename
	 * @return
	 * Key = Rank
	 * Value = Retrieved document
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, String>> GetQueryRanksPatents2(
			String filename) throws IOException {

		_filename = filename;

		BufferedReader br = new BufferedReader(new FileReader(_filename));
		String line;

		// HashMap<String, ArrayList<String>> query_docs = new HashMap<String,
		// ArrayList<String>>();
		HashMap<String, String> patents_ranks = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> query_patent_rank = new HashMap<String, HashMap<String, String>>();

		String[] cols = null;
		String current_quryID;
		String previous_quryID = null;

		int linenum = 0;

		while ((line = br.readLine()) != null) {
			linenum++;
			cols = line.split(" ");
			current_quryID = cols[0];

			if (!current_quryID.equals(previous_quryID) && linenum != 1) {
				// add a new entry for this column number
				query_patent_rank.put(previous_quryID, patents_ranks);
				patents_ranks = new HashMap<String, String>();
			}

			patents_ranks.put(cols[3], cols[2]);
			previous_quryID = current_quryID;

		}

		query_patent_rank.put(previous_quryID, patents_ranks);

		br.close();

		return query_patent_rank;

	}

	/**
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, Integer>> GetQueryPatentsRanks3(
			String filename) throws IOException {

		_filename = filename;

		BufferedReader br = new BufferedReader(new FileReader(_filename));
		String line;

		// HashMap<String, ArrayList<String>> query_docs = new HashMap<String,
		// ArrayList<String>>();
		HashMap<String, Integer> patents_ranks = new HashMap<String, Integer>();
		HashMap<String, HashMap<String, Integer>> query_patent_rank = new HashMap<String, HashMap<String, Integer>>();

		String[] cols = null;
		String current_quryID;
		String previous_quryID = null;

		int linenum = 0;

		while ((line = br.readLine()) != null) {
			linenum++;
			cols = line.split(" ");
			current_quryID = cols[0];

			if (!current_quryID.equals(previous_quryID) && linenum != 1) {
				// add a new entry for this column number
				query_patent_rank.put(previous_quryID, patents_ranks);
				patents_ranks = new HashMap<String, Integer>();
			}

			patents_ranks.put(cols[2], Integer.parseInt(cols[3]));
			previous_quryID = current_quryID;

		}

		query_patent_rank.put(previous_quryID, patents_ranks);

		br.close();

		return query_patent_rank;

	}

	public static void main(String[] args) throws IOException {

		/*-------------------------- Write in outputfile. --------------------------*/
		String outputfile = "./output/test.txt";		
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------*/

		QueryAndPatents qps = new QueryAndPatents();
		//		HashMap<String, ArrayList<String>> _querypatents = qps
		////						.GetQueryPatents("output/results/results-lmdir-desc-100.txt");
		//		 .GetQueryPatents("data/qrel/filtered-qrelfile-original.txt"
		//				 /*"data/qrel/PAC_test_rels.txt"*/);
		//		
		//		int i = 0;
		//		for (Entry<String, ArrayList<String>> k : _querypatents.entrySet()) {
		//			i++;
		//			ArrayList<String> patents = _querypatents.get(k.getKey());
		//			// System.out.println(patents.size());
		//			System.out.println(" [" + i + "] " + k.getKey() + " , " + patents);
		////			ps.println(" [" + i + "] " + k.getKey() + " , " + patents);
		//		}


		/*---------------Uncomment below to test retrieved documents and the ranks---------------*/
		
		HashMap<String, HashMap<String, String>> _docranks = qps
				.GetQueryPatentsRanks("output/results/results-lmdir-desc-100.txt");



		int j = 0;
		for (Entry<String, HashMap<String, String>> dr : _docranks.entrySet()) {
			j++;

			System.out.println(" [" + j + "] " + dr.getKey() + " , "
					+ _docranks.get(dr.getKey()));
		}
		System.err.println(_docranks.get("PAC-191").get("EP-1006760"));
		System.err.println(_docranks.get("PAC-191").get("EP-1006760"));



		/*---------------Uncomment below to test retrieved documents and the ranks 2---------------*/
		/*HashMap<String, HashMap<String, String>> _docranks = qps
				.GetQueryRanksPatents2("output/results/results-lmdir-desc-100.txt");



		int j = 0;
		for (Entry<String, HashMap<String, String>> dr : _docranks.entrySet()) {
			j++;

			System.out.println(" [" + j + "] " + dr.getKey() + " , "
					+ _docranks.get(dr.getKey()));
		}

		System.err.println(_docranks.get("PAC-191").get("18"));*/
	}

}



/*-------------- This class is used to sort a hashmap --------------*/
//
//class ValueComparator implements Comparator<String> {
//
//	Map<String, Integer> base;
//	public ValueComparator(Map<String, Integer> base) {
//		this.base = base;
//	}
//
//	// Note: this comparator imposes orderings that are inconsistent with equals.    
//	public int compare(String a, String b) {
//		if (base.get(a) >= base.get(b)) {
//			return -1;
//		} else {
//			return 1;
//		} // returning 0 would merge keys
//	}
//}
