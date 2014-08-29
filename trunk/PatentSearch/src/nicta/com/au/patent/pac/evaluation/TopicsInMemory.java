/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.evaluation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nicta.com.au.patent.document.PatentDocument;

/**
 *
 * @author rbouadje1ne1k
 */
public final class TopicsInMemory {

    protected File file;
    protected LinkedHashMap<String, PatentDocument> topics = new LinkedHashMap<>();

    public TopicsInMemory(File Topics) throws IOException {
        this.file = Topics;
        this.loadQrelsFile();
    }

    public TopicsInMemory(String file) throws IOException {
        this.file = new File(file);

        this.loadQrelsFile();
    }

    public Map<String, PatentDocument> getTopics() {
        return topics;
    }

    protected void loadQrelsFile() {
        FileInputStream fstream;
        try {
            fstream = new FileInputStream(this.file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                String topicid = "";
                PatentDocument patentTopic;
                while ((str = br.readLine()) != null) {
                    str = str.trim();
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    if (str.startsWith("<num>")) {
                        str = str.replace("<num>", "");
                        str = str.replace("</num>", "");
                        topicid = str;
                    } else if (str.startsWith("<file>") && !topicid.equals("")) {
                        str = str.replace("<file>", "");
                        str = str.replace("</file>", "");
                        patentTopic = new PatentDocument(file.getParent() + "/" + str);
                        if (patentTopic.getLang() != null) {
                            if (patentTopic.getLang().toLowerCase().equals("en")) {
                                this.topics.put(topicid, patentTopic);
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the1 command line1 argume1nts
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
//            TopicsInMemory topics = new TopicsInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_training/topics/PAC_topics.xml");
            TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
            System.out.println(topics.getTopics().size());
            QrelsInMemory qrels = new QrelsInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_training/PACt_training_rels.txt");

            for (String queryid : topics.getTopics().keySet()) {
                for (Map.Entry<String, Integer> e : qrels.getRelevantPatents(queryid).entrySet()) {
                    String docid = e.getKey();
                    int rel = e.getValue();
                    System.out.println(queryid + " 0 " + docid + " " + rel);
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(TopicsInMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
