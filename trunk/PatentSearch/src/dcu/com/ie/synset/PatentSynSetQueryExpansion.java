/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcu.com.ie.synset;

import com.hrstc.lucene.queryexpansion.PatentQueryExpansion;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class PatentSynSetQueryExpansion extends PatentQueryExpansion {
    
    SynSetLoad synset;
    boolean weigth;
    private final int Nbr_Terms;
    
    public PatentSynSetQueryExpansion(SynSetLoad synset, int Nbr_Terms, Boolean weight) {
        this.synset = synset;
        this.weigth = weight;
        this.Nbr_Terms = Nbr_Terms;
    }
    
    public PatentSynSetQueryExpansion(String fileSynset, int Nbr_Terms, Boolean weight) {
        this.synset = new SynSetLoad(fileSynset);
        this.weigth = weight;
        this.Nbr_Terms = Nbr_Terms;
    }
    
    @Override
    public Query expandQuery(PatentQuery query) throws ParseException, IOException {
        BooleanQuery bQuery = new BooleanQuery();
        BooleanQuery bQueryFieldsExpanded = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        Query expandedQuery = null;
        for (int i = 1; i < PatentQuery.getFields().length; i++) {
            if (query.getQueries()[i] != null && !query.getQueries()[i].equals("") && (i != 4 || i != 6) && query.getBoosts().get(PatentQuery.getFields()[i]) != 0) {
                QueryParser qp = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[i],
                        new StandardAnalyzer(Version.LUCENE_48));
                Query q = qp.parse(query.getQueries()[i]);
                if (expandedQuery == null) {
                    BooleanQuery bq;
                    if (q instanceof BooleanQuery) {
                        bq = ((BooleanQuery) q).clone();
                    } else {
                        bq = new BooleanQuery();
                        bq.add(q, BooleanClause.Occur.SHOULD);
                    }
                    
                    BooleanQuery bq2 = new BooleanQuery();
                    for (BooleanClause bc : bq.clauses()) {
                        TermQuery tq = (TermQuery) bc.getQuery();
                        bq2.add(tq, BooleanClause.Occur.SHOULD);
//                        System.err.println(tq.getTerm().text());
                        List<Map.Entry<String, Double>> l = synset.getSynSeyList(tq.getTerm().text(), Nbr_Terms);
                        for (Map.Entry<String, Double> e : l) {
//                            System.err.println("\t" + e.getKey() + " -> " + e.getValue());
                            Term term = new Term(PatentQuery.getFields()[i], e.getKey());
                            TermQuery tq2 = new TermQuery(term);
                            float boost = tq.getBoost();
                            if (weigth) {
                                boost *= e.getValue().floatValue();
                            }
                            tq2.setBoost(boost);
                            bq2.add(tq2, BooleanClause.Occur.SHOULD);
                        }
                    }
                    expandedQuery = bq2;
                } else {
                    BooleanQuery bq = ((BooleanQuery) expandedQuery).clone();
                    BooleanQuery bq2 = new BooleanQuery();
                    for (BooleanClause bc : bq.clauses()) {
                        TermQuery tq = (TermQuery) bc.getQuery();
                        Term term = new Term(PatentQuery.getFields()[i], tq.getTerm().text());
                        TermQuery tq2 = new TermQuery(term);
                        tq2.setBoost(tq.getBoost());
                        bq2.add(tq2, BooleanClause.Occur.SHOULD);
                    }
                    expandedQuery = bq2;
                }
                bQueryFieldsExpanded.add(expandedQuery, BooleanClause.Occur.SHOULD);
                
            }
        }
        if (query.isFilter()) {
            Query q = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[0],
                    new StandardAnalyzer(Version.LUCENE_48)).parse(query.getQueries()[0]);
            q.setBoost(query.getBoosts().get(PatentQuery.getFields()[0]));
            bQuery.add(q, BooleanClause.Occur.MUST);
        }
        bQuery.add(bQueryFieldsExpanded, BooleanClause.Occur.MUST);
//        TopDocs hits = searcher.search(bQuery, 100);
//                System.err.println(hits.totalHits + " total matching documents.");
        return bQuery;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        // TODO code application logic here
        SynSetLoad synset = new SynSetLoad("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/SynSet/SynSet.pruned.txt");
        PatentQuery query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1019_EP-1731500-A1.xml", 0, 1, 0, 0, 0, 0, true, true);
//        System.out.println(query.parse());
        PatentSynSetQueryExpansion pssqe = new PatentSynSetQueryExpansion(synset, 2000, false);
        System.out.println(query.parse());
        System.out.println(pssqe.expandQuery(query));
    }
    
}
