package nicta.com.au.failureanalysis.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import nicta.com.au.main.Searcher;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.BM25Rocchio;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

/**
 * @author mona
 * 
 */
public class CollectionSearcher {
	
	static String indexDir = "data/DocPLusQueryINDEX"/*"data/QINDEX"*/;/*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010" *//*args[0]*/;

	private final IndexSearcher is;
	private final int topK;

	private static Similarity getSimilarity(String similarity) {
		if (similarity.toLowerCase().startsWith("bm25ro")) {
			System.err.println("BM25Rocchio");
			return new BM25Rocchio();
		} else if (similarity.toLowerCase().startsWith("tfidf")) {
			// System.err.println("DefaultSimilarity");
			return new DefaultSimilarity();
		} else if (similarity.toLowerCase().startsWith("lmdir")) {
			// System.err.println("LMDirichletSimilarity");
			return new LMDirichletSimilarity();
		} else if (similarity.toLowerCase().startsWith("lmj")) {
			// System.err.println("LMJelinekMercerSimilarity");
			return new LMJelinekMercerSimilarity(1);
		} else if (similarity.toLowerCase().startsWith("ibs")) {
			// System.err.println("IBSimilarity");
			return new IBSimilarity(new DistributionLL(), new LambdaDF(),
					new Normalization.NoNormalization());
		} else if (similarity.toLowerCase().startsWith("bm25ro")) {
			// System.err.println("BM25Similarity");
			return new BM25Similarity();
		} else {
			// System.err.println("BM25Similarity");
			return new BM25Similarity();
		}
	}

	public IndexSearcher getIndexSearch() {
		return is;
	}

	public CollectionSearcher(String indexDir, String similarity, int topK)
			throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		is = new IndexSearcher(DirectoryReader.open(dir));
		is.setSimilarity(getSimilarity(similarity));
		this.topK = topK;
	}

	public float getscoreinMultipleFields(/*String field,*/ String term, String filename) throws IOException, ParseException {
		
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
				Version.LUCENE_48, 
                new String[]{PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, PatentDocument.Claims},
                new StandardAnalyzer(Version.LUCENE_48));

		Query query = queryParser.parse(term);

//		TermQuery query = new TermQuery(new Term(field, term));
		TopDocs topdocs = is.search(query, topK);
		/*System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");*/
		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			if(doc.get(PatentDocument.FileName).contains(filename)){
				// .substring(3).equals(filename)
				//				termfreq = de.freq();
//				System.out.println(scoreDoc.score);
				return scoreDoc.score;
				
			}
			//			doc.get(PatentDocument.FileName).substring(3);
		}
		return (float) 0;
	}

	
	public float getscore(String field, String term, String filename) throws IOException {
				
		TermQuery query = new TermQuery(new Term(field, term));
		TopDocs topdocs = is.search(query, topK);
		/*System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");*/
		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			if(doc.get(PatentDocument.FileName).contains(filename)){
				// .substring(3).equals(filename)
				//				termfreq = de.freq();
//				System.out.println(scoreDoc.score);
				return scoreDoc.score;
				
			}
			//			doc.get(PatentDocument.FileName).substring(3);
		}
		return (float) 0;
	}

	
	public boolean termExists(String field, String term, String filename) throws IOException {
		TermQuery query = new TermQuery(new Term(field, term));
		TopDocs topdocs = is.search(query, topK);
		System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");
		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			if(doc.get(PatentDocument.FileName).contains(filename)){
				// .substring(3).equals(filename)
				//				termfreq = de.freq();
				System.out.println(scoreDoc.score);
				return true;
				
			}
			//			doc.get(PatentDocument.FileName).substring(3);
		}
		return false;
	}

	/**
	 * @param field
	 * @param term
	 * @return documents that contain "term"
	 * @throws IOException
	 */
	public HashSet<String>/*ArrayList<String>*/ singleTermSearch(String field, String term)
			throws IOException {

		ArrayList<String> returneddocuments = new ArrayList<>();
		HashSet<String> h = new HashSet<>();

		TermQuery query = new TermQuery(new Term(field, term));

		TopDocs topdocs = is.search(query, topK);
		System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");

		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			returneddocuments.add(doc.get(PatentDocument.FileName).substring(3));
			h.add(doc.get(PatentDocument.FileName).substring(3));
		}
//		return returneddocuments;
		return h;
	}
	
	
	public HashSet<String> wildcardSearch(String field, String term) throws IOException {
		HashSet<String> docs = new HashSet<>();

		Term testterm = new Term(field, term);
		System.out.println(testterm);
		WildcardQuery query = new WildcardQuery(testterm);
		TopDocs topDocs = is.search(query,topK);	
		System.err.println("'" + term + "'"
				+ " appeared in " + topDocs.totalHits
				+ " documents:");

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			//			returneddocuments.add(doc.get(PatentDocument.FileName).substring(3));
			docs.add(doc.get(PatentDocument.FileName).substring(3));
		}
		return docs;	
	}
	
	public  HashSet<String> prefixQuerySearch(String field, String term) throws IOException {
		HashSet<String> docs = new HashSet<>();
		
		Term testterm = new Term(field, term);
		PrefixQuery query = new PrefixQuery(testterm);
		TopDocs topDocs = is.search(query, topK);
		System.err.println("'" + term + "'"
				+ " appeared in " + topDocs.totalHits
				+ " documents:");

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			docs.add(doc.get(PatentDocument.FileName).substring(3));
		}

//		int match = matches.totalHits;
//		System.out.println(match);
		return docs;				
	}
	

	public static void main(String[] args) throws IOException, ParseException {

		String term = "hammer"/*"film"*//*"print"*//*"G03F"*//*"C04B"*/ /*"G06F"*//*"b32b" *//* "h01l"*/ /*"methyl" */ /*"mona"*/ /*"resin"*/;
		String wildcardterm = "G03?";
		String prefixterm = "G03";
		String field = /*PatentDocument.Classification*/ PatentDocument.Description;
		String filename = /*"EP-0415270"*/ "EP-0426633" /*"EP-0388383"*/;

		CollectionSearcher searcher = new CollectionSearcher(indexDir, "bm25ro", 10000000);
		System.out.println(searcher.getscoreinMultipleFields(term, filename));
		System.out.println(searcher.getscore(field, term, filename));
		

		/*---------------------Testing SingleTermSEarch method---------------------*/
		System.out.println(searcher.termExists(field, term, filename));
		/*-------------------------------------------------------------------------*/

		/*---------------------Testing SingleTermSEarch method---------------------*/

		/*ArrayList<String>*/HashSet<String> returned_docs = searcher.singleTermSearch(field , term.toLowerCase()); 

		
		/*---------------------wildcard and prefix search ---------------------*/
		int n = 0;
		/*for (String d : returned_docs) {
				n++;
				System.out.println("[" + n + "] " + d);
			}*/

		/*HashSet<String> wilds = searcher.wildcardSearch(field, wildcardterm.toLowerCase());
		for (String d : wilds) {
			n++;
			System.out.println("[" + n + "] " + d);
		}
		System.out.println(wilds);
		searcher.prefixQuerySearch(field, prefixterm.toLowerCase());	*/

	}
}
