/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.main;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
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
public class Lucene440 {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        QueryParser parser = new QueryParser(Version.LUCENE_48, "test", new StandardAnalyzer(Version.LUCENE_48));
        Query q1 =  parser.parse("5 total^4 monomer^4 deriv^3 higher^1 hexafluoropropen^1 cf2cf^1 butylacryl^1 refer^2 acord^1 option^3 number^2 molecular^1 partial^1 curabl^4 ratio^1 r6are^1 formula\\:wherein\\:r1^1 mole^12 vf^1 meth^1 x2^1 c10alkylen^1 perfluoroalkyl^1 x1^1 alkylen^2 rfi^2 site^1 monom^5 c6linear^2 perfluoropropylvinyleth^1 averag^1 perfluoroalkylen^1 fha^1 hydrogen^4 c8perfluoroolefin^1 oxyalkyl^1 z^3 respect^2 peroxid^8 semicrystlalin^1 ethylen^6 ch2ch2o^2 cfox^1 amount^7 methylmethacryl^1 basic^1 bi^6 hexafluoroisobuten^1 vdf^3 ch2o^1 fluorovinyleth^2 hydroxyethylhexylacryl^1 composit^3 cforf^1 resist^1 agent^1 fluoroalkylvinyleth^2 c18linear^1 c6perfluoroalkyl^1 fluorooxyalkyl^1 ch^1 styren^1 prefer^18 c12^3 move2^1 move1^1 weight^9 fluoropolyoxi^1 sum^2 fluoroolefin^2 oxyalkylen^1 ocf3^1 vinyl^1 coat^1 cent^2 tetrafluoroethylen^2 dry^1 comonom^6 c6^2 c5^2 c4^2 equal^1 c3^2 c8^1 unit^4 c8non^1 c1^10 c2^4 articl^1 r6^1 core^2 improv^1 high^1 r2^2 r3^2 r4^2 r5^2 chlorin^1 shell^3 c12alkyl^1 thermal^1 oxyalkylvinyleth^2 gener^4 temperatur^1 cure^3 cfor2f^1 ether^2 c2f5^1 rang^2 c8hydrogen^1 olefin^7 propylen^1 semicrystallin^8 move^1 r2fi^1 cf3^3 oelfin^1 c5alkyl^1 obtain^1 cf2^8 c12oxyalkyl^1 following\\:a^1 perfluoromethyl^1 branch^3 r1^1 cycloalkylen^1 hfp^1 iodo^1 oxygen^2 cfx1o^1 perfluorin^1 express^2 fluoropolym^9 perfluoroelastom^10 cfocf2ocf2cf3^1 cfox0^1 rfperfluoroalkylethylen^1 c3f6o^1 c3f7^1 o1^3 cfx2^2 radic^8 perfluorooxyalkyl^1 fluoropolyoxyalkylen^2 chlorotrifluoroethylen^1 ch2^1 fluorodioxol^2 chain^2 attack^1 atom^4 sch2^1 type^2 trifluoroethylen^1 heat^1 fluoro^3 fluoroalkyl^3 vinyliden^2 cf2o^2 perfluorovinyleth^2 pave^6 c8chloro^1 unsatur^5 cf2cf2cf2o^1 c6cyclic^1 goup^2 ch2och2^1 tfe^7 manufacturd^1 x0i^1 perfuoroelastom^1 ctfe^1 copolym^3 constitut^1 homopolym^1 cx2ocf2ocf2cf2i^1 polym^5 c8fluoroolefin^1 mean^1 bromo^1 cx2ocf2or^1 iodin^5 bromin^4 ii^2 crosslink^23 capabl^1 fluorid^3 acryl^2 x2i^1 cf2cf2o^2 fluorin^4 perfluorodioxol^2 perfluoroethyl^1 cfocf2ocf2cf2ocf3^1 formula^7  ");
        System.out.println(q1);
//        Query q2 = q1.getClauses()[1].getQuery();
//        if (q2 instanceof BooleanQuery) {
//            for(BooleanClause bc:((BooleanQuery) q2).clauses()){
//               TermQuery qt=(TermQuery) bc.getQuery();
//                System.out.println(qt.getTerm().text()+" boost: "+qt.getBoost());
//            }
//        } else if (q2 instanceof TermQuery) {
//            TermQuery q3 = (TermQuery) q2;
//        }

//        TermQuery q3 = (TermQuery) q2.getClauses()[5].getQuery();
//        TermQuery q34 = (TermQuery) q2.getClauses()[0].getQuery();
//        System.out.println("text: " + q3.getTerm().text());
//        System.out.println("field: " + q3.getTerm().field());
//        System.out.println("size: " + q3.getTerm().text().length());
//        System.out.println("tf: " + q3.getBoost());
//        System.out.println("idf: ");
//        System.out.println(q2.clauses().size());
//        System.out.println(q3);
//        System.out.println(Double.parseDouble("1.0"));
    }
}
