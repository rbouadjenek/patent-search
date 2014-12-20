/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.document;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.SAXException;

/**
 *
 * @author rbouadjenek
 */
public class PatentDocument implements Comparable<PatentDocument> {

    protected File file;
    protected String ucid;
    protected List<Claims> claims;
    protected Description description;
    protected Abstract abstrac;
    protected TechnicalData technicalData;
    protected String lang;
    public static String Title = "title";
    public static String Classification = "class";
    public static String Abstract = "abstract";
    public static String Description = "description";
    public static String Claims = "claims";
    public static String FileName = "filename";

    public PatentDocument(String file) {
        this.claims = new ArrayList<>();
        this.file = new File(file);
        this.Parse();
    }

    public PatentDocument(File file) {
        this.claims = new ArrayList<>();
        this.file = file;
        this.Parse();
    }

    private void Parse() {
        try {
            // cr?ation d'une fabrique de documents
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // cr?ation d'un constructeur de documents
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            // lecture du contenu d'un fichier XML avec DOM
            Document document = constructeur.parse(this.file);
            Element racine = document.getDocumentElement();
            this.ucid = racine.getAttribute("ucid"); // Set the Patent name
            this.lang = racine.getAttribute("lang"); // Set the Patent langage
            NodeList claimsList = racine.getElementsByTagName("claims");
            for (int i = 0; i < claimsList.getLength(); i++) { // Iteration over CLAIMS NODES
                Element elementClaims = (Element) claimsList.item(i);
                Claims c = new Claims();
                c.setLang(elementClaims.getAttribute("lang"));
                c.setLoadSource(elementClaims.getAttribute("load-source"));
                c.setStatus(elementClaims.getAttribute("status"));
                NodeList claimNodeList = elementClaims.getElementsByTagName("claim");
                List<Claim> claimList = new ArrayList<>();

                for (int j = 0; j < claimNodeList.getLength(); j++) {// Iteration over CLAIM NODES
                    Element elementClaim = (Element) claimNodeList.item(j);

                    if (claimNodeList.getLength() > 1) {
                        Claim claim = new Claim();
                        String num;
                        Pattern pattern = Pattern.compile("([0-9]+)");
                        Matcher m = pattern.matcher(elementClaim.getAttribute("num"));
                        if (m.find()) {
                            try {
                                num = String.valueOf(Integer.parseInt(m.group(1)));
                            } catch (NumberFormatException e) {
                                num = "-1";
                            }
                        } else {
                            num = String.valueOf(j + 1);
                        }
                        claim.setNum(num);
                        claim.setClaimText(elementClaim.getTextContent());
                        claimList.add(claim);
                    } else {// Cope with the problem of Claim numbers
                        Pattern pattern = Pattern.compile("([0-9]+)\\. ");
                        String claimsText = elementClaim.getTextContent();
                        Matcher matcher = pattern.matcher(claimsText);
                        String t[] = claimsText.split("([0-9]+)\\. ");
                        int b = 0;
                        while (matcher.find()) {
                            Claim claim = new Claim();
                            String num = matcher.group();
                            num = num.substring(0, num.length() - 2);
                            claim.setNum(String.valueOf(num));
                            claim.setClaimText(t[b + 1]);
                            b++;
                            claimList.add(claim);
                        }
                    }
                }
                c.setClaim(claimList);
                this.claims.add(c);
            }
            NodeList descriptionList = racine.getElementsByTagName("description");
            for (int i = 0; i < descriptionList.getLength(); i++) {// ITERATION OVER DESCRIPTION
                Element elementDescription = (Element) descriptionList.item(i);
                if (elementDescription.getAttribute("lang").toLowerCase().equals("en") || this.description == null) {
                    this.description = new Description();
                    this.description.setLang(elementDescription.getAttribute("lang"));
                    this.description.setStatus(elementDescription.getAttribute("status"));
                    this.description.setLoadSource(elementDescription.getAttribute("load-source"));
                    NodeList pNodeList = elementDescription.getElementsByTagName("p");
                    List<P> pList = new ArrayList<>();
                    for (int j = 0; j < pNodeList.getLength(); j++) {// ITERATION OVER DESCRIPTION
                        Element elementP = (Element) pNodeList.item(j);
                        P p = new P();
                        p.setContent(elementP.getTextContent());
                        String num;
                        Pattern pattern = Pattern.compile("([0-9]+)");
                        Matcher m = pattern.matcher(elementP.getAttribute("num"));
                        if (m.find()) {
                            num = String.valueOf(Integer.parseInt(m.group(1)));
                        } else {
                            num = String.valueOf(j + 1);
                        }
                        p.setNum(num);
                        pList.add(p);
                    }
                    this.description.setP(pList);
                }
            }
            NodeList abstractList = racine.getElementsByTagName("abstract");
            this.abstrac = new Abstract();
            for (int i = 0; i < abstractList.getLength(); i++) {// ITERATION OVER absract
                Element elementAbstract = (Element) abstractList.item(i);
                if (elementAbstract.getAttribute("lang").toLowerCase().equals("en")) {
                    this.abstrac.setLang(elementAbstract.getAttribute("lang"));
                    this.abstrac.setContent(elementAbstract.getTextContent());
                }
            }
            Element bibliographicData = (Element) racine.getElementsByTagName("bibliographic-data").item(0);
            Element technicalDataElement = (Element) bibliographicData.getElementsByTagName("technical-data").item(0);
            NodeList inventionTitleNodeList = technicalDataElement.getElementsByTagName("invention-title");

            List<InventionTitle> inventionTitleList = new ArrayList<>();
            for (int i = 0; i < inventionTitleNodeList.getLength(); i++) {// ITERATION OVER Ivention Titles
                InventionTitle inventionTitle = new InventionTitle();
                Element elementIventionTitle = (Element) inventionTitleNodeList.item(i);
                inventionTitle.setLang(elementIventionTitle.getAttribute("lang"));
                inventionTitle.setContent(elementIventionTitle.getTextContent());
                inventionTitleList.add(inventionTitle);
            }
            this.technicalData = new TechnicalData();
            this.technicalData.setInventionTitle(inventionTitleList);
            Element classificationsIpcrElement = (Element) technicalDataElement.getElementsByTagName("classifications-ipcr").item(0);
            List<ClassificationIpcr> classificationIpcrList = new ArrayList<>();
            if (classificationsIpcrElement != null) {
                NodeList classificationIpcrNodeList = classificationsIpcrElement.getElementsByTagName("classification-ipcr");
                for (int i = 0; i < classificationIpcrNodeList.getLength(); i++) {// ITERATION OVER Ivention Titles
                    ClassificationIpcr classificationIpcr = new ClassificationIpcr();
                    Element elementClassificationIpcr = (Element) classificationIpcrNodeList.item(i);
                    classificationIpcr.setContent(elementClassificationIpcr.getTextContent());
                    classificationIpcrList.add(classificationIpcr);
                }
            }
            this.technicalData.setClassificationIpcr(classificationIpcrList);

        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            e.printStackTrace();
        }
    }

    public List<Claims> getClaims() {
        return claims;
    }

    public Description getDescription() {
        return description;
    }

    public Abstract getAbstrac() {
        return abstrac;
    }

    public String getUcid() {
        return ucid;
    }

    public TechnicalData getTechnicalData() {
        return technicalData;
    }

    public void setClaims(List<Claims> claims) {
        this.claims = claims;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setAbstrac(Abstract abstrac) {
        this.abstrac = abstrac;
    }

    public void setTechnicalData(TechnicalData technicalData) {
        this.technicalData = technicalData;
    }

    public void setUcid(String ucid) {
        this.ucid = ucid;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public int compareTo(PatentDocument o) {
        return -ucid.compareTo(o.getUcid());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        PatentDocument pt = new PatentDocument("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_training/topics/PACt-2_EP-1306721-A2.xml");
        System.out.println(pt.getTechnicalData().classificationIpcr.get(0).content);
    }

}