/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alcatel.lucent.bell.labs.matrix;

import java.util.Map;

/**
 *
 * @author rbouadjenek
 */
public class ArrayRealVector {

    /**
     * Entries of the vector.
     */
    protected Map<Integer, Double> data;

    /**
     * Create a new ArrayRealVector using the input array as the underlying data
     * array.
     * <p>
     * If an array is built specially in order to be embedded in a
     * ArrayRealVector and not used directly, the <code>copyArray</code> may be
     * set to      <code>false</code. This will prevent the copying and improve
     * performance as no new array will be built and no data will be copied.</p>
     *
     * @param data
     * @throws IllegalArgumentException if <code>d</code> is empty
     * @throws NullPointerException if <code>d</code> is null
     * @see #ArrayRealVector(double[])
     */
    public ArrayRealVector(Map<Integer, Double> data) throws NullPointerException, IllegalArgumentException {
        this.data = data;
    }

    public double getLength() {
        double length = 0;
        for (Double d : data.values()) {
            length += Math.pow(d, 2);
        }
        length = Math.sqrt(length);
        return length;
    }

    public double getValue(int i) {
        if (data.containsKey(i)) {
            return data.get(i);
        } else {
            return 0;
        }
    }

    public double dotProduct(ArrayRealVector v) {
        double dot = 0;
        for (Map.Entry<Integer, Double> e : data.entrySet()) {
            int i = e.getKey();
            double val = e.getValue();
            dot += val * v.getValue(i);
        }
        return dot;
    }

    public double getCosine(ArrayRealVector v) {
        double dot = this.dotProduct(v);
        dot = dot / (this.getLength() * v.getLength());
        return dot;
    }

}
