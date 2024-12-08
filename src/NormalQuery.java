import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NormalQuery {
    private final Map<String, double[]> normalizedTFIDF;
    private final Map<String, double[]> TermsIDF;

    public NormalQuery(Map<String, double[]> normalizedTFIDF, Map<String, double[]> TermsIDF, String query) {
        this.normalizedTFIDF = normalizedTFIDF;
        this.TermsIDF = TermsIDF;
        processQuery(query);
    }

    public Map<String, Integer> calculateTF(String query) {
        String[] terms = query.split("\\s+"); // Split query into terms
        Map<String, Integer> tf = new HashMap<>();
        for (String term : terms) {
            tf.put(term, tf.getOrDefault(term, 0) + 1);
        }
        return tf;
    }

    public Map<String, Double> calculateWeightedTF(Map<String, Integer> tf) {
        Map<String, Double> weightedTF = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            double tfValue = entry.getValue();
            weightedTF.put(entry.getKey(), tfValue > 0 ? 1 + Math.log(tfValue) : 0);
        }
        return weightedTF;
    }

    public Map<String, Double> calculateTFIDF(Map<String, Double> tf) {
        Map<String, Double> tfidf = new HashMap<>();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double tfValue = entry.getValue();
            double idfValue = TermsIDF.containsKey(term) ? TermsIDF.get(term)[1] : 0;
            tfidf.put(term, tfValue * idfValue);
        }
        return tfidf;
    }

    public Map<String, Double> calculateNormalizedTFIDF(Map<String, Double> tfidf) {
        Map<String, Double> normalizedTFIDF = new HashMap<>();
        double sumOfSquares = tfidf.values().stream().mapToDouble(value -> value * value).sum();
        double magnitude = Math.sqrt(sumOfSquares);

        for (Map.Entry<String, Double> entry : tfidf.entrySet()) {
            normalizedTFIDF.put(entry.getKey(), entry.getValue() / magnitude);
        }
        return normalizedTFIDF;
    }

    private Set<Integer> matchDocuments(String[] terms) {
        Set<Integer> matchedDocuments = null; // Start with null to initialize it with the first term's documents.

        for (String term : terms) {
            double[] docs = normalizedTFIDF.get(term);

            // If the term is not found in normalizedTFIDF, skip it
            if (docs == null) {
                continue;
            }

            // Collect documents with normalized TF-IDF > 0 for the current term
            Set<Integer> currentDocs = new HashSet<>();
            for (int i = 1; i <= docs.length; i++) {
                if (docs[i - 1] > 0) {
                    currentDocs.add(i);
                }
            }

            // If matchedDocuments is null, initialize it with currentDocs
            if (matchedDocuments == null) {
                matchedDocuments = new HashSet<>(currentDocs);
            } else {
                // Retain only the documents that exist in both sets
                matchedDocuments.retainAll(currentDocs);
            }

            // If no documents remain in the intersection, early exit
            if (matchedDocuments.isEmpty()) {
                break;
            }
        }

        // Return the resulting matched documents, or an empty set if none match
        return matchedDocuments == null ? new HashSet<>() : matchedDocuments;
    }

    private Map<Integer, Double> calculateSimilarity(String[] terms, Map<String, Double> normalizedTFIDF,
            Set<Integer> matchedDocs) {
        Map<Integer, Double> similarityScores = new HashMap<>();

        // For each term in the query
        for (String term : terms) {
            double queryNormTFIDF = normalizedTFIDF.getOrDefault(term, 0.0); // Get the normalized TF-IDF for the query

            if (queryNormTFIDF > 0) { // Only proceed if the term has a non-zero normalized TF-IDF value
                double[] docNormTFIDF = this.normalizedTFIDF.get(term); // Get the document's normalized TF-IDF values
                                                                        // for the term

                if (docNormTFIDF != null) {
                    // For each document in the matchedDocs set
                    for (Integer docId : matchedDocs) {
                        int index = docId - 1; // Adjust index to match the array's 0-based indexing
                        if (docNormTFIDF[index] > 0) {
                            // Multiply the query's normalized TF-IDF by the document's normalized TF-IDF
                            double product = queryNormTFIDF * docNormTFIDF[index];

                            // Add the product to the similarity score for the document
                            similarityScores.put(docId, similarityScores.getOrDefault(docId, 0.0) + product);
                        }
                    }
                }
            }
        }
        return similarityScores;
    }

    public void processQuery(String query) {
        String[] terms = query.split("\\s+");
        Map<String, Integer> tf = calculateTF(query);
        Map<String, Double> weightedTF = calculateWeightedTF(tf);
        Map<String, Double> tfidf = calculateTFIDF(weightedTF);
        Map<String, Double> normalizedTFIDF = calculateNormalizedTFIDF(tfidf);
        Set<Integer> matchedDocs = matchDocuments(terms);
        Map<Integer, Double> similarityScores = calculateSimilarity(terms, normalizedTFIDF, matchedDocs);

        List<Map.Entry<Integer, Double>> sortedScores = new ArrayList<>(similarityScores.entrySet());
        sortedScores.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        System.out.println("Similar Documents:");
        for (Map.Entry<Integer, Double> entry : sortedScores) {
            System.out.println("d" + entry.getKey());
        }
    }
}
