import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String queryString;
        System.out.println();
        Map<String, List<String>> positionalIndex = Calculations.positionalIndexGenerator();
        Map<String, int[]> termFrequencyMatrix = Calculations.termFrequencyGenerator(positionalIndex);
        Calculations.weightTermFrequencyGenerator(termFrequencyMatrix);
        Map<String, double[]> idfMatrix = Calculations.IDFMatrixGenerator(termFrequencyMatrix);
        Map<String, double[]> tfidfMatrix = Calculations.tf_idf(termFrequencyMatrix, idfMatrix);
        Map<String, Double> tf_idf_lengthMatrix = Calculations.tf_idf_length(tfidfMatrix);
        Map<String, double[]> normalizedTfIdfMatrix = Calculations.normalizedTFIDF(tfidfMatrix, tf_idf_lengthMatrix);
        do {
            System.out.print("Enter Query: ");
            queryString = sc.nextLine();
            queryString = queryString.toLowerCase();

            if (queryString.contains("and") || queryString.contains("or") || queryString.contains("not")) {
                BooleanQueryProcessor.run(positionalIndex, queryString);
            } else if (queryString.equals("stop")) {
                continue;
            } else {
                new NormalQuery(normalizedTfIdfMatrix, idfMatrix, queryString);
            }
            System.out.println("to end search: stop");
            System.out.println();
        } while (!queryString.equals("stop"));

        sc.close();
    }
}
