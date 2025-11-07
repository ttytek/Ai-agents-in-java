package agents;

import com.google.adk.tools.Annotations.Schema;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Provides access to a user's billing and payment history.
 * This class is designed to be registered as a tool for an ADK agent.
 */
public class BillingHistory {

    // Helper class to represent a single billing transaction
    private static class Transaction {
        String date;
        String description;
        double amount;
        String status;

        public Transaction(String date, String description, double amount, String status) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.status = status;
        }

        // Convert the object to a map, which is safer for LLM consumption
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("description", description);
            map.put("amount", String.format("%.2f", amount)); // Format as string for currency display
            map.put("status", status);
            return map;
        }
    }

    /**
     * Retrieves the complete billing history, including transactions and payment status, for a given user.
     * * The agent MUST use this tool whenever the user asks about their 'bills', 
     * 'payments', 'account balance', or 'transaction history'.
     * * @param userId The unique identifier for the user whose history is being requested.
     * @return A map containing the user's ID and a list of transactions, or an error message.
     */
    @Schema(description = "Retrieves the complete billing history, including transactions and payment status, for a given user.")
    public static Map<String, Object> billingHistory(
        @Schema(name = "userId", description = "The unique identifier of the user (e.g., email or account number).")
        String userId) {

        // --- Start Simulation/Mock Data Retrieval ---
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);

        if (userId.equalsIgnoreCase("1001-A")) {
            // Mock data for a user with clean history
            List<Transaction> transactions = Arrays.asList(
                new Transaction("2025-27-10", "Subscription Renewal (27.10-02.11)", 49.99, "Paid"),
                new Transaction("2025-20-10", "Subscription Renewal (20.10-26.10)", 49.99, "Paid"),
                new Transaction("2025-08-15", "One-time service fee", 15.00, "Paid")
            );
            
            // Convert list of objects to list of maps for easy JSON serialization
            List<Map<String, Object>> transactionMaps = transactions.stream()
                .map(Transaction::toMap)
                .collect(java.util.stream.Collectors.toList());

            response.put("transactions", transactionMaps);
            response.put("accountStatus", "Current");

        } else if (userId.equalsIgnoreCase("2002-B")) {
             // Mock data for a user with an outstanding balance
            List<Transaction> transactions = Arrays.asList(
                new Transaction("2025-11-01", "Subscription Renewal (Overdue)", 49.99, "Pending"),
                new Transaction("2025-10-01", "Subscription Renewal", 49.99, "Paid")
            );
            
            List<Map<String, Object>> transactionMaps = transactions.stream()
                .map(Transaction::toMap)
                .collect(java.util.stream.Collectors.toList());

            response.put("transactions", transactionMaps);
            response.put("accountStatus", "Balance Due: $49.99");
            
        } else {
            // Handle unknown user
            response.put("transactions", List.of());
            response.put("accountStatus", "Error");
            response.put("message", "User ID not found in the billing system.");
        }
        
        // --- End Simulation/Mock Data Retrieval ---
        return response;
    }
}