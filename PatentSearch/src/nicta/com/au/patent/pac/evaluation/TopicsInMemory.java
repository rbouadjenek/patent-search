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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.PatentQuery;

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
            QrelsInMemory qrels = new QrelsInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2011/PAC_test/PAC_test_rels.txt");

            Map<String, Float> boosts = new HashMap<>();
            boosts.put(PatentDocument.Classification, new Float(0));
            boosts.put(PatentDocument.Title, new Float(0));
            boosts.put(PatentDocument.Abstract, new Float(1));
            boosts.put(PatentDocument.Description, new Float(0));
            boosts.put("descriptionP5", new Float(0));
            boosts.put(PatentDocument.Claims, new Float(0));
            boosts.put("claims1", new Float(0));
            for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
                PatentQuery pt = new PatentQuery(e.getValue(), boosts, true, true);
                System.out.println(e.getKey() + "\t" + pt.getAbstractSize()+ "\t" +qrels.getNumberOfRelevantPatent(e.getKey()));

            }

        } catch (IOException ex) {
            Logger.getLogger(TopicsInMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
