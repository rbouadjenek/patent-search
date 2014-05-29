/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.evaluation;

import com.hrstc.lucene.queryexpansion.PatentClassCodeBasedQueryExpansion;
import com.hrstc.lucene.queryexpansion.PatentQueryExpansion;
import com.hrstc.lucene.queryexpansion.PatentRocchioQueryExpansion;
import com.hrstc.lucene.queryreduction.PatentRocchioQueryReduction;
import dcu.com.ie.patent.queryreduction.PatentMagdyQueryReduction;
import dcu.com.ie.synset.PatentSynSetQueryExpansion;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.pac.search.PACSearcher;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.PatentQuery;
import nicta.com.au.patent.pac.search.RewriteQuery;
import nicta.com.au.patent.queryexpansion.PatentMMRQueryExpansion;
import nicta.com.au.patent.queryreduction.PatentMMRQueryReduction;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author rbouadjenetopK
 */
public final class ExecuteTopics {

    private final File topicFile;
    private final PACSearcher searcher;
    private final Map<String, Float> boosts;
    private final boolean filter;
    private final boolean stopWords;
    private final boolean rewrite;
    private final RewriteQuery rewriteQuery;
    private final boolean expansion;
    private PatentQueryExpansion pqe;
    private final String startingPoint;

    public ExecuteTopics(String indexDir, String topicFile, int topK, String similarity, float titleBoost, float abstractBoost,
            float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords,
            boolean rewrite, String termsImpactFilename, boolean expansion, String algo, String classCodesIndexDir, int nbrDocs, int nbrTerms, int source, float alpha, float beta, float gamma, String decay) throws IOException, Exception {
        this.topicFile = new File(topicFile);
        boosts = new HashMap<>();
        boosts.put(PatentDocument.Classification, new Float(0));
        boosts.put(PatentDocument.Title, titleBoost);
        boosts.put(PatentDocument.Abstract, abstractBoost);
        boosts.put(PatentDocument.Description, descriptionBoost);
        boosts.put("descriptionP5", descriptionP5Boost);
        boosts.put(PatentDocument.Claims, claimsBoost);
        boosts.put("claims1", claims1Boost);
        this.filter = filter;
        this.stopWords = stopWords;
        this.rewrite = rewrite;
        this.rewriteQuery = new RewriteQuery(termsImpactFilename);
        searcher = new PACSearcher(indexDir, similarity, topK);
        this.expansion = expansion;
        startingPoint = decay;
        if (startingPoint.equals("-1")) {
            System.out.print(" #indexDir: " + indexDir);
            System.out.print(" #topicfile: " + topicFile);
            System.out.print(" #topK: " + topK);
            System.out.print(" #Similarity: " + similarity);
            System.out.print(" #titleBoost: " + titleBoost);
            System.out.print(" #abstractBoost: " + abstractBoost);
            System.out.print(" #descriptionBoost: " + descriptionBoost);
            System.out.print(" #descriptionP5Boost: " + descriptionP5Boost);
            System.out.print(" #claimsBoost: " + claimsBoost);
            System.out.print(" #claims1Boost: " + claims1Boost);
            System.out.print(" #Filter: " + filter);
            System.out.print(" #Stop Words: " + stopWords);
        }
        if (expansion) {
            if (startingPoint.equals("-1")) {
                System.out.print(" #Expansion: " + expansion);
                System.out.print(" #Algo: " + algo);
            }
            if (algo.toLowerCase().equals("rocc")) {
                pqe = new PatentRocchioQueryExpansion(indexDir, nbrDocs, nbrTerms, source, alpha, beta, gamma, 0);
                System.out.print(" #nbrDocs: " + nbrDocs);
                if (source != 7) {
                    System.out.print(" #source: " + PatentQuery.getFields()[source]);
                } else {
                    System.out.println(" #source: All");
                }
            } else if (algo.toLowerCase().equals("roccqr")) {
                pqe = new PatentRocchioQueryReduction(indexDir, nbrDocs, nbrTerms, source, alpha, beta, gamma, 0);
                System.out.print(" #nbrDocs: " + nbrDocs);
                if (source != 7) {
                    System.out.print(" #source: " + PatentQuery.getFields()[source]);
                } else {
                    System.out.println(" #source: All");
                }
            } else if (algo.toLowerCase().equals("mmrqe")) {
                pqe = new PatentMMRQueryExpansion(searcher.getIndexSearch(), similarity, nbrDocs, nbrTerms, source, alpha, beta);
                System.out.print(" #nbrDocs: " + nbrDocs);
                if (source != 7) {
                    System.out.print(" #source: " + PatentQuery.getFields()[source]);
                } else {
                    System.out.println(" #source: All");
                }
            } else if (algo.toLowerCase().equals("mmrqr")) {
                pqe = new PatentMMRQueryReduction(searcher.getIndexSearch(), similarity, nbrDocs, nbrTerms, source, beta);
                if (startingPoint.equals("-1")) {
                    System.out.print(" #nbrDocs: " + nbrDocs);
                    if (source != 7) {
                        System.out.print(" #source: " + PatentQuery.getFields()[source]);
                    } else {
                        System.out.println(" #source: All");
                    }
                }
            } else if (algo.toLowerCase().equals("magdyqr")) {
                pqe = new PatentMagdyQueryReduction(searcher.getIndexSearch(), similarity, nbrDocs, nbrTerms, source);
                System.out.print(" #nbrDocs: " + nbrDocs);
                if (source != 7) {
                    System.out.print(" #source: " + PatentQuery.getFields()[source]);
                } else {
                    System.out.println(" #source: All");
                }
            } else if (algo.toLowerCase().equals("wsynset")) {
                System.out.print(" #SynSet: " + termsImpactFilename);
                pqe = new PatentSynSetQueryExpansion(termsImpactFilename, nbrTerms, true);
            } else if (algo.toLowerCase().equals("usynset")) {
                System.out.print(" #SynSet: " + termsImpactFilename);
                pqe = new PatentSynSetQueryExpansion(termsImpactFilename, nbrTerms, false);
            } else {
                pqe = new PatentClassCodeBasedQueryExpansion(classCodesIndexDir, nbrTerms, alpha, beta, 0);
                System.out.print(" #classCodesIndexDir: " + classCodesIndexDir);
            }
            if (startingPoint.equals("-1")) {
                System.out.print(" #nbrTerms: " + nbrTerms);
                if (algo.toLowerCase().equals("mmrqe")) {
                    System.out.print(" #alpha: " + alpha);
                    System.out.print(" #lambda: " + beta);
                } else if (algo.toLowerCase().equals("mmrqr")) {
                    System.out.print(" #lambda: " + beta);
                } else {
                    System.out.print(" #alpha: " + alpha);
                    System.out.print(" #beta: " + beta);
                    System.out.print(" #gamma: " + gamma);
                    System.out.print(" #decay: " + decay);
                }
                System.out.println("");
            }

        }
    }

    public void execute() throws IOException, Exception {
        TopicsInMemory topics = new TopicsInMemory(topicFile);
        long start = System.currentTimeMillis();
        int j = 0;
        boolean startP = false;
        for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
            j++;
            String queryid = e.getKey();
            if (startingPoint.equals("-1")) {
                startP = true;
            } else if (startingPoint.equals(queryid)) {
                startP = true;
                continue;
            }
            if (!startP) {
                continue;
            }
            PatentDocument pt = e.getValue();
            System.err.print(j + "- " + queryid + " -> " + pt.getUcid());
            long start2 = System.currentTimeMillis();
            TopDocs hits;
//            if (rewrite) {
//                Query query = searcher.rewrite(queryid, pt, rewriteQuery, boosts, filter, stopWords);
//                hits = searcher.search(query);                
//            } else
            if (expansion) {
                PatentQuery pq = new PatentQuery(pt, boosts, filter, stopWords);
//                System.err.println("");
//                System.err.println("---------------------------------------------------------------------------------------------------");
//                System.err.println("Original query: " + pq.parse());
//                System.err.println("---------------------------------------------------------------------------------------------------");
                Query query = pqe.expandQuery(pq);
//                System.err.println("Expanded query: " + query);
                hits = searcher.search(query);
            } else {
                hits = searcher.search(pt, boosts, filter, stopWords);
            }
            long end2 = System.currentTimeMillis();
            System.err.println(" - Found " + hits.totalHits
                    + " document(s) has matched query " + pt.getUcid() + ". Processed in " + Functions.getTimer(end2 - start2) + ".");

//            System.err.println(queryid + "\t" + hits.totalHits);
            int i = 0;
            if (hits.totalHits == 0) {
                System.out.println(queryid + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
            }
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                i++;
                Document doc = searcher.getIndexSearch().doc(scoreDoc.doc);
                System.out.println(queryid + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + i + " " + scoreDoc.score + " STANDARD");
            }
        }
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("#Global Execution time: " + Functions.getTimer(millis) + ".");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String indexDir;
        String topicFile;
        int topK;
        String sim;
        float titleBoost;
        float abstractBoost;
        float descriptionBoost;
        float descriptionP5Boost;
        float claimsBoost;
        float claims1Boost;
        boolean filter;
        boolean stopWords;
        // Rewriting?
        boolean rewrite;
        String termsImpactFilename = null;
        // Expansion?
        boolean expansion;
        String algo = "";
        int nbrDocs = 0;
        String classCodesIndexDir = null;
        int nbrTerms = 0;
        int source = 0;
        float alpha = 0;
        float beta = 0;
        float gamma = 0;
//        float decay = 0;
        String decay = "";
        if (args.length < 13) {
            System.err.println("ERROR: incorrect parameters for nicta.com.au.patent.pac.evaluation.ExecuteTopics! "
                    + "only " + args.length + " parameters :-(");
            System.err.println("Usage: java -jar Lucene-4.4.0.jar [options]");
            System.err.println("[options] have to be defined in the following order:");
            System.err.println("[-indexDir]: directory of the index");
            System.err.println("[-topicFile]: topic file");
            System.err.println("[-topK]: topK document retrieved for each topic");
            System.err.println("[-similarity]: Similarity used (DefaultSimilarity, BM25Similarity,"
                    + " LMDirichletSimilarity, LMJelinekMercerSimilarity, IBSimilarity)");
            System.err.println("[-titleBoost]: value for boosting the title ");
            System.err.println("[-abstractBoost]: value for boosting the abstract ");
            System.err.println("[-descriptionBoost]: value for boosting the description ");
            System.err.println("[-descriptionP5Boost]: value for boosting the first five paragraphs of the description ");
            System.err.println("[-claimsBoost]: value for boosting the claims ");
            System.err.println("[-claims1Boost]: value for boosting the first claims ");
            System.err.println("[-filter]: boolean to indicate whether or not we should use the IPC filer ");
            System.err.println("[-stopWords]: boolean to indicate whether or not we should use the patent specific stop words ");
            // Rewriting?
            System.err.println("[-rewrite]: boolean to indicate whether or not we should rewrite the query according to a terms impact file");
            System.err.println("[-termsImpactFilename]: term impact file");
            // Expansion?
            System.err.println("[-expansion]: boolean to indicate whether or not we should expand the query");
            System.err.println("[-expansion]: what expansion algorithm to use? Rocchio based on PRF or on Classification codes, or using MMRQE algorithm");
            System.err.println("[-nbrDocs]: if Rocchio based on PRF, then give number of document used for the PRF set");
            System.err.println("[-indexClassDir]: if Rocchio based on Classification codes, then give the directory of the classification index");
            System.err.println("[-nbrtermsTitle]: number of term used for the expansion");
            System.err.println("[-source]: source of the expansion title(1),abstract(2),description(3),or claims(5)");
            System.err.println("[-alpha]: value of alpha to set the weight of the original query terms");
            System.err.println("[-beta]: value of beta to set the weight of the expanded query terms for relevant documents");
            System.err.println("[-gamma]: value of gamma to set the weight of the expanded query terms for irrelevant documents");
            System.err.println("[-decay]: weight of the terms in top k documents");
        } else {
            indexDir = args[0];
            topicFile = args[1];
            topK = Integer.parseInt(args[2]);
            sim = args[3];
            titleBoost = Float.parseFloat(args[4]);
            abstractBoost = Float.parseFloat(args[5]);
            descriptionBoost = Float.parseFloat(args[6]);
            descriptionP5Boost = Float.parseFloat(args[7]);
            claimsBoost = Float.parseFloat(args[8]);
            claims1Boost = Float.parseFloat(args[9]);
            filter = Boolean.valueOf(args[10]);
            stopWords = Boolean.valueOf(args[11]);
            rewrite = Boolean.valueOf(args[12]);
            int j;
            if (rewrite) {
                termsImpactFilename = args[13];
                j = 14;
            } else {
                j = 13;
            }
            expansion = Boolean.valueOf(args[j]);
            if (expansion) {
                algo = args[j + 1];
                if (algo.toLowerCase().startsWith("code")) {
                    classCodesIndexDir = args[j + 2];
                    j++;
                }
                nbrDocs = Integer.parseInt(args[j + 2]);
                nbrTerms = Integer.parseInt(args[j + 3]);
                source = Integer.parseInt(args[j + 4]);
                alpha = Float.parseFloat(args[j + 5]);
                beta = Float.parseFloat(args[j + 6]);
                gamma = Float.parseFloat(args[j + 7]);
//                decay = Float.parseFloat(args[j + 8]);
                decay = args[j + 8];
            }
            try {
                ExecuteTopics ex = new ExecuteTopics(indexDir, topicFile, topK, sim, titleBoost, abstractBoost,
                        descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords, rewrite, termsImpactFilename,
                        expansion, algo, classCodesIndexDir, nbrDocs, nbrTerms, source, alpha, beta, gamma, decay);
                ex.execute();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
