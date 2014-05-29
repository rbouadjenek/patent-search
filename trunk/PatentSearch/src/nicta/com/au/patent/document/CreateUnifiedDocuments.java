/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.document;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nicta.com.au.main.Functions;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author rbouadjenek
 */
public class CreateUnifiedDocuments {

    int total = 0;

    public void writeUnifiedPatentDocument(PatentDocument pt, File directory) throws FileNotFoundException, UnsupportedEncodingException {
        String fileName = directory.getAbsolutePath() + "/" + pt.getUcid() + ".xml";
        total++;
        System.out.println(total + "- Creating: " + fileName);
        try (PrintWriter writer = new PrintWriter(fileName, "ISO-8859-1")) {
            writer.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
            writer.println("<patent-document ucid=\"" + pt.getUcid() + "\">");
            writer.println("\t<bibliographic-data>");
            writer.println("\t\t<technical-data>");
            writer.println("\t\t\t<classifications-ipcr>");
            for (ClassificationIpcr ipc : pt.getTechnicalData().getClassificationIpcr()) {
                writer.println("\t\t\t\t<classification-ipcr>" + StringEscapeUtils.escapeXml(ipc.getContent()) + "</classification-ipcr>");
            }
            writer.println("\t\t\t</classifications-ipcr>");
            for (InventionTitle title : pt.getTechnicalData().getInventionTitle()) {
                writer.println("\t\t\t<invention-title  lang=\"" + title.getLang() + "\">" + StringEscapeUtils.escapeXml(title.getContent()) + "</invention-title>");
            }
            writer.println("\t\t</technical-data>");

            writer.println("\t</bibliographic-data>");
            if (pt.getAbstrac() != null) {
                writer.println("\t<abstract lang=\"" + pt.getAbstrac().getLang() + "\">");
                writer.println(StringEscapeUtils.escapeXml(pt.getAbstrac().getContent()));
                writer.println("\t</abstract>");
            }
            if (pt.getDescription() != null) {
                writer.println("\t<description load-source=\"" + pt.getDescription().getLoadSource() + "\" status=\"" + pt.getDescription().getStatus() + "\" lang=\"" + pt.getDescription().getLang() + "\">");

                for (P p : pt.getDescription().getP()) {
                    writer.println("\t\t<p num=\"" + p.getNum() + "\">");
                    writer.println(StringEscapeUtils.escapeXml(p.getContent()));
                    writer.println("\t\t</p>");
                }
                writer.println("\t</description>");
            }
            if (pt.getClaims() != null) {
                for (Claims claims : pt.getClaims()) {
                    writer.println("\t<claims load-source=\"" + claims.getLoadSource() + "\" status=\"" + claims.getStatus() + "\" lang=\"" + claims.getLang() + "\">");
                    for (Claim claim : claims.getClaim()) {
                        writer.println("\t\t<claim num=\"" + claim.getNum() + "\">");
                        writer.println(StringEscapeUtils.escapeXml(claim.getClaimText()));
                        writer.println("\t\t</claim>");
                    }
                    writer.println("\t</claims>");
                }
            }
            writer.println("</patent-document>");
            writer.flush();
        }
    }

    public PatentDocument analyze(File directory) {
        PatentDocument unifiedPatentDoc = null;
        List<PatentDocument> list = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (!file.getName().startsWith("UN-") && file.getName().toLowerCase().endsWith(".xml")) {
                PatentDocument pt = new PatentDocument(file);
                if (pt.getUcid() != null) {
                    list.add(pt);
                }
            }
        }
        Collections.sort(list);
        for (PatentDocument pt : list) {
            if (unifiedPatentDoc == null) {
                unifiedPatentDoc = pt;
                if (unifiedPatentDoc.getUcid().startsWith("EP")) {
                    unifiedPatentDoc.setUcid("UN-" + unifiedPatentDoc.getUcid().substring(0, 10)); // Set the good unified name to EP patents
                } else if (unifiedPatentDoc.getUcid().startsWith("WO")) {
                    unifiedPatentDoc.setUcid("UN-" + unifiedPatentDoc.getUcid().substring(0, 13)); // Set the good unified name to WO patents
                }
            } else {
                // Check Abtract
                if (unifiedPatentDoc.getAbstrac().getContent() == null) {
                    unifiedPatentDoc.setAbstrac(pt.getAbstrac());
                }
                // Check Description
                if (unifiedPatentDoc.getDescription() == null) {
                    unifiedPatentDoc.setDescription(pt.getDescription());
                } else if (unifiedPatentDoc.getDescription().getP().isEmpty()) {
                    unifiedPatentDoc.setDescription(pt.getDescription());
                }
                // Check Claims
                if (unifiedPatentDoc.getClaims() == null) {
                    unifiedPatentDoc.setClaims(pt.getClaims());
                } else if (unifiedPatentDoc.getClaims().isEmpty()) {
                    unifiedPatentDoc.setClaims(pt.getClaims());
                }
                // Check Technical data
                if (unifiedPatentDoc.getTechnicalData() == null) {
                    unifiedPatentDoc.setTechnicalData(pt.getTechnicalData());
                }
                if (unifiedPatentDoc.getTechnicalData().getInventionTitle() == null) {
                    unifiedPatentDoc.getTechnicalData().setInventionTitle(pt.getTechnicalData().getInventionTitle());
                } else if (unifiedPatentDoc.getTechnicalData().getInventionTitle().isEmpty()) {
                    unifiedPatentDoc.getTechnicalData().setInventionTitle(pt.getTechnicalData().getInventionTitle());
                }
                if (unifiedPatentDoc.getTechnicalData().getClassificationIpcr() == null) {
                    unifiedPatentDoc.getTechnicalData().setClassificationIpcr(pt.getTechnicalData().getClassificationIpcr());
                } else if (unifiedPatentDoc.getTechnicalData().getClassificationIpcr().isEmpty()) {
                    unifiedPatentDoc.getTechnicalData().setClassificationIpcr(pt.getTechnicalData().getClassificationIpcr());
                }
            }
        }
        return unifiedPatentDoc;
    }

    public boolean index(File dataDir, FileFilter filter) throws Exception {
        File[] listFiles = dataDir.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                if (index(file, filter) == true) {
                    this.writeUnifiedPatentDocument(this.analyze(file), file);
                }
            } else {
                if (!file.isHidden() && file.exists() && file.canRead() && (filter == null || filter.accept(file))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean index(String dataDir, FileFilter filter) throws Exception {
        return this.index(new File(dataDir), filter);
    }

    private static class TextFilesFilter implements FileFilter {

        @Override
        public boolean accept(File path) {
            return path.getName().toLowerCase()
                    .endsWith(".xml");
        }
    }

    public int getTotal() {
        return total;
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String dir;
        if (args.length == 0) {
            dir = "/Volumes/Macintosh HD/Users/rbouadjenek/test/patents/WO/001981/00/01/";

        } else {
            dir = args[0];
        }
        CreateUnifiedDocuments c = new CreateUnifiedDocuments();
        long start = System.currentTimeMillis();
        c.index(dir, new TextFilesFilter());
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(c.getTotal() + " files has been created in " + Functions.getTimer(millis) + ".");
    }

}
