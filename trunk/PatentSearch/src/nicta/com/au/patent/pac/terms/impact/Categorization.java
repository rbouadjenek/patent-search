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

    public void evaluate(double z) {
        int somme = 0;
        double AP = 0;
        double RR = 0;
        int nbrRelevant = 0;
        String currentQueryid = "";
        String currentRemovedterm = "";
        System.out.println("#queryid\tterm\toldAV\tAV\timpact\trate\tlabel1\tlabel2\tlabel3\tlabel5");
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
                        double rate = 0;
                        if (oldAP != 0) {
                            rate = impact * 100 / oldAP;
                        }
                        int label1;
                        int label2;
                        int label3;
                        if (rate > 0) {
                            label1 = 1;
                            label2 = 1;
                            label3 = 1;
                        } else if (rate < 0 || (oldAP == 0 && AP == 0)) {
                            label1 = 0;
                            label2 = 0;
                            label3 = -1;
                        } else {
                            label1 = 0;
                            label2 = 1;
                            label3 = 0;
                        }
                        int label5 = 1;
                        if (rate < -z || rate == 0) {
                            label5 = 0;
                        }
                        somme += label5;
                        System.out.println(currentQueryid + "\t" + currentRemovedterm + "\t" + df.format(oldAP) + "\t" + df.format(AP) + "\t" + df.format(impact) + "\t" + df.format(rate) + "\t" + label1 + "\t" + label2 + "\t" + label3 + "\t" + label5);
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
                double rate = 0;
                if (oldAP != 0) {
                    rate = impact * 100 / oldAP;
                }
                int label1;
                int label2;
                int label3;
                if (rate > 0) {
                    label1 = 1;
                    label2 = 1;
                    label3 = 1;
                } else if (rate < 0 || (oldAP == 0 && AP == 0)) {
                    label1 = 0;
                    label2 = 0;
                    label3 = -1;
                } else {
                    label1 = 0;
                    label2 = 1;
                    label3 = 0;
                }
                int label5 = 1;
                if (rate < -z || rate == 0) {
                    label5 = 0;
                }
                somme += label5;
                System.out.println(currentQueryid + "\t" + currentRemovedterm + "\t" + df.format(oldAP) + "\t" + df.format(AP) + "\t" + df.format(impact) + "\t" + df.format(rate) + "\t" + label1 + "\t" + label2 + "\t" + label3 + "\t" + label5);
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
        double z = -1;
        if (args.length == 0) {
            qrelFile = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/PAC_test_rels.txt";
            results = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/TermsImpact/abstractResults_Test.txt";
            reference = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/distribution/distAbstract.txt";
        } else {
            qrelFile = args[0];
            results = args[1];
            reference = args[2];
            z = Double.parseDouble(args[3]);
        }
        Categorization re = new Categorization(qrelFile, results, reference);
        re.evaluate(z);
    }
}
