package nicta.com.au.failureanalysis.query.test;

import java.io.File;
import java.io.IOException;

import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.BM25Rocchio;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
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

public class TestQTermScoring {
	static String indexDir =  "data/DocPLusQueryINDEX"/*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/;
	private static IndexReader ir;	
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
	
	public TestQTermScoring (String indexDir, String similarity, int topK)
			throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		is = new IndexSearcher(DirectoryReader.open(dir));
		is.setSimilarity(getSimilarity(similarity));
		this.topK = topK;
		
		ir = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
	}


	public void CalculateTermScoreInDoc(String field, String term, String filename) throws IOException {
		
	}
	
	
	public static void main(String[] args) throws IOException {
		String term = "hammer"/*"film"*//*"print"*//*"G03F"*//*"C04B"*/ /*"G06F"*//*"b32b" *//* "h01l"*/ /*"methyl" */ /*"mona"*/ /*"resin"*/;
		String field = /*PatentDocument.Classification*/ PatentDocument.Description;
		String filename = /*"UN-EP-0415270"*/ "EP-1238759-A1" /*"UN-EP-0426633"*/ /*"UN-EP-0388383"*/;
		
		
		String similarity = /*"tfidf"*/"lmdir"/*"bm25ro"*/;
		int topK = 1000000;
		
		CollectionReader reader = new CollectionReader(indexDir);
		
		int b = reader.getDocId(/*"UN-" + */filename, PatentDocument.FileName);
		System.out.println(b);
		
		TestQTermScoring termscore = new TestQTermScoring(indexDir, similarity, topK);
		termscore.CalculateTermScoreInDoc(field, term, filename);
	}
}
