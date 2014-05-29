package nicta.com.au.patent.document;

import java.util.ArrayList;
import java.util.List;

public class Claims {

    protected List<Claim> claim;
    protected String status;
    protected String loadSource;
    protected String lang;

    public List<Claim> getClaim() {
        if (claim == null) {
            claim = new ArrayList<>();
        }
        return this.claim;
    }

    public void setClaim(List<Claim> claim) {
        this.claim = claim;
    }

    /**
     * Obtient la valeur de la propriété status.
     *
     * @return possible object is {@link String }
     *
     */
    public String getStatus() {
        if (status == null) {
            return "new";
        } else {
            return status;
        }
    }

    /**
     * Définit la valeur de la propriété status.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété loadSource.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLoadSource() {
        return loadSource;
    }

    /**
     * Définit la valeur de la propriété loadSource.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLoadSource(String value) {
        this.loadSource = value;
    }

    /**
     * Obtient la valeur de la propriété lang.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLang() {
        return lang;
    }

    /**
     * Définit la valeur de la propriété lang.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLang(String value) {
        this.lang = value;
    }
}
