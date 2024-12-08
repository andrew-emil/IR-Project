import java.util.*;
import java.util.stream.Collectors;

public class BooleanQueryProcessor {
    private final Map<String, List<String>> positionalIndex;

    public BooleanQueryProcessor(Map<String, List<String>> positionalIndex) {
        this.positionalIndex = positionalIndex;
    }

    public Set<String> processQuery(String query) {
        String[] tokens = query.split("\\s+");
        Stack<Set<String>> operandStack = new Stack<>();
        Stack<String> operatorStack = new Stack<>();

        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("NOT", 3); // Highest precedence
        precedence.put("AND", 2);
        precedence.put("OR", 1); // Lowest precedence

        for (String token : tokens) {
            token = token.trim().toLowerCase(); // Normalize to lower case

            if (token.equals("and") || token.equals("or") || token.equals("not")) {
                String operator = token.toUpperCase();
                while (!operatorStack.isEmpty() &&
                        precedence.get(operator) <= precedence.get(operatorStack.peek())) {
                    applyOperator(operandStack, operatorStack.pop());
                }
                operatorStack.push(operator);
            } else {
                Set<String> docIds = getDocumentsForTerm(token);
                operandStack.push(docIds);
            }
        }

        // Apply remaining operators
        while (!operatorStack.isEmpty()) {
            applyOperator(operandStack, operatorStack.pop());
        }

        return operandStack.isEmpty() ? new HashSet<>() : operandStack.pop();
    }

    private void applyOperator(Stack<Set<String>> operandStack, String operator) {
        if (operator.equals("NOT")) {
            // If the stack is empty, assume the context is all documents
            Set<String> setToExclude = operandStack.pop();
            Set<String> currentContext = operandStack.isEmpty() ? getAllDocumentIDs() : operandStack.pop();
            operandStack.push(currentContext);
            operandStack.push(notOperation(currentContext, setToExclude));
        } else {
            // For AND/OR, ensure there are two operands
            if (operandStack.size() < 2) {
                throw new IllegalStateException("Insufficient operands for operator: " + operator);
            }

            Set<String> operand2 = operandStack.pop();
            Set<String> operand1 = operandStack.pop();

            if (operator.equals("AND")) {
                operandStack.push(andOperation(operand1, operand2));
            } else if (operator.equals("OR")) {
                operandStack.push(orOperation(operand1, operand2));
            }
        }
    }

    private Set<String> getDocumentsForTerm(String term) {
        List<String> postings = positionalIndex.getOrDefault(term, Collections.emptyList());
        return postings.stream()
                .map(posting -> posting.split(":")[0]) // Extract document IDs
                .collect(Collectors.toSet());
    }

    private Set<String> andOperation(Set<String> set1, Set<String> set2) {
        Set<String> result = new HashSet<>(set1);
        result.retainAll(set2); // Intersection
        return result;
    }

    private Set<String> orOperation(Set<String> set1, Set<String> set2) {
        Set<String> result = new HashSet<>(set1);
        result.addAll(set2); // Union
        return result;
    }

    private Set<String> notOperation(Set<String> currentContext, Set<String> setToExclude) {
        Set<String> result = new HashSet<>(currentContext);
        result.removeAll(setToExclude); // Remove documents in `setToExclude` from the `currentContext`
        return result;
    }

    private Set<String> getAllDocumentIDs() {
        return positionalIndex.values().stream()
                .flatMap(List::stream)
                .map(posting -> posting.split(":")[0]) // Extract document IDs
                .collect(Collectors.toSet());
    }

    public static void run(Map<String, List<String>> positionalIndex, String query) {
        BooleanQueryProcessor processor = new BooleanQueryProcessor(positionalIndex);
        System.out.println("your Query: " + query);
        Set<String> result = processor.processQuery(query);
        System.out.print("Result: ");
        if (!result.isEmpty()) {
            // Join the results with ", " and prefix "D" for each document
            String formattedResult = result.stream()
                    .map(doc -> "D" + doc.trim())
                    .collect(Collectors.joining(", "));
            System.out.println(formattedResult);
        } else {
            System.out.println("No results found.");
        }
    }
}
