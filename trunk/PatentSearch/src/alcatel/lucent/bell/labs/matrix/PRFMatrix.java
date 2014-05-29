/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alcatel.lucent.bell.labs.matrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static nicta.com.au.main.Functions.df;
import nicta.com.au.patent.pac.index.TermFreqVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 *
 * @author rbouadjenek
 */
public final class PRFMatrix {

    /**
     * Return value for missing entries.
     */
    private final double missingEntries = 0.0;
    /**
     * Number of rows of the matrix.
     */
    final private int rows;
    private final Map<Integer, Integer> Doc_entries = new LinkedHashMap<>();
    /**
     * Number of columns of the matrix.
     */
    private final Map<String, Integer> Term_entries = new LinkedHashMap<>();
    /**
     * Storage for (sparse) matrix elements.
     */
    private final Map<Long, Double> entries = new LinkedHashMap<>();

    private final double[][] similarities;

    public PRFMatrix(Map<Integer, TermFreqVector> docsTermVectorDocs, IndexReader ir, String field, String wSchem)
            throws MatrixException, IOException {
        rows = docsTermVectorDocs.size();
        int i = 0;
        int j = 0;
        for (Map.Entry<Integer, TermFreqVector> e : docsTermVectorDocs.entrySet()) {
            Doc_entries.put(e.getKey(), i);
            i++;
            TermFreqVector vec = e.getValue();
            for (String term : vec.getTerms()) {
                if (!Term_entries.containsKey(term)) {
                    Term_entries.put(term, j);
                    j++;
                }
                if (wSchem.toLowerCase().startsWith("bin")) {
                    this.setEntry(e.getKey(), term, 1);
                } else {
                    double tf = vec.getFreq(term);
                    Term t = new Term(field, term);
                    int docs = ir.getDocCount(field);
                    double idf = Math.log10((double) docs / (ir.docFreq(t) + 1));
                    this.setEntry(e.getKey(), term, tf * idf);
                }
            }
        }
        similarities = similarities();
    }

    /**
     * Set the entry in the specified row and column.
     * <p>
     * Row and column indices start at 0 and must satisfy <ul>
     * <li><code>0 <= row < rowDimension</code></li> <li><c ode> 0 <= column <
     * columnDimension</code></li> </ul> oth erwise a
     * <code>PRFMatrixIndexException</code> is thrown.</p>
     *
     * @param row row location of entry to be set
     * @param column column location of entry to be set
     * @param value matrix entry to be set in row,column
     * @since 2.0
     */
    public void setEntry(int row, int column, double value) throws MatrixException {
        this.checkRowIndex(row);
        this.checkColumnIndex(column);
        if (value == 0.0) {
            entries.remove(computeKey(row, column));
        } else {
            entries.put(computeKey(row, column), value);
        }
    }

    public void setEntry(Integer doc, String term, double value) throws MatrixException {
        int i = Doc_entries.get(doc);
        int j = Term_entries.get(term);
        this.setEntry(i, j, value);
    }

    public double getEntry(int row, int column) throws MatrixException {
        this.checkRowIndex(row);
        this.checkColumnIndex(column);
        Double d = entries.get(computeKey(row, column));
        if (d == null) {
            return this.missingEntries;
        }
        return d;
    }

    /**
     * Compute the key to access a matrix element
     *
     * @param row row index of the matrix element
     * @param column column index of the matrix element
     * @return key within the map to access the matrix element
     */
    public long computeKey(int row, int column) {
        return (long) column * rows + row;
    }

    /**
     * Check if a row index is valid.
     *
     * @param row row index to check
     */
    public void checkRowIndex(final int row) throws MatrixException {
        if (row < 0 || row >= rows) {
            throw new MatrixException("row index " + row + " out of allowed range [" + 0 + ", " + (rows - 1) + "]");
        }
    }

    /**
     * Check if a column index is valid.
     *
     * @param column column index to check
     */
    public void checkColumnIndex(final int column)
            throws MatrixException {
        if (column < 0 || column >= Term_entries.size()) {
            throw new MatrixException("column index" + Term_entries.size() + " out of allowed range [" + 0 + "," + (Term_entries.size() - 1) + " ]");
        }
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/FrobeniusNorm.html">
     * Frobenius norm</a> of the matrix.
     *
     * @return norm
     */
    public double getFrobeniusNorm() {
        return 1;
    }

    public int getColumnDimension() {
        return Term_entries.size();
    }

    public Map<Long, Double> getEntries() {
        return entries;
    }

    public int getRowDimension() {
        return rows;
    }

    public ArrayRealVector getRowVector(final Integer doc) {
        int i = this.Doc_entries.get(doc);
        return new ArrayRealVector(this.getRow(i));
    }

    public Map<Integer, Double> getRow(final int row) {
        final int nCols = getColumnDimension();
        final Map<Integer, Double> out = new HashMap<>();
        for (int i = 0; i < nCols; ++i) {
            double v = getEntry(row, i);
            if (v != 0) {
                out.put(i, v);
            }
        }
        return out;
    }

    public ArrayRealVector getColumnVector(final String term) {
        if (!this.Term_entries.containsKey(term)) {
            return null;
        }
        int j = this.Term_entries.get(term);
        return new ArrayRealVector(this.getColumn(j));
    }

    public Map<Integer, Double> getColumn(final int column) {
        final int nRows = getRowDimension();
        final Map<Integer, Double> out = new HashMap<>();
        for (int i = 0; i < nRows; ++i) {
            double v = getEntry(i, column);
            if (v != 0) {
                out.put(i, v);
            }
        }
        return out;
    }

    public Map<String, Integer> getTerm_entries() {
        return Term_entries;
    }

    public Map<Integer, Integer> getDoc_entries() {
        return Doc_entries;
    }

    /**
     * Compute similarities between features
     *
     * @return
     */
    public double[][] similarities() {
        // safety check
//        MatrixUtils.checkMultiplicationCompatible(this, m);
        final double[][] out = new double[Term_entries.size()][Term_entries.size()];
        for (Map.Entry<String, Integer> e1 : Term_entries.entrySet()) {
            for (Map.Entry<String, Integer> e2 : Term_entries.entrySet()) {
                int i = e1.getValue();
                int j = e2.getValue();
                if (j < i) {
                    continue;
                }
                ArrayRealVector v1 = getColumnVector(e1.getKey());
                ArrayRealVector v2 = getColumnVector(e2.getKey());
                double sim = v1.getCosine(v2);
                out[i][j] = sim;
                out[j][i] = sim;
            }
        }
        return out;
    }

    public double getSimilarity(String term1, String term2) {
        if (!getTerm_entries().containsKey(term1) || !getTerm_entries().containsKey(term2)) {
            return 0;
        }
        int i = getTerm_entries().get(term1);
        int j = getTerm_entries().get(term2);
        return similarities[i][j];
    }

    public void printMatrix() {
        System.out.println("---------------------------------------------------------------");
        System.out.print("        ");
        for (int j = 0; j < this.getColumnDimension(); j++) {
            Iterator<String> it = Term_entries.keySet().iterator();
            while (it.hasNext()) {
                String term = it.next();
                int v = Term_entries.get(term);
                while (term.length() < 10) {
                    term += " ";
                }
                if (v == j) {
                    System.out.print(term.substring(0, 4) + "|");
                    break;
                }
            }
        }
        System.out.println("");
        for (int i = 0; i < this.getRowDimension(); i++) {
            for (Integer doc : Doc_entries.keySet()) {
                int v = Doc_entries.get(doc);
                if (v == i) {
                    String d = Integer.toString(doc);
                    while (d.length() < 7) {
                        d += " ";
                    }
                    System.out.print(d + "|");
                    break;
                }
            }
            for (int j = 0; j < this.getColumnDimension(); j++) {
                try {
                    String v = df.format(this.getEntry(i, j)).replaceAll(",", ".");
                    while (v.length() < 10) {
                        v += " ";
                    }
                    v = v.substring(0, 4);
                    v += "|";
                    System.out.print(v);
                } catch (MatrixException ex) {
                    Logger.getLogger(PRFMatrix.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("");
        }
        System.out.println("---------------------------------------------------------------");
    }

    public void printMatrix2() {

//        System.out.println("---------------");
//        System.out.println(this.Doc_entries.keySet().iterator().next().getDoc().getDocid());
        System.out.println("---------------");
        for (int i = 0; i < this.getRowDimension(); i++) {
            System.out.print("|");
            for (int j = 0; j < this.getColumnDimension(); j++) {
                try {
                    String v = df.format(this.getEntry(i, j)).replaceAll(",", ".");
                    v += "|";
                    System.out.print(v);
                } catch (MatrixException ex) {
                    Logger.getLogger(PRFMatrix.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("");
        }
    }

    public void printSimilarities() {
        for (double[] similaritie : similarities) {
            for (double val : similaritie) {
                String v = df.format(val).replaceAll(",", ".");
                System.out.print(v + "|");
            }
            System.out.println("");
        }
    }
}
