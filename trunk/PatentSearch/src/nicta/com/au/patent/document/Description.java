//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5-2 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2013.08.29 à 05:53:32 PM EST 
//
package nicta.com.au.patent.document;

import java.util.List;

public class Description {

    protected List<P> p;
    protected String status;
    protected String loadSource;
    protected String lang;

    public List<P> getP() {
        return p;
    }

    public void setP(List<P> p) {
        this.p = p;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getLoadSource() {
        return loadSource;
    }

    public void setLoadSource(String value) {
        this.loadSource = value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String value) {
        this.lang = value;
    }
}
