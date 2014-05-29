/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.index;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author rbouadjenek
 */
public class RetrieveTopTerms {

    File INDEX_DIR;

    public RetrieveTopTerms(File INDEXES_DIR) {
        this.INDEX_DIR = INDEXES_DIR;
    }

    public RetrieveTopTerms(String INDEXES_DIR) {
        this.INDEX_DIR = new File(INDEXES_DIR);
    }

    public void showTopTerms(int z) throws IOException, Exception {
//        int k = z * 10;
//        if (INDEX_DIR.isDirectory()) {
//            Directory dir = FSDirectory.open(INDEX_DIR);
//            DirectoryReader dr = DirectoryReader.open(dir);
//            System.out.println("-------------------------------------");
//            System.out.println("TOP TERMS in Title");
//            System.out.println("-------------------------------------");
//            TermStats[] tStat = HighFreqTerms.getHighFreqTerms(dr, k, PatentDocument.Title);
//            tStat = HighFreqTerms.sortByTotalTermFreq(dr, tStat);
//            String str = "";
//            for (TermStats tStat1 : tStat) {
//                double freq = (double) tStat1.docFreq / 1768641;
//                if (!Functions.isNumeric(tStat1.termtext.utf8ToString()) && freq >= 0.05) {
//                    System.out.println(tStat1.termtext.utf8ToString() + " -> DocFreq: " + tStat1.docFreq + ", TotalTermFreq: " + tStat1.totalTermFreq + ", Frequency: " + freq);
//                    str += "\"" + tStat1.termtext.utf8ToString() + "\",";
//                }
//                if (freq < 0.05) {
//                    break;
//                }
//            }
//            System.out.println(str);
//            System.out.println("-------------------------------------");
//            System.out.println("TOP TERMS in Abstract");
//            System.out.println("-------------------------------------");
//            tStat = HighFreqTerms.getHighFreqTerms(dr, k, PatentDocument.Abstract);
//            tStat = HighFreqTerms.sortByTotalTermFreq(dr, tStat);
//            str = "";
//            for (TermStats tStat1 : tStat) {
//                double freq = (double) tStat1.docFreq / 1768641;
//                if (!Functions.isNumeric(tStat1.termtext.utf8ToString()) && freq >= 0.05) {
//                    System.out.println(tStat1.termtext.utf8ToString() + " -> DocFreq: " + tStat1.docFreq + ", TotalTermFreq: " + tStat1.totalTermFreq + ", Frequency: " + freq);
//                    str += "\"" + tStat1.termtext.utf8ToString() + "\",";
//                }
//                if (freq < 0.05) {
//                    break;
//                }
//            }
//            System.out.println(str);
//            System.out.println("-------------------------------------");
//            System.out.println("TOP TERMS in Description");
//            System.out.println("-------------------------------------");
//            tStat = HighFreqTerms.getHighFreqTerms(dr, k, PatentDocument.Description);
//            tStat = HighFreqTerms.sortByTotalTermFreq(dr, tStat);
//            str = "";
//            for (TermStats tStat1 : tStat) {
//                double freq = (double) tStat1.docFreq / 1768641;
//                if (!Functions.isNumeric(tStat1.termtext.utf8ToString()) && freq >= 0.05) {
//                    System.out.println(tStat1.termtext.utf8ToString() + " -> DocFreq: " + tStat1.docFreq + ", TotalTermFreq: " + tStat1.totalTermFreq + ", Frequency: " + freq);
//                    str += "\"" + tStat1.termtext.utf8ToString() + "\",";
//                }
//                if (freq < 0.05) {
//                    break;
//                }
//            }
//            System.out.println(str);
//            System.out.println("-------------------------------------");
//            System.out.println("TOP TERMS in Claims");
//            System.out.println("-------------------------------------");
//            tStat = HighFreqTerms.getHighFreqTerms(dr, k, PatentDocument.Claims);
//            tStat = HighFreqTerms.sortByTotalTermFreq(dr, tStat);
//            str = "";
//            for (TermStats tStat1 : tStat) {
//                double freq = (double) tStat1.docFreq / 1768641;
//                if (!Functions.isNumeric(tStat1.termtext.utf8ToString()) && freq >= 0.05) {
//                    System.out.println(tStat1.termtext.utf8ToString() + " -> DocFreq: " + tStat1.docFreq + ", TotalTermFreq: " + tStat1.totalTermFreq + ", Frequency: " + freq);
//                    str += "\"" + tStat1.termtext.utf8ToString() + "\",";
//                }
//                if (freq < 0.05) {
//                    break;
//                }
//            }
//            System.out.println(str);
//        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RetrieveTopTerms top = new RetrieveTopTerms("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/PATIndex/");
        try {
            top.showTopTerms(50);
        } catch (Exception ex) {
            Logger.getLogger(RetrieveTopTerms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
