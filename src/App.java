import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("");
        Map<String, List<String>> positionalIndex = Calculations.positionalIndexGenerator();
        Map<String, int[]> termFrequencyMatrix = Calculations.termFrequencyGenerator(positionalIndex);
        Calculations.weightTermFrequencyGenerator(termFrequencyMatrix);
        Map<String, double[]> idfMatrix = Calculations.IDFMatrixGenerator(termFrequencyMatrix);
        Map<String, double[]> tfidfMatrix = Calculations.tf_idf(termFrequencyMatrix, idfMatrix);
        Map<String, Double> tf_idf_lengthMatrix = Calculations.tf_idf_length(tfidfMatrix);
        Map<String, double[]> normalizedTfIdfMatrix = Calculations.normalizedTFIDF(tfidfMatrix, tf_idf_lengthMatrix);
        //TODO: insert a query an get the result
    }
}
