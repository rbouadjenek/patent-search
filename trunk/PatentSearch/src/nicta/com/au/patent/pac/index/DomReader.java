/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.index;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import nicta.com.au.patent.document.PatentDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author rbouadjenek
 */
public class DomReader {

    public static String cpcRefs = "";
    public static String ipcRefs = "";
    private static final String SCHEME = "scheme";
    private static final String CPC = "cpc";
    private static final String IPC = "ipc";
    private static final String NOTE = "note";
    private static final String TEXT = "text";     // campo de busqueda
    private static final String LEVEL = "level";
    private static final String PRE_TEXT = "pre_text";
    private static final String TITLE = "class_title";
    private static final String TITLE_PART = "class_title_part";
    private static final String CLASS_REF_CPC = "class_ref_cpc"; //class_ref_cpc"
    private static final String CLASS_REF_IPC = "class_ref_ipc";
    private static final String REFERENCE = "reference";
    private static final String CLASS_ITEM = "class_item";
    private static final String CPC_TEXT = "cpc_spectif_text";
    private final File file;

    public DomReader(File file) {
        this.file = file;
    }

    public DomReader(String fileName) {
        this.file = new File(fileName);
    }

    public int counter = 0;
    public int idCounter = 0;

    public List<Document> getDocuments() {
        String level = "";
        String symbol = "";
        String title = "";
        String titlePart = "";
        String reference = "";
        String classRef = "";
        String cpcText = "";
        String refCPC = "";
        String refIPC = "";
        String note = "";
        String noteParagraph = "";
        List<Document> out = new ArrayList<>();

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(file);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("classification-item");

            //System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                //System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    level = eElement.getAttribute("level");

                    NodeList classSymbolNode = eElement.getElementsByTagName("classification-symbol");
                    if (classSymbolNode != null && classSymbolNode.getLength() > 0) {
                        symbol = classSymbolNode.item(0).getTextContent();
                    }

                    NodeList titleNode = eElement.getElementsByTagName("class-title");
                    if (titleNode != null && titleNode.getLength() > 0) {
                        title = titleNode.item(0).getTextContent();
                    }

                    NodeList titlePartNode = eElement.getElementsByTagName("title-part");
                    if (titlePartNode != null && titlePartNode.getLength() > 0) {
                        titlePart = titlePartNode.item(0).getTextContent();
                    }

                    NodeList referenceNode = eElement.getElementsByTagName("reference");
                    if (referenceNode != null && referenceNode.getLength() > 0) {
                        reference = referenceNode.item(0).getTextContent();
                        getRefClass(referenceNode);
                        /*
                         System.out.println("ipc ref : " + ipcRefs);
                         System.out.println("cpc ref : " + cpcRefs);
                         System.out.println("done!");
                         */
                    }

                    NodeList cpcTextNode = eElement.getElementsByTagName("CPC-specific-text");
                    if (cpcTextNode != null && cpcTextNode.getLength() > 0) {
                        cpcText = cpcTextNode.item(0).getTextContent();
                    }

                    NodeList noteNode = eElement.getElementsByTagName("note");
                    if (noteNode != null && noteNode.getLength() > 0) {
                        note = noteNode.item(0).getTextContent();
                    }

                    NodeList noteParaNode = eElement.getElementsByTagName("note-paragraph");
                    if (noteParaNode != null && noteParaNode.getLength() > 0) {
                        noteParagraph = noteParaNode.item(0).getTextContent();
                    }

                    // Prepare solr feeder
                    // Create solr doc
                    Document document = new Document();                    
                    document.add(new StringField(PatentDocument.Classification, symbol.replace("/", "NICTA"), Field.Store.YES));
                    document.add(new VecTextField(PatentDocument.Title, title, Field.Store.YES));
//                    System.out.println(symbol+": "+title);
                    out.add(document);
//                    System.out.println(temp + "- " + symbol);

                    level = "";
                    symbol = "";
                    title = "";
                    titlePart = "";
                    reference = "";
                    classRef = "";
                    cpcText = "";
                    refCPC = "";
                    refIPC = "";
                    note = "";
                    noteParagraph = "";
                    ipcRefs = "";
                    cpcRefs = "";
                }

            }
            return out;
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void getRefClass(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                if (tempNode.getNodeName().equalsIgnoreCase("class-ref")) {
                    //System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
                    //System.out.println("Node Value =" + tempNode.getTextContent());
                    if (tempNode.hasAttributes()) {
                        // get attributes names and values
                        NamedNodeMap nodeMap = tempNode.getAttributes();

                        for (int i = 0; i < nodeMap.getLength(); i++) {
                            Node node = nodeMap.item(i);
                            // System.out.println("attr name : " + node.getNodeName());
                            // System.out.println("attr value : " + node.getNodeValue());

                            if (node.getNodeValue().equalsIgnoreCase("cpc")) {
                                cpcRefs = cpcRefs.concat(" ");
                                cpcRefs = cpcRefs.concat(tempNode.getTextContent());
                            }

                            if (node.getNodeValue().equalsIgnoreCase("ipc")) {
                                ipcRefs = ipcRefs.concat(" ");
                                ipcRefs = ipcRefs.concat(tempNode.getTextContent());
                            }
                        }
                    }
                }

                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    getRefClass(tempNode.getChildNodes());
                }
                //System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]"); 
            }
        }
    }

    public static void main(String args[]) {
        DomReader reader = new DomReader("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/classCodes/xml/cpc-A01B.xml");
        reader.getDocuments();
    }

}
