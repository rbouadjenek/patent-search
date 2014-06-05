/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.terms.impact;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import nicta.com.au.patent.pac.evaluation.DistributionInMemory;
import nicta.com.au.patent.pac.evaluation.QrelsInMemory;

/**
 *
 * @author rbouadjenek
 */
public class Categorization {

    private final QrelsInMemory qrels;
    private final String results;
    private final DistributionInMemory distribution;
    private final DecimalFormat df;

    public Categorization(String qrelFile, String results, String reference) {
        qrels = new QrelsInMemory(qrelFile);
        this.results = results;
        distribution = new DistributionInMemory(reference);
        df = new DecimalFormat();
        df.setMaximumFractionDigits(4); //arrondi à 2 chiffres apres la virgules
        df.setMinimumFractionDigits(4);
        df.setDecimalSeparatorAlwaysShown(true);

    }

    public void evaluate() {
        double AP = 0;
        double RR = 0;
        int nbrRelevant = 0;
        String currentQueryid = "";
        String currentRemovedterm = "";
        System.out.println("#queryid\tterm\toldAV\tAV\timpact\ty");
        try {
            FileInputStream fstream = new FileInputStream(results);
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
                    String queryid = st.nextToken();
                    String removedterm = st.nextToken();
                    st.nextToken();
                    String docid = st.nextToken();
                    int rank = Integer.parseInt(st.nextToken());
                    if (currentQueryid.equals("") || currentRemovedterm.equals("")) {
                        currentQueryid = queryid;
                        currentRemovedterm = removedterm;
                    }
                    if (!queryid.equals(currentQueryid) || !removedterm.equals(currentRemovedterm)) {
                        AP /= qrels.getNumberOfRelevantPatent(currentQueryid);
                        double oldAP = distribution.getDistributionInMemory().get(currentQueryid);
                        double impact = oldAP - AP;
                        int y = 0;
                        if (impact > 0) {
                            y = 1;
                        }
                        System.out.println(currentQueryid + "\t" + currentRemovedterm.replace("title:", "") + "\t" + df.format(oldAP).replace(",", ".") + "\t" + df.format(AP).replace(",", ".") + "\t" + df.format(impact).replace(",", ".") + "\t" + y);
                        AP = 0;
                        RR = 0;
                        nbrRelevant = 0;
                        currentQueryid = queryid;
                        currentRemovedterm = removedterm;
                    }
                    if (qrels.isPatentRelevant(queryid, docid)) {
                        //Compute AP and RR
                        nbrRelevant++;
                        AP += ((double) nbrRelevant / rank);
                        if (RR == 0) {
                            RR = Double.valueOf(1) / rank;
                        }
                    }
                }
                AP /= qrels.getNumberOfRelevantPatent(currentQueryid);
                double oldAP = distribution.getDistributionInMemory().get(currentQueryid);
                double impact = oldAP - AP;
                int y = 0;
                if (impact > 0) {
                    y = 1;
                }
                System.out.println(currentQueryid + "\t" + currentRemovedterm.replace("title:", "") + "\t" + df.format(oldAP).replace(",", ".") + "\t" + df.format(AP).replace(",", ".") + "\t" + df.format(impact).replace(",", ".") + "\t" + y);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String qrelFile;
        String results;
        String reference;
        if (args.length == 0) {
            qrelFile = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/PAC_test_rels.txt";
            results = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/termsImpactResults/abstractTermImpactResults-2.txt";
            reference = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/distribution/distAbstract.txt";
        } else {
            qrelFile = args[0];
            results = args[1];
            reference = args[2];
        }
        Categorization re = new Categorization(qrelFile, results, reference);
        re.evaluate();
    }
}
