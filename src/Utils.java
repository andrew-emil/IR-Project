import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class Utils {
    private static final String folderPath = (new File("").getAbsolutePath() + "\\data");
    private static final File[] listOfFiles = new File(folderPath).listFiles();
    private static final int numOfFiles = listOfFiles.length;
    protected static String[] docs = initializingDocs();

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

    public static void displayMatrix(String tableName, int width, Map<String, int[]> matrix) {
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


    // map with double value
    public static void displayMatrixWithDouble(String tableName, int width, Map<String, double[]> matrix) {
        System.out.println(tableName);
        System.out.println("=".repeat(120));
        System.out.printf("%-15s |", "Term");

        for (String doc : docs) {
            String docName = "d" + doc;
            int leftPadding = (width - docName.length()) / 2;
            int rightPadding = width - docName.length() - leftPadding;

            String centeredDoc = " ".repeat(leftPadding) + docName + " ".repeat(rightPadding);
            System.out.printf("%s|", centeredDoc);
        }
        System.out.println("");
        System.out.println("-".repeat(120));

        for (Map.Entry<String, double[]> entry : matrix.entrySet()) {
            System.out.printf("%-15s |", entry.getKey());
            for (double num : entry.getValue()) {
                String formattedNumber = String.format("%.5f", num);

                String centeredDoc = " " + formattedNumber + " ";
                System.out.printf("%s|", centeredDoc);
            }
            System.out.println("");
            System.out.println("-".repeat(120));
        }
        System.out.println("");
    }

}
