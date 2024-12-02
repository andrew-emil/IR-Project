import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Calculations {
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

        for (Map.Entry<String, List<String>> entry : positionalIndex.entrySet()) {
            System.out.printf("%-15s | %s\n", entry.getKey(), String.join("; ", entry.getValue()));
        }
        System.out.println("=".repeat(50));

        br.close();

        return positionalIndex;
    }

    public static Map<String, int[]> termFrequencyGenerator(Map<String, List<String>> positionalIndex)
            throws IOException {
        Map<String, int[]> termFrequencyMatrix = new LinkedHashMap<>();
        String folderPath = (new File("").getAbsolutePath() + "\\data");
        File[] listOfFiles = new File(folderPath).listFiles();
        int[] vector = new int[listOfFiles.length];
        Arrays.fill(vector, 0);
        String[] docs = new String[listOfFiles.length];

        for (int index = 0; index < listOfFiles.length; index++) {
            docs = listOfFiles[index].getName().split("/");
            docs = docs[0].split(".txt");
            Arrays.sort(docs);
        }

        for (Map.Entry<String, List<String>> entry : positionalIndex.entrySet()) {
            String term = entry.getKey();
            List<String> positions = entry.getValue();

            for (String pos : positions) {
                String[] docPos = pos.split(":");
                int docID = Integer.parseInt(docPos[0].trim());

                vector[--docID] += 1;
            }

            termFrequencyMatrix.put(term, vector);
        }

        System.out.println("Term Frequency:");
        System.out.println("=".repeat(50));
        System.out.printf("%-15s | ", "Term");
        
        for (String doc: docs) {
            System.out.printf("%-5s |", doc);
        }
        System.out.println("");
        System.out.println("-".repeat(50));

        for (Map.Entry<String, int[]> entry : termFrequencyMatrix.entrySet()) {
            System.out.printf("%-15s | %s\n", entry.getKey(), String.join("; ", entry.getValue().toString()));
        }
        System.out.println("=".repeat(50));

        return termFrequencyMatrix;
    }
}
