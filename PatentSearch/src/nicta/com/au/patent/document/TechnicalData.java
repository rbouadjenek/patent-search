package nicta.com.au.patent.document;

import java.util.List;

public class TechnicalData {

    protected List<ClassificationIpcr> classificationIpcr;
    protected List<InventionTitle> inventionTitle;

    public List<ClassificationIpcr> getClassificationIpcr() {
        return classificationIpcr;
    }

    

    public List<InventionTitle> getInventionTitle() {
        return inventionTitle;
    }

    public void setClassificationIpcr(List<ClassificationIpcr> classificationIpcr) {
        this.classificationIpcr = classificationIpcr;
    }

    public void setInventionTitle(List<InventionTitle> inventionTitle) {
        this.inventionTitle = inventionTitle;
    }

}
