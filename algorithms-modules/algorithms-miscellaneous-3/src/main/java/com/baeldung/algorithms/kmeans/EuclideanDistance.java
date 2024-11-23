package com.baeldung.algorithms.kmeans;

import java.util.Map;

/**
 * Calculates the distance between two items using the Euclidean formula.
 */
public class EuclideanDistance implements Distance {

    @Override
    public double calculate(Map<String, Double> f1, Map<String, Double> f2) {
        if (f1 == null || f2 == null) {
            throw new IllegalArgumentException("Feature vectors can't be null");
        }

        double sum = 0;
        for (Map.Entry<String,Double> entry : f1.entrySet()) {
            Double v1 = entry.getValue();
            Double v2 = f2.get(entry.getKey());
            if (v1 != null && v2 != null) sum += Math.pow(v1 - v2, 2);
        }

        return Math.sqrt(sum);
    }
}
