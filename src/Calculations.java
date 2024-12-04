import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Calculations {
    private static final String folderPath = (new File("").getAbsolutePath() + "\\data");
    private static final File[] listOfFiles = new File(folderPath).listFiles();
    private static final int numOfFiles = listOfFiles.length;

    public static Map<String, List<String>> positionalIndexGenerator() throws IOException {
        String folderPath = (new File("").getAbsolutePath()) + "\\map reduce";
        BufferedReader br = new BufferedReader(new FileReader(folderPath + "/positional index output.txt"));
        Map<String, List<String>> positionalIndex = new LinkedHashMap<>();
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty())
                continue;

            String[] parts = line.split("\\s+", 2);
            if (parts.length < 2)
                continue;

            String term = parts[0];
            String[] positions = parts[1].split(";");

            positionalIndex.put(term, Arrays.asList(positions));
        }

        // Display the positional index in the console
        System.out.println("Positional Index:");
        System.out.println("=".repeat(50));
        System.out.printf("%-15s | %s\n", "Term", "Positions");
        System.out.println("-".repeat(50));

        positionalIndex
                .forEach((term, positions) -> System.out.printf("%-15s | %s%n", term, String.join("; ", positions)));
        System.out.println("=".repeat(50));

        br.close();

        return positionalIndex;
    }

    public static Map<String, int[]> termFrequencyGenerator(Map<String, List<String>> positionalIndex)
            throws IOException {
        Map<String, int[]> termFrequencyMatrix = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : positionalIndex.entrySet()) {
            String term = entry.getKey();
            List<String> positions = entry.getValue();
            int[] vector = new int[numOfFiles];
            Arrays.fill(vector, 0);

            for (String pos : positions) {
                String[] docPos = pos.split(":");
                int docID = Integer.parseInt(docPos[0].trim());

                if (docID > 0 && docID <= numOfFiles) {
                    vector[docID - 1]++; // Adjust to 0-based index
                }
            }

            termFrequencyMatrix.put(term, vector);
        }
        System.out.println("");

        Utils.displayMatrix("Term Frequency:", 6, termFrequencyMatrix);

        return termFrequencyMatrix;
    }

    public static Map<String, int[]> weightTermFrequencyGenerator(Map<String, int[]> tf) {
        Map<String, int[]> weightTF = new LinkedHashMap<>();

        // Calculate weights
        for (Map.Entry<String, int[]> entry : tf.entrySet()) {
            String term = entry.getKey();
            int[] tfVector = entry.getValue();
            int[] weightVector = new int[tfVector.length];

            for (int i = 0; i < tfVector.length; i++) {
                if (tfVector[i] > 0) {
                    weightVector[i] = (int) (Math.log10(tfVector[i]) + 1); // Weighted frequency
                }
            }

            weightTF.put(term, weightVector);
        }

        Utils.displayMatrix("Weight Term Frequency:", 6, weightTF);
        return weightTF;
    }

    public static Map<String, double[]> IDFMatrixGenerator(Map<String, int[]> tf) {
        Map<String, double[]> idfMatrix = new LinkedHashMap<>();

        for (Map.Entry<String, int[]> entry : tf.entrySet()) {
            String term = entry.getKey();
            int[] vector = entry.getValue();
            double[] idfVector = { 0, 0 };

            for (int number : vector) {
                idfVector[0] += number;
            }
            idfVector[1] += Math.log10(numOfFiles / idfVector[0]);

            idfMatrix.put(term, idfVector);
        }

        // Display the idfMatrix in the console
        System.out.println("IDF:");
        System.out.println("=".repeat(50));
        System.out.printf("%-15s |%-5s |%-10s\n", "Term", "df", "idf");
        System.out.println("-".repeat(50));

        idfMatrix
                .forEach((term, vector) -> System.out.printf("%-15s |%-5s |%-10.5f\n", term, (int) vector[0],
                        vector[1]));
        System.out.println("=".repeat(50));
        System.out.println("");

        return idfMatrix;
    }

    public static Map<String, double[]> tf_idf(Map<String, int[]> tfMatrix, Map<String, double[]> idfMatrix) {
        Map<String, double[]> tfidfMatrix = new LinkedHashMap<>();

        for (Map.Entry<String, int[]> tfEntry : tfMatrix.entrySet()) {
            String term = tfEntry.getKey();
            int[] tfVector = tfEntry.getValue();
            double[] idfVector = idfMatrix.get(term);
            double[] tfidfVector = new double[tfVector.length];
            for (int i = 0; i < tfVector.length; i++) {
                tfidfVector[i] = tfVector[i] * idfVector[1]; // Multiply TF by IDF
            }

            tfidfMatrix.put(term, tfidfVector);
        }
        System.out.println("");

        Utils.displayMatrixWithDouble("TF*IDF:", 9, tfidfMatrix);
        return tfidfMatrix;
    }

    public static Map<String, Double> tf_idf_length(Map<String, double[]> tfidfMatrix) {
        double[][] values = tfidfMatrix.values().toArray(new double[0][]);
        int rowLength = values[0].length;

        Map<String, Double> tf_idf_length = new LinkedHashMap<>();

        for (int i = 0; i < rowLength; i++) {
            double columnsSqr = 0;
            for (Map.Entry<String, double[]> tfidfEntry : tfidfMatrix.entrySet()) {
                double[] column = new double[] { tfidfEntry.getValue()[i] };
                columnsSqr = columnsSqr + Math.pow(column[0], 2);

            }
            tf_idf_length.put("d" + (i + 1), Math.sqrt(columnsSqr));
        }
        System.out.println("Tf Idf length:");
        System.out.println("=".repeat(50));
        for (int i = 0; i < tf_idf_length.size(); i++) {
            System.out.printf("%-15s |%.6f\n", "d" + (i + 1) + " length", tf_idf_length.get("d" + (i + 1)));
            System.out.println("-".repeat(50));
        }

        return tf_idf_length;

    }

    public static Map<String, double[]> normalizedTFIDF(Map<String, double[]> tfidfMatrix,
            Map<String, Double> tf_idf_length) {
        Map<String, double[]> normalizedTFIDFMatrix = new LinkedHashMap<>();

        double[] tf_idf_lengthVector = new double[numOfFiles];
        int index = 0;

        for (Map.Entry<String, Double> entry : tf_idf_length.entrySet()) {
            tf_idf_lengthVector[index] = entry.getValue();
            index++;
        }

        for (Map.Entry<String, double[]> entry : tfidfMatrix.entrySet()) {
            String term = entry.getKey();
            double[] normalizedVector = new double[numOfFiles];
            for (int i = 0; i < normalizedVector.length; i++) {
                // System.out.print(entry.getValue()[i] + " " + tf_idf_lengthVector[i] + "\n");

                normalizedVector[i] = entry.getValue()[i] / tf_idf_lengthVector[i];
            }

            normalizedTFIDFMatrix.put(term, normalizedVector);
        }
        System.out.println("");
        Utils.displayMatrixWithDouble("Normalized TF.IDF:", 9, normalizedTFIDFMatrix);

        return normalizedTFIDFMatrix;
    }
}