import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Calculations {
    private static final String folderPath = (new File("").getAbsolutePath() + "\\data");
    private static final File[] listOfFiles = new File(folderPath).listFiles();
    private static final int numOfFiles = listOfFiles.length;
    private static String[] docs = initializingDocs();

    /**
     * Initializes the document names from the files in the folder, sorted
     * numerically.
     *
     * @return An array of document names.
     */
    private static String[] initializingDocs() {
        if (listOfFiles == null || listOfFiles.length == 0) {
            System.err.println("No files found in the directory: " + folderPath);
            return new String[0];
        }

        String[] docs = new String[numOfFiles];
        for (int index = 0; index < listOfFiles.length; index++) {
            String[] doc = listOfFiles[index].getName().split("/");
            docs[index] = doc[0].split(".txt")[0];
        }
        Arrays.sort(docs, Comparator.comparingInt(Integer::parseInt));
        return docs;
    }

    private static void displayMatrix(String tableName, int width, Map<String, int[]> matrix) {
        System.out.println(tableName);
        System.out.println("=".repeat(100));
        System.out.printf("%-15s | ", "Term");

        for (String doc : docs) {
            String docName = "d" + doc;
            int leftPadding = (width - docName.length()) / 2;
            int rightPadding = width - docName.length() - leftPadding;

            String centeredDoc = " ".repeat(leftPadding) + docName + " ".repeat(rightPadding);
            System.out.printf("%s|", centeredDoc);
        }
        System.out.println("");
        System.out.println("-".repeat(100));

        for (Map.Entry<String, int[]> entry : matrix.entrySet()) {
            System.out.printf("%-15s | ", entry.getKey());
            for (int num : entry.getValue()) {
                int leftPadding = (width - 1) / 2;
                int rightPadding = width - 1 - leftPadding;

                String centeredDoc = " ".repeat(leftPadding) + num + " ".repeat(rightPadding);
                System.out.printf("%s|", centeredDoc);
            }
            System.out.println("");
            System.out.println("-".repeat(100));
        }
        System.out.println("");
    }

    /**
     * Generates a positional index from a file and displays it in the console.
     * 
     * @return A map containing terms as keys and their respective positions as
     *         values.
     * @throws IOException If an I/O error occurs during file reading.
     */
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

    /**
     * Generates a term frequency matrix from a positional index.
     *
     * @param positionalIndex A map containing terms and their positions.
     * @return A term frequency matrix with terms as keys and frequency vectors as
     *         values.
     * @throws IOException If an I/O error occurs.
     */
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
        displayMatrix("Term Frequency:", 6, termFrequencyMatrix);

        return termFrequencyMatrix;
    }

    /**
     * Generates a weight term frequency matrix from a term frequency.
     *
     * @param tf A term frequency matrix with terms as keys and frequency vectors as
     *           values.
     * @return A weight term frequency matrix with terms as keys and weight
     *         frequency vectors as
     *         values.
     * @throws IOException If an I/O error occurs.
     */
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

        displayMatrix("Weight Term Frequency:", 6, weightTF);
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
                .forEach((term, vector) -> System.out.printf("%-15s |%-5s |%-10.5f\n", term,(int) vector[0], vector[1]));
        System.out.println("=".repeat(50));
        System.out.println("");

        return idfMatrix;
    }
}
