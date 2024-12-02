import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("");
        Map<String, List<String>> positionalIndex = Calculations.positionalIndexGenerator();
        Map<String, int[]> termFrequencyMatrix = Calculations.termFrequencyGenerator(positionalIndex);
    }
}
