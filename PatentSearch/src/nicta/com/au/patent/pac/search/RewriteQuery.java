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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
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
    public Query rewrite(String queryid, Query query) throws ParseException {
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
                    double av = Double.parseDouble(st.nextToken());
                    double impact = Double.parseDouble(st.nextToken());
                    int label = Integer.parseInt(st.nextToken());
                    if (queryid.equals(id) && label == 0) {
                        BooleanClause bClause = ((BooleanQuery) query).getClauses()[1];
                        if (bClause.getQuery() instanceof BooleanQuery) {
                            BooleanQuery bQuery = (BooleanQuery) bClause.getQuery();
                            for (BooleanClause bC : bQuery.clauses()) {
                                Query q = (Query) bC.getQuery();
                                if (q.toString().equals(term)) {
                                    bQuery.clauses().remove(bC);
                                    break;
                                }
                            }
                        }
                    }
                }
                return query;
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        // TODO code application logic here
        RewriteQuery r = new RewriteQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/resultsAbstractTermsImpact.txt");
        QueryParser parser = new QueryParser(Version.LUCENE_48, null, new StandardAnalyzer(Version.LUCENE_48));
        Query q = r.rewrite("PAC-1075", parser.parse("+class:c08g^0.0 (abstract:gener abstract:solvent abstract:treatment abstract:cf2cl abstract:higher abstract:filtrat^2.0 abstract:function^2.0 abstract:compris abstract:x1and abstract:pfpe^6.0 abstract:bifunct abstract:perfluoropolyeth abstract:c3f6cl abstract:select abstract:number abstract:cf3^2.0 abstract:molecular abstract:solid^3.0 abstract:obtain^3.0 abstract:liquid^2.0 abstract:separ^2.0 abstract:cf2cf3 abstract:phase^3.0 abstract:filter^2.0 abstract:ch2oh^2.0 abstract:weight abstract:cfxch2oh^2.0 abstract:rf^2.0 abstract:x1 abstract:mixtur^6.0 abstract:adsorb abstract:cf2cf2cl abstract:subsequ abstract:end^2.0 abstract:polar abstract:addit^2.0 abstract:stir abstract:monofunct abstract:cf2br abstract:averag^2.0 abstract:process^2.0 abstract:termin^2.0 abstract:chain abstract:of\\:a abstract:c2^2.0 abstract:perfluoropolyoxyalkylen abstract:high abstract:group^2.0 abstract:step abstract:formula)"));
        System.out.println(q);
    }

}
