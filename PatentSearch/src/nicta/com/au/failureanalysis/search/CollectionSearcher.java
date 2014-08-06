package nicta.com.au.failureanalysis.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.BM25Rocchio;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
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

/**
 * @author mona
 * 
 */
public class CollectionSearcher {

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
	
	public CollectionSearcher(String indexDir, String similarity, int topK)
			throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		is = new IndexSearcher(DirectoryReader.open(dir));
		is.setSimilarity(getSimilarity(similarity));
		this.topK = topK;
	}
	
	
	/**
	 * @param field
	 * @param term
	 * @return documents that contain "term"
	 * @throws IOException
	 */
	public ArrayList<String> singleTermSearch(String field, String term)
			throws IOException {

		ArrayList<String> returneddocuments = new ArrayList<>();

		TermQuery query = new TermQuery(new Term(field, term));
		
		// TermQuery query = new TermQuery(new Term(PatentDocument.Classification, term));

		TopDocs topdocs = is.search(query, topK);
		System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");
		
		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			returneddocuments.add(doc.get(PatentDocument.FileName).substring(3));

		}
		return returneddocuments;
	}
	
	public static void main(String[] args) {
		
		String indexDir = args[0];
		
		try {
			String term = /* "b32b" *//* "h01l"*/ /*"methyl" */ "resin" ;
			String field = /*PatentDocument.Classification*/ PatentDocument.Title;

			CollectionSearcher searcher = new CollectionSearcher(indexDir, "bm25ro", 1000);

			ArrayList<String> returned_docs = searcher.singleTermSearch(field , term.toLowerCase()); 

			int n = 0;
			for (String d : returned_docs) {
				n++;
				System.out.println("[" + n + "] " + d);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
