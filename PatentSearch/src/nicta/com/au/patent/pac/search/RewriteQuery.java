/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.search;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
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
public class RewriteQuery {

    private final String termsImpactFilename;

    public RewriteQuery(String termsImpactFilename) {
        this.termsImpactFilename = termsImpactFilename;
    }

//    public Query rewrite(String queryid, Query query) throws ParseException {
//        return this.rewrite(queryid, query.toString());
//    }
    public Query rewrite(String queryid, PatentQuery query) throws ParseException {

        BooleanQuery bQuery = (BooleanQuery) query.parse();
        BooleanQuery bQuery2 = (BooleanQuery) ((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses()[0].getQuery();
        BooleanQuery bQueryFinal = new BooleanQuery();
        BooleanQuery bQuery3 = bQuery2.clone();
        try {
            FileInputStream fstream = new FileInputStream(termsImpactFilename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(str);
                    String id = st.nextToken();
                    String term = st.nextToken();
                    double oldav = Double.parseDouble(st.nextToken());
                    double av = Double.parseDouble(st.nextToken());
                    double impact = Double.parseDouble(st.nextToken());
                    int y = Integer.parseInt(st.nextToken());
                    if (queryid.equals(id) && y == 0) {
                        for (BooleanClause bC : bQuery3.clauses()) {
                            TermQuery tq = (TermQuery) bC.getQuery();
                            if (term.startsWith(tq.getTerm().text())) {
                                bQuery3.clauses().remove(bC);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        BooleanQuery bq = new BooleanQuery();
        for (int i = 1; i < PatentQuery.getFields().length; i++) {
            if (query.getQueries()[i] != null && !query.getQueries()[i].equals("") && (i != 4 || i != 6) && query.getBoosts().get(PatentQuery.getFields()[i]) != 0) {

                BooleanQuery bq2 = new BooleanQuery();
                for (BooleanClause bc : bQuery3.clauses()) {
                    TermQuery tq = (TermQuery) bc.getQuery();
                    Term term = new Term(PatentQuery.getFields()[i], tq.getTerm().text());
                    TermQuery tq2 = new TermQuery(term);
                    tq2.setBoost(tq.getBoost());
                    bq2.add(tq2, BooleanClause.Occur.SHOULD);
                }
                bq.add(bq2, BooleanClause.Occur.SHOULD);
            }
        }
        bQueryFinal.add((Query) bQuery.getClauses()[0].getQuery(), BooleanClause.Occur.MUST);
        bQueryFinal.add(bq, BooleanClause.Occur.MUST);

        return bQueryFinal;
    }

    /**
     * @param args the command line arguments
     * @throws org.apache.lucene.queryparser.classic.ParseException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        // TODO code application logic here
        RewriteQuery r = new RewriteQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/termsImpactResults/abstractTermImpact.txt");
        PatentQuery query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1019_EP-1731500-A1.xml", 0, 1, 0, 0, 0, 0, true, true);
        System.out.println(query.parse());
        Query q = r.rewrite("PAC-1019", query);
        System.out.println(q);
    }

}
